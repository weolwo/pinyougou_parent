app.controller("itemController",function ($scope,$http) {

    $scope.specificationItems={};//记录用户选择的规格

    $scope.addNum=function (x) {

        $scope.num+=x;

        if ($scope.num<1){

            $scope.num=1;
        }
    }

    //存储用户选择规格
   $scope.selectSpecification=function (key,value) {

       $scope.specificationItems[key]=value;
	   
	   $scope.searchSku();

   }

    //判断某个规格是否被用户选择
    $scope.isSelected=function (key,value) {

        if ($scope.specificationItems[key]==value){

            return true;
        }else {

            return false;
        }
    }
    //加载默认的SKU信息
    $scope.loadSku=function () {

        $scope.sku=skuList[0];

        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));//此处使用圣科隆
    }

    //匹配两个对象是否相等
    $scope.matchObject=function (map1,map2) {

        for (var key in map1) {
            if (map1[key]!=map2[key]){

                return false;
            }
        }

        for (var key in map2) {
            if (map2[key]!=map1[key]){

                return false;
            }
        }

        return true;
    }

    //查询SKU
    $scope.searchSku=function () {


       for (var i=0;i<skuList.length;i++){
           if( $scope.matchObject(skuList[i].spec,$scope.specificationItems)){
               //如果返回true
               $scope.sku=skuList[i];

               return;
           }
       } 

       
        $scope.sku={id:0,title:'--------',price:0};//如果没有匹配的
    }
	
	  //加入购物车
    $scope.addToCart=function () {

       /* alert("sku-id"+$scope.sku.id);*/
        $http.get("http://localhost:8088/cart/addGoodsToCartList.do?itemId="+$scope.sku.id+"&num="+$scope.num,{'withCredentials':true}).success(function (response) {
            if (response.success){
                //跳转到购物车页面
                location.href='http://localhost:8088/cart.html';

            } else {
                alert(response.message);
            }
        });
    }


})