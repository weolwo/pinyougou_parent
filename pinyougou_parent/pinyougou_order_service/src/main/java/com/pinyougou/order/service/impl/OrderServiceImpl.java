package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.utils.IdWorker;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service(timeout = 10000)
//@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //引入雪花ID生成器
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbOrder> result = new PageResult<TbOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbOrder> list = orderMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加订单
     */
    @Override
    public void add(TbOrder order) {
        //1.从redis中取出购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        List<String> orderList = new ArrayList<>();//订单ID列表
        double totalMoney = 0;//总金额
        //2.循环读取购物车数据,保存订单
        for (Cart cart : cartList) {
            TbOrder tbOrder = new TbOrder();
            long orderId = idWorker.nextId();

            orderList.add(orderId + "");//添加到订单列表

            tbOrder.setOrderId(orderId);//设置订单id
            tbOrder.setPaymentType(order.getPaymentType());//支付方式
            tbOrder.setStatus("1");//订单状态 1:未付款
            tbOrder.setCreateTime(new Date());//订单生成时间
            tbOrder.setUpdateTime(new Date());//订单更新时间
            tbOrder.setUserId(order.getUserId());//用户
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收货地址
            tbOrder.setReceiver(order.getReceiver());//收货人
            tbOrder.setReceiverMobile(order.getReceiverMobile());//收货人电话
            tbOrder.setSourceType(order.getSourceType());//订单来源
            tbOrder.setSellerId(cart.getSellerId());//商家

            //循环购物明细列表,保存订单明细信息
            for (TbOrderItem orderItem : cart.getOrderItemList()) {

                orderItem.setId(idWorker.nextId());
                orderItem.setSellerId(cart.getSellerId());
                orderItem.setOrderId(orderId);
                totalMoney += orderItem.getTotalFee().doubleValue();
                orderItemMapper.insertSelective(orderItem);
            }

            tbOrder.setPayment(new BigDecimal(totalMoney));//实付金额
            orderMapper.insertSelective(tbOrder);
        }

        //生成支付日志
        if (order.getPaymentType().equals("1")) {//微信支付
            TbPayLog payLog = new TbPayLog();
            payLog.setCreateTime(new Date());//日志生成时间
            payLog.setOutTradeNo(idWorker.nextId() + "");//微信订单支付号
            payLog.setPayType("1");//支付类型
            payLog.setTradeState("1");//交易状态,未支付

            String orderIds = orderList.toString().replace("[", "").replace("]", "").replace(" ", "");

            payLog.setOrderList(orderIds);//订单编号列表,数据库中以逗号分隔
            payLog.setTotalFee((long) (totalMoney * 100));//支付金额（分）
            payLog.setUserId(order.getUserId());
            payLogMapper.insert(payLog);

            //存入到redis中
            redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
        }
        //3.清楚rdis中的数据
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        orderMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageResult<TbOrder> result = new PageResult<TbOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if (order != null) {
            //如果字段不为空
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andLike("paymentType", "%" + order.getPaymentType() + "%");
            }
            //如果字段不为空
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andLike("postFee", "%" + order.getPostFee() + "%");
            }
            //如果字段不为空
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andLike("status", "%" + order.getStatus() + "%");
            }
            //如果字段不为空
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andLike("shippingName", "%" + order.getShippingName() + "%");
            }
            //如果字段不为空
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andLike("shippingCode", "%" + order.getShippingCode() + "%");
            }
            //如果字段不为空
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andLike("userId", "%" + order.getUserId() + "%");
            }
            //如果字段不为空
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andLike("buyerMessage", "%" + order.getBuyerMessage() + "%");
            }
            //如果字段不为空
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andLike("buyerNick", "%" + order.getBuyerNick() + "%");
            }
            //如果字段不为空
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andLike("buyerRate", "%" + order.getBuyerRate() + "%");
            }
            //如果字段不为空
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andLike("receiverAreaName", "%" + order.getReceiverAreaName() + "%");
            }
            //如果字段不为空
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andLike("receiverMobile", "%" + order.getReceiverMobile() + "%");
            }
            //如果字段不为空
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andLike("receiverZipCode", "%" + order.getReceiverZipCode() + "%");
            }
            //如果字段不为空
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andLike("receiver", "%" + order.getReceiver() + "%");
            }
            //如果字段不为空
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andLike("invoiceType", "%" + order.getInvoiceType() + "%");
            }
            //如果字段不为空
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andLike("sourceType", "%" + order.getSourceType() + "%");
            }
            //如果字段不为空
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andLike("sellerId", "%" + order.getSellerId() + "%");
            }

        }

        //查询数据
        List<TbOrder> list = orderMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    @Override
    public TbPayLog searchPayLogFromRedis(String username) {

        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(username);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //1. 修改支付日志状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);//根据主键查询TbPayLog
        if (payLog != null) {
            payLog.setPayTime(new Date());//支付成功时间
            payLog.setTradeState("1");//交易状态,交易成功
            payLog.setTransactionId(transaction_id);//微信支付成功流水号

            //跟新日志
            payLogMapper.updateByPrimaryKey(payLog);
        }
        // 2. 修改关联的订单的状态
        String orderList = payLog.getOrderList();//获取订单列表
        String[] orderIds = orderList.split(",");//获取订单数组
        for (String orderId : orderIds) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
            if (tbOrder!=null){
                tbOrder.setStatus("2");//已支付
                tbOrder.setPaymentTime(new Date());//付款时间
                orderMapper.updateByPrimaryKey(tbOrder);
            }
        }
        // 3. 清除缓存中的支付日志对象
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }

}
