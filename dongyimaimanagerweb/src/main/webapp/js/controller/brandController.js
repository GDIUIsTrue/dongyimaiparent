app.controller('brandController',function ($scope,$controller,brandService) {
    $controller('baseController',{$scope:$scope});

    $scope.findAll=function () {
        brandService.findAll().success(
            function (response) {
                $scope.list=response;
            }
        )
    }

    //分页控制配件
    /*$scope.paginationConf={
        currentPage:1,
        totalItems:10,
        itemsPerPage:10,
        perPageOptions:[10,20,30,40,50],
        onChange:function () {
            $scope.reloadList();//重新加载
        }
    }*/
    //根据条件分页查询
    $scope.searchEntity={};//定义搜索对象
    $scope.search=function(page,rows){
        brandService.search($scope.searchEntity,page,rows).success(
            function (response) {
                $scope.paginationConf.totalItems=response.total;
                $scope.list=response.rows;
            }
        )
    }
    //重新加载列表数据
    /*$scope.reloadList=function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }
*/
    //分页
    /*$scope.findPage=function (page,rows) {
        $http.get('../brand/findPage.do?page='+page+'&rows='+rows).success(
                function (response) {
                    $scope.list=response.rows;
                    $scope.paginationConf.totalItems=response.total;//更新总记录数
                }
        )
    }*/
    //保存品牌
    $scope.save=function () {
        var methodName="add";//方法名称
        if($scope.entity.id!=null){//如果有ID，则执行修改方法
            methodName="update";
        }
        $http.post('../brand/'+methodName+'.do',$scope.entity).success(
            function (response) {
                if (response.success){
                    $scope.reloadList();
                }else {
                    alert(response.message);
                }
            }
        )
    }
    //查询实体
    $scope.findOne=function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    }
    //批量删除
/*    $scope.selectIds=[];*/
    //更新复选
   /* $scope.updateSelection=function ($event,id) {
        if($event.target.checked){//如果是被选中，则增加到数组
            $scope.selectIds.push(id);
        }else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(id,1);//删除
        }
    }*/
    $scope.delete=function () {
        //获取选中的复选框
        brandService.delete($scope.selectIds).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }
            }
        )
    }
})