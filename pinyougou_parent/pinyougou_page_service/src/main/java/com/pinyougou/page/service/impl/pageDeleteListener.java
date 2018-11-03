package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * 监听删除静态商品详情页,消费者类
 */
@Component
public class pageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage= (ObjectMessage) message;
        System.out.println("接收到消息");
        try {
            Long[] ids= (Long[]) objectMessage.getObject();
            boolean b = itemPageService.deleteGenItemHtml(ids);
            System.out.println("删除静态商品详情页:"+b);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
