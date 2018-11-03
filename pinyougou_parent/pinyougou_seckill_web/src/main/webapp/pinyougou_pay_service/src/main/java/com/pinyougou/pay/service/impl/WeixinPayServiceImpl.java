package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.utils.HttpClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信扫描支付实现类
 */
@Service(timeout = 10000)
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${notifyurl}")
    private String notifyurl;

    @Value("${partnerkey}")
    private String partnerkey;


    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        Map map = new HashMap();
        try {
            //1、包装微信接口需要的参数
            Map param = new HashMap();
            param.put("appid", appid);  //公众号ID
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
            param.put("body", "品优购");  //商品描述，扫码后用户看到的商品信息
            param.put("out_trade_no", out_trade_no); //订单号
            param.put("total_fee", total_fee);  //订单总金额，单位为分
            param.put("spbill_create_ip", "127.0.0.1");  //终端IP，只要附合ip地址规范，可以随意写
            param.put("notify_url", notifyurl);  //回调地址
            param.put("trade_type", "NATIVE");  //交易类型，NATIVE 扫码支付
            //2、生成xml，通过httpClient发送请求得到数据
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数:" + xmlParam);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //3、解析结果
            String xmlResult = httpClient.getContent();
            System.out.println("微信返回结果：" + xmlResult);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no", out_trade_no);//订单号
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {

        //1.封装信接口需要的参数
        Map param=new HashMap();
        param.put("appid", appid);  //公众号ID
        param.put("mch_id", partner);  //商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
        param.put("out_trade_no", out_trade_no); //订单号

        //2.生成xml发送请求
        try {
            String  xmlParam= WXPayUtil.generateSignedXml(param,partnerkey);
            System.out.println("微信发送数据:"+xmlParam);
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.返回数据,解析数据
            String resultXML = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXML);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

    }

}
