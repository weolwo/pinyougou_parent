/**
 * 购物车模块前端控制层
 */
app.controller("cartController", function ($scope, cartService) {

    //查询购物车列表信息
    $scope.findCartList = function () {

        cartService.findCartList().success(function (response) {

            $scope.cartList = response;

            $scope.totalValue = cartService.sum($scope.cartList);
        })
    }

    //购物车数量增减与移除
    $scope.addGoodsToCartList = function (itemId, num) {

        cartService.addGoodsToCartList(itemId, num).success(function (response) {

            if (response.success == true) {
                //刷新列表
                $scope.findCartList();
            } else {
                alert(response.message);
            }
        })
    }

    //查询当前登录用户的收货地址信息列表
    $scope.findAddressList = function () {

        cartService.findAddressList().success(function (response) {

            $scope.addressList = response;

            //地址默认被选中
            for (var i=0;i<$scope.addressList.length;i++){

                if ($scope.addressList[i].isDefault=='1'){

                    $scope.address=$scope.addressList[i];//取出是默认的那个地址并赋值给$scope.address

                    break;
                }
            } 
        })
    }

    //选择地址
    $scope.selectAddress = function (address) {

        $scope.address = address;
    }

    //判断是否是当前选中的地址
    $scope.isSelectedAddress = function (address) {

        if (address == $scope.address) {
            return true;
        } else {
            return false;
        }
    }

    //定义一个订单对象
    $scope.order={paymentType:'1'}

    //选择支付方式
    $scope.selectPayType=function (type) {

        $scope.order.paymentType=type;
    }

    //保存订单信息
    $scope.submitOrder=function () {

        //封装order
        $scope.order.receiverAreaName=$scope.address.address;//收货地址
        $scope.order.receiverMobile=$scope.address.mobile;//收货人电话
        $scope.order.receiver=$scope.address.contact;//收货人
        cartService.submitOrder($scope.order).success(function (response) {

            if (response.success){//如果订单保存成功
                if ($scope.order.paymentType=='1'){//如果挺好选择的是微信支付

                    location.href="pay.html";
                } else {
                    location.href="paysuccess.html";
                }
            } else {
                alert(response.message);//也可以跳转到一个页面
            }
        })
    }
});