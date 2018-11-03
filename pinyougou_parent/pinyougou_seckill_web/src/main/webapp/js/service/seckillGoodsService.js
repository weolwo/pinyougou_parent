/**
 * 秒杀商品前端服务层
 */

app.service("seckillGoodsService",function ($http) {

    //查询参与秒杀的商品列表
    this.findList=function () {

        return $http.get("../seckillGoods/findList.do");
    }

    //查询商品
    this.findOneFromRedis=function (id) {

        return $http.get("../seckillGoods/findOneFromRedis.do?id="+id);
    }

    //提交订单
    this.submitOrder=function (seckillId) {

        return $http.get("../seckillOrder/submitOrder.do?seckillId="+seckillId);
    }
})