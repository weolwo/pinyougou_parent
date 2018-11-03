
//登录控制层
app.controller("indexController",function ($scope,loginService) {

    $scope.showLoginName=function () {

        loginService.loginName().success(function (data) {

            $scope.loginName=data.loginName;
        })
    }
})