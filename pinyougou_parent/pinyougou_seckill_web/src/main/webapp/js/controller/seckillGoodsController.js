/**
 * 秒杀商品前端控制层
 */
 app.controller("seckillGoodsController",function ($scope,$location,$interval,seckillGoodsService) {

     //查询所有正在参与秒杀的商品列表

     $scope.findList=function () {

         seckillGoodsService.findList().success(function (response) {

             $scope.list=response;
         });
     }

     //查询正在参与秒杀的商品

     $scope.findOne=function () {
        var id= $location.search()['id'];
         seckillGoodsService.findOneFromRedis(id).success(function (response) {

             $scope.entity=response;

             //计算当前时间到结束时间的总秒数
             secondAll=Math.floor((new Date(response.endTime).getTime()-new Date().getTime())/1000);
             timer= $interval(function () {
                 secondAll=secondAll-1;

                 if (secondAll==0){
                     $interval.cancel(timer)
                     alert("秒杀活动已结束!")
                 }else {
                     $scope.timeString=convertTimeString(secondAll);
                 }
             },1000);
         });
     }

     ////把秒转换为 天小时分钟秒格式  XXX天 10:22:33
     convertTimeString=function (secondAll) {
         var days=Math.floor(secondAll/(60*60*24));//天数
         var hours=Math.floor((secondAll-days*60*60*24)/(60*60));//小时数
         var minutes=Math.floor((secondAll-days*60*60*24-hours*60*60)/60);//分钟
         var seconds=secondAll-days*60*60*24-hours*60*60-minutes*60;//秒
         var timeString="";
         if (days>0){
            timeString=days+"天"
         }
         return timeString+hours+":"+minutes+":"+seconds
     }

     //提交订单
     $scope.submitOrder=function () {

         seckillGoodsService.submitOrder( $scope.entity.id).success(function (response) {

             if (response.success){
                 alert("恭喜你!抢购成功,请在5分钟内支付完成!")
                 location.href="pay.html";
             } else {

                 alert(response.message);
             }
         });
     }
 })