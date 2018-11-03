//前端索索服务控制层
app.controller("searchController", function ($scope,$location, searchService) {

    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 40,
        'sortField':'',
        'sort':''
    }

    $scope.search = function () {

        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo);//转换为数字,保证传递到后端的时数字

        searchService.search($scope.searchMap).success(function (data) {

            $scope.resultMap = data;

            $scope.buildPageLabel();
        })
    }

    //增加搜索项
    $scope.addSearchItem = function (key, value) {

        if (key == 'category' || key == 'brand' || key == 'price') {//如果用户点击分类或者品牌

            $scope.searchMap[key] = value;

        } else {

            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行搜索
    }
    //撤销搜索项
    $scope.removeSearchItem = function (key) {

        if (key == 'category' || key == 'brand' || key == 'price') {

            $scope.searchMap[key] = ''; //如果用户点击分类或者品牌

        } else {

            delete  $scope.searchMap.spec[key];//移除此属性
        }

        $scope.search();
    }

    //构建分页兰
    $scope.buildPageLabel = function () {
        //构建分页属性
        $scope.pageLabel = []

        var curPage = 1;//开始页

        var lastPage = $scope.resultMap.totalPages;//末页;

        $scope.firstDot=true;

        $scope.endDot=true;

        if ($scope.resultMap.totalPages>5){//如果总页数>5

            if($scope.searchMap.pageNo<=3){//如果当前页<=3,显示前5页
                $scope.firstDot=false;
               lastPage=5;

            }else if ($scope.searchMap.pageNo>$scope.resultMap.totalPages-2){//如果当前页>总页数-2,显示后5页
                $scope.endDot=false;
                curPage=$scope.resultMap.totalPages-4;
            }else {
                //否则显示以当前页为中心的5页
                curPage=$scope.searchMap.pageNo-2;

                lastPage=$scope.searchMap.pageNo+2;

            }
        }else {//如果总页数<5
            $scope.firstDot=false;

            $scope.endDot=false;
        }

        for (var i=curPage;i<=lastPage;i++){

            //循环产生页码
            $scope.pageLabel.push(i);
        }
    }

    //根据当前页码查询
    $scope.queryByPage=function (pageNo) {

        if (pageNo>$scope.resultMap.totalPages ||pageNo<1 ){

            return;
        } else {

            $scope.searchMap.pageNo=pageNo;

            $scope.search();
        }
    }

    //判断当前页是否时第一页
    $scope.isTopPage=function () {

        if ($scope.searchMap.pageNo==1){

            return true;
        } else {

            return false;
        }
    }

    //判断当前页是否时最后一页
    $scope.isEndPage=function () {

        if ($scope.searchMap.pageNo==$scope.resultMap.totalPages){

            return true;
        } else {

            return false;
        }
    }

    //拍寻
    $scope.sortSearch=function (sortField,sort) {

        $scope.searchMap.sortField=sortField;

        $scope.searchMap.sort=sort;

        $scope.search();
    }

    //判断关键字中是否包含品牌信息
    $scope.keywordsIsBrand=function () {
        
        //遍历品牌列表
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)!=-1){//返回-1代表没有

                return true;
            }
        }
        return false;
    }

    //搜索页接收主页传过来的关键字
    $scope.loadkeywords=function () {

        $scope.searchMap.keywords=$location.search()['keywords']

        $scope.search();
    }
})