//控制层
app.controller('goodsController' ,function($scope,$controller,itemCatService,goodsService,$http){

    $controller('baseController',{$scope:$scope});//继承

    //读取列表数据绑定到表单中
    $scope.findAll=function(){
        goodsService.findAll().success(
            function(response){
                $scope.list=response;
            }
        );
    }

    //分页
    $scope.findPage=function(page,rows){
        goodsService.findPage(page,rows).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne=function(id){
        goodsService.findOne(id).success(
            function(response){
                $scope.entity= response;
            }
        );
    }

    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.entity.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.reloadList();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.del=function(){
        $http.get('../goods/delete.do?ids='+$scope.selectIds).success(
            function (response) {
                if (response.success){
                    $scope.reloadList();
                    $scope.selectIds=[];
                }
            }
        )
    };

    $scope.searchEntity={};//定义搜索对象

    //搜索
    $scope.search=function(page,rows){
        goodsService.search(page,rows,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    $scope.status=['未审核','申请通过','审核驳回','已关闭'];//商品状态
    $scope.itemCatList=[];//商品分类列表
    //加载商品分类列表
    $scope.findItemCatList=function () {
        itemCatService.findAll().success(
            function (response) {
                for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
                }
            }
        )
    }
    //更改状态
    $scope.updateStatus=function (status) {
        $http.get('../goods/updateStatus.do?ids='+$scope.selectIds+'&status='+status).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];//清空ID集合
                }else {
                    alert(response,message);
                }
            })
    }

})