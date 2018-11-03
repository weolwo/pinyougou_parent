package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务层接口
 */
public interface CartService {


    /**
     * 添加商品到购物车
     * @param cartList 购物车列表
     * @param itemId  SKUID
     * @param num  数量
     * @return 购物车列表
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);


    /**
     * 从redis中提取购物车
     * @param key key
     * @return
     */
    public List<Cart> findCartListFromRedis(String key);


    /**
     * 把购物车存入redis中
     * @param username 当前登录系统的用户
     * @param cartList 购物车列表
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList1 购物车1
     * @param cartList2 购物车2
     * @return 返回合并后的购物车
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
