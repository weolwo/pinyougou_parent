package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * activeMQ消费者监听类
 */
@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;


    @Override
    public void onMessage(Message message) {

        System.out.println("监听到啦");
        try {
            TextMessage textMessage = (TextMessage) message;

            String text = textMessage.getText();
            //将获取到的SKUjson数据转换成list类型数据
            List<TbItem> items = JSON.parseArray(text, TbItem.class);
            if (items != null && items.size() > 0) {
                List<SolrItem> solrItems = new ArrayList<>();
                for (TbItem item : items) {
                    SolrItem solrItem = new SolrItem();
                    //全用深克隆复制TbItem对象所有属性到SolrItem对象中
                    BeanUtils.copyProperties(item, solrItem);
                    //把数据查询出来的json字符串转成map
                    Map map = JSON.parseObject(item.getSpec());
                    //设置动态域内容
                    solrItem.setSpecMap(map);
                    solrItems.add(solrItem);
                }
                //调用方法保存到索引库
                itemSearchService.importList(solrItems);
                System.out.println("导入solr成功!");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
