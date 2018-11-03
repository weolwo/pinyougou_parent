app.controller('payController' ,function($scope ,$location,weixinPayService){
    //本地生成二维码
    $scope.createNative=function () {

        weixinPayService.createNative().success(function (response) {

            $scope.totalMoney=(response.total_fee/100).toFixed(3);//支付金额

            $scope.out_trade_no=response.out_trade_no;//订单号

            var pr=new QRious({//生成支付二维码

                element:document.getElementById('qrious'),
                size:250,
                level:'H',
                value:response.code_url

            });

            queryPayStatus();//调用状态查询方法
        })
    }

    //查询支付状态
    queryPayStatus=function () {
        weixinPayService.queryPayStatus($scope.out_trade_no).success(function (response) {
            
            if (response.success){//支付成功
                
                location.href="paysuccess.html#?totalMoney="+$scope.totalMoney;
            } else {
                if (response.message=="支付超时"){
                    //重新生成新的二维码
                   // $scope.createNative();
                    alert("支付超时,订单无效")
                }else {

                    location.href="payfail.html";
                }
            }
        });
    }

    //在支付成功页面获取支付金额
    $scope.getMoney=function () {

        return $location.search()['totalMoney'];
    }
});
