package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询所有已经审核的商品信息
 */
@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入商品数据
     */
    public void importItemData(){

        TbItem where =new TbItem();
        where.setStatus("1");
        List<TbItem> itemList = tbItemMapper.select(where);

        //创建solritem
        List<SolrItem> solrItems=new ArrayList<>();
        SolrItem solrItem=null;
        System.out.println("商品列表开始");
        for (TbItem item : itemList) {
            System.out.println(item.getTitle()+"  "+item.getBrand()+"  "+item.getPrice());
            solrItem=new SolrItem();
            //使用spring的beanUtil工具类深克隆对象
            BeanUtils.copyProperties(item,solrItem);
            //将spec中的json字符串转成json对象
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            solrItem.setSpecMap(map);
            solrItems.add(solrItem);
        }
        //保存到solr
        solrTemplate.saveBeans(solrItems);
        //提交事务
        solrTemplate.commit();
        System.out.println("商品列表结束");
    }

    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItemData();

    }
}
