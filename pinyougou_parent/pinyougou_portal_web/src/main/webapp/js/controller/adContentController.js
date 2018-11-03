
////广告内容管理前端控制层
app.controller("adContentController",function ($scope,adContentService) {

    $scope.contentList=[];

    $scope.findByCategoryId=function (categoryId) {

        adContentService.findByCategoryId(categoryId).success(function (data) {

            $scope.contentList[categoryId]=data;
        })
    }

    //搜索跳转
    $scope.search=function () {

        window.location.href="http://localhost:8084/search.html#?keywords="+$scope.keywords;
    }
})