package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * 请求处理器
 *
 * @author Steven
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 5000)//远程调用
    private GoodsService goodsService;

//    @Reference(timeout = 10000)
//    private ItemPageService itemPageService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination queueSolrDestination;

    @Autowired
    private Destination queueSolrDeleteDestination;

    @Autowired
    private Destination topicPageDestination;

    @Autowired
    private Destination topicPageDeleteDestination;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }


    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);
            //itemSearchService.deleteByGoodsIds(ids);

            //删除索引
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {

                    //由于Long的父类Number实现了序列化,直接传一个对象过去
                    return session.createObjectMessage(ids);
                }
            });

            //删除商品详情页静态网页
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {

                    //由于Long的父类Number实现了序列化,直接传一个对象过去
                    return session.createObjectMessage(ids);
                }
            });
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

//    @Reference(timeout = 10000)
//    private ItemSearchService itemSearchService;

    /**
     * 商品审核
     *
     * @param ids    商品id数组
     * @param status 状态值
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(final Long[] ids, String status) {


        try {
            goodsService.updateStatus(ids, status);

            //如果审核通过
            if ("1".equals(status)) {
                //查询sku列表
                List<TbItem> items = goodsService.findItemListByGoodsIdandStatus(ids, status);
                if (items != null && items.size() > 0) {
                  /*   List<SolrItem> solrItems = new ArrayList<>();
                    for (TbItem item : items) {
                        SolrItem solrItem = new SolrItem();
                        //全用深克隆复制TbItem对象所有属性到SolrItem对象中
                        BeanUtils.copyProperties(item, solrItem);
                        //把数据查询出来的json字符串转成map
                        Map map = JSON.parseObject(item.getSpec());
                        //设置动态域内容
                        solrItem.setSpecMap(map);
                        solrItems.add(solrItem);
                    }*/
                    //调用方法保存到索引库
                    //itemSearchService.importList(solrItems);

                    //把SKU列表数据转换成json字符串
                    final String jsonString = JSON.toJSONString(items);
                    //使用jms模板发送json字符串数据到mq
                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {

                            return session.createTextMessage(jsonString);
                        }
                    });
                }

                //生成商品静态页面
//                for (Long id : ids) {
//
//                    //itemPageService.genItemHtml(id);
//                }
                jmsTemplate.send(topicPageDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(ids);
                    }
                });

            }

            return new Result(true, "操作成功!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Result(true, "操作失败!");
    }



    /*   *//**
     * 生成页面测试
     * @param goodsId
     *//*
    @RequestMapping("genItemHtml")
    public void genItemHtml(Long goodsId){
        itemPageService.genItemHtml(goodsId);
    }*/
}
