/**
 * 购物车前端服务层
 */
app.service("cartService",function ($http) {

    //查询购物车列表
    this.findCartList=function () {

        return $http.get("../cart/findCartList.do");
    }

    //购物车数量增减与移除
    this.addGoodsToCartList=function (itemId,num) {

        return $http.get("../cart/addGoodsToCartList.do?itemId="+itemId+"&num="+num);
    }

    //实现商品总金额
    this.sum=function (cartList) {
        //定义一个总数对象
        var totalValue={totalNum:0,totalMoney:0.000};

        //循环遍历购物车列表
        for (var i=0;i<cartList.length;i++){
            var cart=cartList[i];

            //遍历购物车明细列表
            for (var j=0;j<cart.orderItemList.length;j++){
              var  orderItem=cart.orderItemList[j];

              totalValue.totalNum+=orderItem.num;
              totalValue.totalMoney+=orderItem.totalFee;
            }
        }
        return totalValue;
    }

    //查询当前登录用户的收货地址信息列表
    this.findAddressList=function () {

        return $http.get("../address/findListByLoginUser.do");
    }

    //保存订单信息
    this.submitOrder=function (order) {

        return $http.post("../order/add.do",order);
    }
})