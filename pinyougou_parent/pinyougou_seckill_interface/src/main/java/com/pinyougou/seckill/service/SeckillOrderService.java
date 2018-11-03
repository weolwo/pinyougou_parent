package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillOrder;
import entity.PageResult;

import java.util.List;

/**
 * 业务逻辑接口
 * @author Steven
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);


	/**
	 * 秒杀下单
	 * @param seckillId 秒杀商品id
	 * @param userId 登录用户
	 */
	public  void submitOrder(Long seckillId,String userId);

	/**
	 * 查询用户订单
	 * @param userId 用户名
	 * @return
	 */
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId);

	/**
	 * 支付成功保存订单
	 * @param userId 登录用户
	 * @param orderId 订单id,用于验证订单
	 * @param transactionId 微信返回的支付流水号
	 */
	public void saveOrderFromRedisToDb(String userId,Long orderId,String transactionId);

	/**
	 * 当用户下单后5分钟尚未付款应该释放订单，增加库存
	 * @param userId 登录用户
	 * @param seckillId 订单号
	 */
	public void deleteOrderFromRedis(String userId,Long seckillId);
}
