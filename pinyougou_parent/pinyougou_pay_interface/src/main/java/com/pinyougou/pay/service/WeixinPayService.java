package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 微信扫描支付接口类
 */
public interface WeixinPayService {

    /**
     * 生成二维码
     * @param out_trade_no 商户订单号
     * @param total_fee 总金额
     * @return 返回支付二维码连接及其他参数
     */
    public Map createNative(String out_trade_no,String total_fee);

    /**
     * 查询支付状态
     * @param out_trade_no 商户订单号
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 关闭订单
     * @param out_trade_no 商户订单号
     * @return
     */
    public Map closePay(String out_trade_no);
}
