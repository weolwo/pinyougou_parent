package com.sms.listener;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.sms.utils.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信发送监听器
 */
@Component
public class SmsListener {

    @Autowired
    private SmsUtil smsUtil;

    @JmsListener(destination = "sms")
    public void sendSms(Map<String,String> map){

        try {
            SendSmsResponse smsResponse = smsUtil.sendSms(map.get("mobile"), map.get("sign_name"), map.get("template_code"), map.get("param"));
            System.out.println("Code:"+smsResponse.getCode());
            System.out.println("Message"+smsResponse.getMessage());
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
