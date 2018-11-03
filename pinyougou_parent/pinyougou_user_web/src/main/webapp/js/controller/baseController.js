//创建品牌控制器
app.controller("baseController",function ($scope) {
    //分页标签属性
    $scope.paginationConf = {
        //当前页
        currentPage: 1,
        //总记录数
        totalItems: 10,
        //每页查询的记录数
        itemsPerPage: 10,
        //分页选项，用于选择每页显示多少条记录
        perPageOptions: [10, 20, 30, 40, 50],
        //当页码变更后触发的函数
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };

    //刷新页面内容
    $scope.reloadList=function () {
        //$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);

        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage,$scope.searchEntity);
    }

    //记录要删除的id列表
    $scope.selectIds=[];

    //选中checkbox，记录删除的id列表
    $scope.updateSelection=function ($event,id) {
        //判断checkbox有没有选中
        if($event.target.checked){
            $scope.selectIds.push(id);
        }else {
            //查询入参id的下标，用于接下来的删除数组使用
            var index = $scope.selectIds.indexOf(id);
            //参数一：删除的下标，参数二：从下标开始删除多少个元素
            $scope.selectIds.splice(index,1)
        }
    }

    //跟据需求输出json串
    //jsonString要转换的json串,key要读取的值
    $scope.jsonToString=function (jsonString,key) {
        var json = JSON.parse(jsonString);
        var result = "";
        for(var i = 0;i < json.length; i++){
            if(i > 0){
                result += ",";
            }
            result += json[i][key];
        }
        return result;
    }

});