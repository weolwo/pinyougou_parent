package com.pinyougou.page.service.impl;

import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${PAGE_HTML_DIR}")
    private String PAGE_HTML_DIR;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {

        boolean flag = true;
        //获取FreeMarkerConfigurer配置对象
        Configuration configurer = freeMarkerConfigurer.getConfiguration();

        try {
            //获取模板
            Template template = configurer.getTemplate("item.ftl");

            //创建数据模型
            Map dataModel = new HashMap();

            //查询商品信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);

            //查询商品描述信息
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);

            //查询商品分类名称
            String category1Id = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String category2Id = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String category3Id = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

            dataModel.put("category1Id", category1Id);
            dataModel.put("category2Id", category2Id);
            dataModel.put("category3Id", category3Id);

            //查询SKU数据
            Example example = new Example(TbItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("goodsId", goodsId);//指定spu id
            criteria.andEqualTo("status", "1");//只查询状态正常的商品
            example.setOrderByClause("is_default desc");////按默认降序，为了第一个选中默认的,前端方表取数据
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);

            //模板生成目标位置
            Writer out = new FileWriter(new File(PAGE_HTML_DIR + goodsId + ".html"));
            template.process(dataModel, out);
            out.close();//关闭输出流
        } catch (Exception e) {
            e.printStackTrace();

            flag = false;
        }

        return flag;
    }

    @Override
    public boolean deleteGenItemHtml(Long[] goodsIds) {

        try {
            for (Long goodsId : goodsIds) {

                new File(PAGE_HTML_DIR + goodsId + ".html").delete();

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
