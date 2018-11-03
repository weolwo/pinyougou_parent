//定义模块
var app=angular.module("pinyougou",[]);

//定义一个html信任过滤器
/*$sce服务写成过滤器*/
app.filter("trustHtml",['$sce',function ($sce) {//相当于一个全局方法

    //data 传入参数时,被转换的内容
    return function (data) {

        return $sce.trustAsHtml(data)//转换后的内容
    }
}]);

