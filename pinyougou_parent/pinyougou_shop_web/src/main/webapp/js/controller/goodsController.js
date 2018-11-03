//控制层
app.controller('goodsController', function ($scope, $controller,$location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //上传图片
    $scope.image_entity = {url: ""}

    $scope.uploadFile = function () {

        uploadService.uploadFile().success(function (response) {

            //如果上传成功,绑定url到表单
            if (response.success) {
                $scope.image_entity.url = response.message;
            } else {
                alert(response.message)
            }
        }).error(function () {
            alert("上传图片异常!")
        });
    }

    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};//定义页面实体结构

    //上传图片列表
    $scope.add_image_entity = function () {

        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //移除图片
    $scope.remove_image_entity = function (index) {

        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        //由于我们的修改页面和列表信息页面不是同一个页面,所以需要使用到angularjs对象$location为我们传递商品id
       var id= $location.search()['id'];

       if (id==null){
           //如果id为空就没必要和后端交互,(新增的时候id为空)
           return;
       }
        goodsService.findOne(id).success(

            function (response) {

                $scope.entity = response;
                //像富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //显示图片列表 , 将图片列表由字符串转换为json集合对象
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);

                //显示扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems)

                //显示商品规格
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems)

                //SKU列表转换.遍历拿到里面的spec
                for (var i=0;i<$scope.entity.itemList.length;i++){

                    $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec)
                }
            }
        );
    }

    //根据规格名称和选项名称返回是否被勾选[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]}]
    $scope.checkAttributeValue=function(specName,optionName){
        //先通过方法寻找规格数据,如果能找到attributeName对应的值,再进一步判断attributeValue的值,如果attributeName都不能匹配到,则返回false
      var items= $scope.entity.goodsDesc.specificationItems;

       var object= $scope.searchObjectByKey(items,'attributeName',specName);

       if (object!=null){
            
           if (object.attributeValue.indexOf(optionName)>=0){

               return true;
           } else {

               return false;
           }
           
        }else {
           
           return false;
       }
        return true;
    }

    //保存
    $scope.save = function () {

        var serviceObject;//服务层对象

        //早保存之前获取富文本中的内容
        $scope.entity.goodsDesc.introduction = editor.html();

        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                   //保存成功后跳转到商品列表
                    location.href="goods.html";
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询一级分类
    $scope.selectItemCat1List = function () {

        itemCatService.findByParentId(0).success(function (response) {

            $scope.itemCat1List = response;
        });
    }
    //跟据一级类目，更新二级类目
    //$watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {

        itemCatService.findByParentId(newValue).success(function (response) {

            $scope.itemCat2List = response;
        })
    })

    //跟据二级类目，更新三级类目
    //$watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {

        itemCatService.findByParentId(newValue).success(function (response) {

            $scope.itemCat3List = response;
        })
    })
//选择三级类目后，更新模板id
//$watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {

        itemCatService.findOne(newValue).success(function (response) {

            $scope.entity.goods.typeTemplateId = response.typeId;
        })
    })

    //跟据模板id，更新品牌列表
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {

        typeTemplateService.findOne(newValue).success(function (response) {

            $scope.typeTemplate = response;
            $scope.typeTemplate.brandIds = JSON.parse(response.brandIds)//json.parse()将字符串转成json对象。
            //在用户更新模板ID时，读取模板中的扩展属性赋给商品的扩展属性
            if ($location.search()['id']==null){//如果id等于null,就是增加商品,其他时候不执行,否则会覆盖其他方法的值

                $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems)
            }
            //alert($scope.typeTemplate.brandIds)
            typeTemplateService.findSpecList(newValue).success(function (response) {

                $scope.specList = response;
            })
        })
    })

    //勾选选择框时调用此函数,此处有两种情种
    //1,选择的选项,规格名称已经存在,则只需在attributeValue里面添加选择则值
    //2,选择的选项,规格名称不存在,就创建一个新的数据模型,
    //数据模型:[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]}]
    $scope.updateSpecAttribute = function ($event, name, value) {

        //判断选择的规格名称是否已经存在
        var obj = this.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);

        //根据返回值的不同处理
        if (obj != null) {
            //如果已选中
            //点击单选框是否选中,选中就添加到数组中
            if ($event.target.checked) {

                obj.attributeValue.push(value);
            } else {
                //已取消,则把相应的数据移除数组
                obj.attributeValue.splice(obj.attributeValue.indexOf(value), 1);
                //如果当attributeValue没有值时,则移除整个集合
                if (obj.attributeValue.length == 0) {

                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(obj), 1)
                }
            }
        } else {
            //如果没有选中,添加一条记录
            $scope.entity.goodsDesc.specificationItems.push({'attributeName': name, 'attributeValue': [value]});
        }
    }

    //创建SKU列表
    $scope.createItemList = function () {

        //创建初始数据模型
        $scope.entity.itemList = [{spec: {}, price: 0, num: 9999, status: '0', isDefault: '0'}];

        var items = $scope.entity.goodsDesc.specificationItems;

        for (var i = 0; i < items.length; i++) {

            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
        }
    }

    //添加列值
    addColumn=function (list,columnName,columnValus) {

        var newList=[];

        for (var i=0;i<list.length;i++){

            var oldRow=list[i];

            for (var j=0;j<columnValus.length;j++){

               var  newRow=JSON.parse(JSON.stringify(oldRow));//对象的深度克隆

                newRow.spec[columnName]=columnValus[j];

                newList.push(newRow);
            }
        }

        return newList;
    }

    //定义一个变量,由于该数组下标和我们的状态代码刚好一致.
    $scope.status=['未审核','已审核','审核未通过','已关闭'];

    //使用数组封装分类信息,已分类id作为下标,
    $scope.itemCatList=[];
    
    $scope.findItemCatList=function () {

        itemCatService.findAll().success(function (data) {

            //遍历返回的分类数据
            for (var i=0;i<data.length;i++){

                $scope.itemCatList[data[i].id]=data[i].name;
            }
        })
    }

});	
