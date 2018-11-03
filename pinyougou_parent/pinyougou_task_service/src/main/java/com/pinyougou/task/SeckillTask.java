package com.pinyougou.task;

import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 秒杀任务定时调度
 */
@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 刷新秒杀商品
     */
    @Scheduled(cron = "0/10 * * * * ?")//每分钟执行一次增量添加
    public void refreshSeckillGoods() {
        System.out.println("任务调度开始了...." + new Date());
        //从缓存中取出所有的key,SET集合里面的元素是不重复的赚LIST是比较容易的
        List seckillGoodsIds = new ArrayList(redisTemplate.boundHashOps("secKillGoods").keys());
        System.out.println(seckillGoodsIds);
        //到数据库中查询
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1");//审核通过的商品
        criteria.andLessThanOrEqualTo("startTime", new Date());//开始时间小于等于当前时间
        criteria.andGreaterThanOrEqualTo("endTime", new Date());//结束时间大于等于当前时间
        criteria.andGreaterThan("stockCount", 0);//剩余库存数必需大于0

        if (seckillGoodsIds.size() > 0) {//否则当seckillGoodsIds.size==0表示缓存中没有数据,需要将所有色

            criteria.andNotIn("id", seckillGoodsIds);//排除缓存中已有的商品
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //把数据村如缓存中
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            //以商品id作为键,以商品对象作为值存入缓存中
            redisTemplate.boundHashOps("secKillGoods").put(seckillGoods.getId(), seckillGoods);
        }
        System.out.println("将" + seckillGoodsList.size() + "数据存入缓存");
    }


    /**
     * 移除过期的秒杀商品
     */
    @Scheduled(cron = "* * * * * ?")//每秒钟执行一次
    public void removeSeckillGoods(){
        System.out.println("移除国企的商品"+new Date());
        //扫描缓存中的每一条商品数据,如果发现截至时间小于当前时间的商品,就存缓存中移除
        //从redis中读取数据
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("secKillGoods").values();
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            if (seckillGoods.getEndTime().getTime()<new Date().getTime()){//截至时间小于当前时间
                //保存到数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);

                //从缓存中清楚过期的商品
                redisTemplate.boundHashOps("secKillGoods").delete(seckillGoods.getId());
                System.out.println("秒杀商品****:"+seckillGoods.getId()+"已过期---");
            }
        }

        System.out.println("清楚结束-----end");
    }
}
