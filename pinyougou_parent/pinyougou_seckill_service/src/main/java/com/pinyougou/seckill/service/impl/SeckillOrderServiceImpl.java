package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.utils.IdWorker;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insertSelective(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKeySelective(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        seckillOrderMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            //如果字段不为空
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andLike("userId", "%" + seckillOrder.getUserId() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andLike("sellerId", "%" + seckillOrder.getSellerId() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andLike("status", "%" + seckillOrder.getStatus() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andLike("receiverAddress", "%" + seckillOrder.getReceiverAddress() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andLike("receiverMobile", "%" + seckillOrder.getReceiverMobile() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andLike("receiver", "%" + seckillOrder.getReceiver() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andLike("transactionId", "%" + seckillOrder.getTransactionId() + "%");
            }

        }

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    @Override
    public void submitOrder(Long seckillId, String userId) {

        //从缓存中取出参与秒杀的商品
        TbSeckillGoods secKillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("secKillGoods").get(seckillId);
        if (secKillGoods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (secKillGoods.getStockCount() <= 0) {
            throw new RuntimeException("商品已被抢空啦");
        }

        //扣减缓存中的商品库存数
        secKillGoods.setStockCount(secKillGoods.getStockCount() - 1);
        //重新存入缓存
        redisTemplate.boundHashOps("secKillGoods").put(seckillId, secKillGoods);

        //如果库存不足
        if (secKillGoods.getStockCount() == 0) {
            //保存商品信息到数据库中
            seckillGoodsMapper.updateByPrimaryKey(secKillGoods);
            //从缓存中删除商品
            redisTemplate.boundHashOps("secKillGoods").delete(seckillId);
        }

        //保存订单信息
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setMoney(secKillGoods.getCostPrice());
        seckillOrder.setSellerId(secKillGoods.getSellerId());//商家
        seckillOrder.setSellerId(userId);
        seckillOrder.setSeckillId(seckillId);//秒杀商品ID
        seckillOrder.setStatus("0");//状态

        //保存订单到redis
        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
    }

    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        //从缓存中查询订单
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {

        //从缓存中提取订单
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder==null){

            throw new RuntimeException("订单不存在");
        }

        if (seckillOrder.getId().longValue()!=orderId.longValue()){//对象不能用==判断是否相等
            throw new RuntimeException("订单信息不匹配");
        }

        seckillOrder.setStatus("1");//支付完成
        seckillOrder.setPayTime(new Date());//支付时间
        seckillOrder.setTransactionId(transactionId);

        //保存到数据库
        seckillOrderMapper.insert(seckillOrder);
        //清楚缓存中的订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }

    @Override
    public void deleteOrderFromRedis(String userId, Long seckillId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder!=null && seckillOrder.getId().longValue()==seckillId.longValue()){

            //1.删除订单
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }

        //2.恢复库存
        //从缓存中查询该商品信息
        TbSeckillGoods secKillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("secKillGoods").get(seckillOrder.getSeckillId());

        secKillGoods.setStockCount(secKillGoods.getStockCount()+1);

        //恢复库存
        redisTemplate.boundHashOps("secKillGoods").put(seckillOrder.getSeckillId(),secKillGoods);
    }

}
