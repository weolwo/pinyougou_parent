package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.utils.CookieUtil;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 购物车控制层类
 */
@RestController
@RequestMapping("cart")
public class cartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 6000)
    private CartService cartService;


    /**
     * 获取购物车列表
     *
     * @return
     */
    @RequestMapping("findCartList")
    public List<Cart> findCartList() {
        //获取当前登录的用户
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人:" + name);

        //从cookie中获取购物车
        String cookieValue = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (!StringUtils.isNotBlank(cookieValue)) {

             cookieValue="[]";
        }
        //把字符串转换成list
        List<Cart> cartList_coookie = JSON.parseArray(cookieValue, Cart.class);

        if (name.equals("anonymousUser")) {

            return cartList_coookie;

        } else {
            //从Redis中提取购物车数据
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
            //合并购物车
            if (cartList_coookie.size() > 0) {//判断本地购物车中存在数据,为了避免每次都去执行下面这段代码
                List<Cart> cartList = cartService.mergeCartList(cartList_coookie, cartList_redis);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(name, cartList);

                //清空本地购物车
                CookieUtil.deleteCookie(request, response, "cartList");

                return cartList;
            }
            //如果本地购物车不存在,返回redis中的购物车
            return cartList_redis;
        }
    }

    /**
     * 添加商品到购物车
     *
     * @param itemId SKUID
     * @param num    数量
     * @return
     */
    @RequestMapping("addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:8085",allowCredentials = "true")//allowCredentials="true"  可以缺省
    public Result addGoodsToCartList(Long itemId, Integer num) {
//        //设置可以访问的域，值设置为*时，允许所有域
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8085");
//        //如果需要操作cookies，必须加上此配置，标识服务端可以写cookies，
//        // 并且Access-Control-Allow-Origin不能设置为*，因为cookies操作需要域名
//        response.setHeader("Access-Control-Allow-Credentials", "true");

        //获取当前登录的用户
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("anonymousUser".equals(name)) {//如果用户当前未登录
                //将购物车列表存入cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {//当前用户已登录
                cartService.saveCartListToRedis(name, cartList);
            }

            return new Result(true, "加入购物车成功!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "加入购物车失败!");
    }
}
