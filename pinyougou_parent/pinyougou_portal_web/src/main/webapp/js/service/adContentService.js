
//广告内容管理前端服务层
app.service("adContentService",function ($http) {

    //根据id查询所有广告列表
    this.findByCategoryId=function (categoryId) {

        return $http.get("../content/findByCategoryId.do?categoryId="+categoryId)
    }

})