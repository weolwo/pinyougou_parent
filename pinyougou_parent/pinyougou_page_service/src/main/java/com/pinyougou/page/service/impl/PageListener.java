package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * 订阅模式之消费者类
 */
@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        //强转从消息队列中获取的数据
        ObjectMessage objectMessage= (ObjectMessage) message;
        try {
            Long[] ids = (Long[]) objectMessage.getObject();
            for (Long id:ids){
                boolean b = itemPageService.genItemHtml(id);
                System.out.println("生成结果:"+b);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
