package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
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
    private SeckillOrderService seckillOrderService;

    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //到redis中查询缓存的订单
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
        if (seckillOrder != null) {//判端订单是否存在
            //可能有小数,所以把它转成long
            return weixinPayService.createNative(seckillOrder.getId() + "", (long) (seckillOrder.getMoney().doubleValue() * 100) + "");
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
        //获取登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
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
                seckillOrderService.saveOrderFromRedisToDb(username, new Long(out_trade_no), map.get("transaction_id"));
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
            if (x >= 100) {//设置时间大概为5分钟
                result = new Result(false, "支付超时");
                //调用微信接口关闭订单,由于可能用户在时间差时支付成功,所以需要做一下判断
                Map<String, String> resultMap = weixinPayService.closePay(out_trade_no);
                if (resultMap != null && "FAIL".equals(resultMap.get("result_code"))) {

                    if ("ORDERPAID".equals(resultMap.get("err_code"))) {
                        //用户已支付,当作已支付的正常交易
                        result = new Result(true, "支付成功!");
                        //调用修改订单状态的方法
                        seckillOrderService.saveOrderFromRedisToDb(username, new Long(out_trade_no), map.get("transaction_id"));
                    }
                }

                if (result.isSuccess() == false) {
                    //删除库存
                    seckillOrderService.deleteOrderFromRedis(username, new Long(out_trade_no));
                }
                break;
            }
        }

        return result;
    }
}
