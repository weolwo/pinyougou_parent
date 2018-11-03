app.controller("baseController",function ($scope) {
    //分页控件属性配置
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
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };

    //重新加载数据
    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //选中的id列表
    $scope.selectIds = [];

    //复选更新选中列表
    $scope.updateSelection = function ($event, id) {
        //如果是被选中,则增加到数组
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            //查找当前id的下标
            var idx = $scope.selectIds.indexOf(id);
            //删除数据
            $scope.selectIds.splice(idx, 1);
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

    //判断一个集合中某个属性名称是否已经存在,如果存在就返回,否则返回空
    /**
     *
     * @param list 搜索的列表
     * @param key 搜索的key
     * @param keyValue 对比的值
     */
    $scope.searchObjectByKey=function (list,key,keyValue) {

        //遍历集合
        for (var i=0;i<list.length;i++){

            if (list[i][key]==keyValue){

                return list[i];//如果找到相应的key,返回该集合
            }
        }
        //没有找到则返回null
        return null;
    }
})