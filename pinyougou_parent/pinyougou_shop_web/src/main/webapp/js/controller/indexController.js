
//登录控制层
app.controller("indexController",function ($scope,loginService) {

    //读取登录人的信息
    $scope.showLoginName=function () {

        loginService.loginName().success(function (data) {

            $scope.loginName=data.loginName;
        });
    }
})