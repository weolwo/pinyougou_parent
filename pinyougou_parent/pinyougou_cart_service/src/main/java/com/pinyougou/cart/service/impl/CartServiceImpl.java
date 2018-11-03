package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service(timeout = 10000)
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据SKUID查询商品明细SKU对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("该商品不存在!");
        }
        //有可能时间差,商品刚好被商家下架
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("该商品已失效!");
        }
        //2.根据SKU对象获取商家id
        String sellerId = item.getSellerId();
        //3.根据商家id在购物车中查询购物车对象(是否存在该商家的购物车)
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1创建一个新的购物车
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2将新购物车项添加到购物车列表中
            cartList.add(cart);
        } else {

            //5.如果购物车中存在该商家的购物车
            //5.1判断该商品是否在购物车明细中存在
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {
                //5.2如果不存在,创建新的购物车明细对象,并添加到购物车明细列表中
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.3如果存在,在原有的数量上添加数量,并更新金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));

                //如果购物明细列表中购物明细对象数量为零,则移除该购物明细对象
                if (orderItem.getNum() <= 0) {

                    cart.getOrderItemList().remove(orderItem);
                }

                //如果购物车中,购物明细列表中没有购物明细对象,则移除该购物明细列表
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }


        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String key) {
        System.out.println("从redis中提取购物车数据");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(key);
        if (cartList==null){
            return new ArrayList<>();
        }else {

            return cartList;
        }
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("项redis中存入购物车数据");
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
       // cartList1.addAll(cartList2);不能使用这种合并方式,会导致大量重复数据的出现
     
        //遍历其中任何一个购物车
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                List<Cart> cartList = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }

        }
        return cartList1;
    }

    /**
     * 根据商家id查询购物车
     *
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {

        //遍历购物车列表
        for (Cart cart : cartList) {
            //由于商家id为字符串类型的数据
            if (cart.getSellerId().equals(sellerId)) {

                return cart;
            }
        }
        return null;
    }

    /**
     * 查询购物明细列表中是否存在某商品
     *
     * @param orderItemList 物明细列表
     * @param itemId        SKUID
     * @return
     */
    public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {

        for (TbOrderItem orderItem : orderItemList) {
            //只有两个基本数据类型才能用==好比较是否相等
            if (orderItem.getItemId().longValue() == itemId.longValue()) {

                return orderItem;
            }
        }

        return null;
    }

    /**
     * 创建购物车明细对象
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("非法操作!");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }
}
