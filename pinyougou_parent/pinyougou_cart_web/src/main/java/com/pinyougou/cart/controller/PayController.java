package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信扫描支付控制类
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //到redis中查询缓存的日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(username);
        if (payLog != null) {//判端日志是否存在

            return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee().toString());
        } else {
            return new HashMap();
        }

    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no 商户订单号
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true) {
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
            if (map == null) {//出错

                result = new Result(false, "支付失败!");
                break;
            }

            if (map.get("trade_state").equals("SUCCESS")) {//表示支付成功
                result = new Result(true, "支付成功!");
                //调用修改订单状态的方法
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
                break;
            }

            try {
                Thread.sleep(3000);//暂停三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //如果用户到了二维码页面一直未支付，或是关掉了支付页面，我们的代码会一直循环调用微信接口，这样会对程序造成很大的压力。
            // 所以我们要加一个时间限制或是循环次数限制，当超过时间或次数时，跳出循环。
            x++;
            if (x == 100) {//设置时间大概为5分钟
                result = new Result(false, "支付超时");
                break;
            }
        }

        return result;
    }
}
