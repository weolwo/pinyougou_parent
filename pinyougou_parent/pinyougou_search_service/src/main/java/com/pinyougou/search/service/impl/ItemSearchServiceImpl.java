package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 30000)
public class ItemSearchServiceImpl implements ItemSearchService {


    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();
        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));

        //按照关键字查询(高亮显示)
        map.putAll(searchList(searchMap));//map.putAll可以合两个map,如果key相同后者覆盖前者
        //按照关键字查询,商品分类
        List<String> categoryList = searchCategoryList(searchMap);

        map.put("categoryList", categoryList);

        //根据商品分类名称查询规格和品牌信息,此处我们应该判断用户是否选择分类,应按照用户当前所选的分类显示相应的品牌信息
        String category = (String) searchMap.get("category");
        if (!"".equals(category)){//如果有分类名称,按分类名称查询

            map.putAll(searchBrandAndSpecList(category));
        }else {

            if (categoryList.size() > 0) {//如果没有分类名称,就默认查询第一个

                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        System.out.println(map.toString());
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(Long[] goodsIdList) {

        Query query=new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 根据商品分类名称查询规格和品牌信息
     *
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //根据分类名称查询模板id
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        //根据模板id查询品牌信息
        if (templateId != null) {
            //查询平牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);

            //查询规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }
        return map;
    }

    //根据搜索关键字查询商品分类名称列表
    private List<String> searchCategoryList(Map searchMap) {

        List<String> list = new ArrayList<>();
        //创建查询条件
        Query query = new SimpleQuery();
        //组装查询条件
        //通过关键字查询
        String keywords = (String) searchMap.get("keywords");

        if (StringUtils.isNotBlank(keywords)) {

            Criteria criteria = new Criteria("item_keywords").is(keywords);
            query.addCriteria(criteria);
        }

        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //得到分组页
        GroupPage<SolrItem> page = solrTemplate.queryForGroupPage(query, SolrItem.class);

        //构建列得到分组结果集
        GroupResult<SolrItem> groupResult = page.getGroupResult("item_category");

        //得到分组结果入口页
        Page<GroupEntry<SolrItem>> entryPage = groupResult.getGroupEntries();

        //得到入口集合
        List<GroupEntry<SolrItem>> content = entryPage.getContent();

        for (GroupEntry<SolrItem> entry : content) {
            // entry.getGroupValue(),分类名称
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;

    }

    //高亮显示
    private Map searchList(Map searchMap) {
        //封装map
        Map map = new HashMap();

        //构造查询对象
        HighlightQuery query = new SimpleHighlightQuery();

        //1.关键字查询
        String keywords = (String) searchMap.get("keywords");
        if (StringUtils.isNotBlank(keywords)) {

            Criteria criteria = new Criteria("item_keywords").is(keywords);
            query.addCriteria(criteria);
        }

        //2.构建高亮显示三部曲
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮域,可以设置多个
        highlightOptions.setSimplePrefix("<em style='color:red'>");//设置前缀
        highlightOptions.setSimplePostfix("</em>");//设置后缀
        query.setHighlightOptions(highlightOptions);

        //3.过滤查询
        //3.1 分类过滤查询
        String category = (String) searchMap.get("category");
        if (StringUtils.isNotBlank(category)) {
            Criteria criteria = new Criteria("item_category").is(category);
            FilterQuery filterQuery = new SimpleFilterQuery(criteria);
            query.addFilterQuery(filterQuery);
        }

        //3.2,根据品牌查询
        String brand = (String) searchMap.get("brand");
        if (StringUtils.isNotBlank(brand)) {
            Criteria criteria = new Criteria("item_brand").is(brand);
            FilterQuery filterQuery = new SimpleFilterQuery(criteria);
            query.addFilterQuery(filterQuery);
        }

        //3.3根据规格查询
        //把前端传过来的spec转换成map对象
        Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
        //如果specMap不玩null并且size>0
        if (specMap != null&&specMap.size()>0) {
            for (String key : specMap.keySet()) {
                Criteria criteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //3.4 根据价格区间查询
        String price = (String) searchMap.get("price");
        if (StringUtils.isNotBlank(price)){
            String[] split = price.split("-");
            //区间价格不低于0
            if(!"0".equals(split[0])){
                Criteria criteria = new Criteria("item_price").greaterThanEqual(split[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }

            //区间终点不等于*
            if(!"*".equals(split[1])){
                Criteria criteria = new Criteria("item_price").lessThanEqual(split[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //3.5分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo==null){
            pageNo=1;//默认当前页
        }

        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize==null){
            pageSize=20;//默认20
        }
        query.setOffset((pageNo-1)*pageSize);//设置分页开始页
        query.setRows(pageSize);//每页记录数

        //3.6排序
        String sortValue = (String) searchMap.get("sort");//排序值
        String sortField = (String) searchMap.get("sortField");//排序字段

        //只有当二者都有值时才进行排序操作
        if (StringUtils.isNotBlank(sortValue)){

            if (sortValue.equals("ASC")){

                Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }

            if (sortValue.equals("DESC")){

                Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        HighlightPage<SolrItem> page = solrTemplate.queryForHighlightPage(query, SolrItem.class);
        //高亮入口(每条记录的高亮入口)
        List<HighlightEntry<SolrItem>> entryList = page.getHighlighted();
        //循环高亮入口
        for (HighlightEntry<SolrItem> entry : entryList) {

            SolrItem item = entry.getEntity();
            //获取高亮列表(高亮域的个数,可能对多个域进行高亮)
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();

           /* for (HighlightEntry.Highlight highlight : highlightList) {

                List<String> snipplets = highlight.getSnipplets();//每个域可能存有多值

                System.out.println(snipplets);
            }*/

            //由于我们已经确定我们只需要对一个域进行高亮且每个域只有一个值,所以我们直接取集合的第一个值即可
            if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {

                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        map.put("totalPages",page.getTotalPages());//总页数
        map.put("total",page.getTotalElements());//总记录数
        return map;
    }
}
