
//登录业务逻辑
app.service("loginService",function ($http) {

    //读取登录名
    this.loginName=function () {

        return $http.get("../login/name.do");
    }
})