$(function () {
    let oTable = new TableInit();
    oTable.Init();

    /**
     * ajax封装
     * url 发送请求的地址
     * data 发送到服务器的数据，数组存储，如：{"date": new Date().getTime(), "state": 1}
     * dataType 预期服务器返回的数据类型，常用的如：xml、html、json、text
     * successfn 成功回调函数
     * errorfn 失败回调函数
     */
    $.axspost=function(url, data, successfn, errorfn) {
        data = (data==null || data==="" || typeof(data)==="undefined")? {data: ""} : data;
        $.ajax({
            type: "post",
            data: data,
            url: url,
            dataType: "json",
            success: function(d){
                successfn(d);
            },
            error: function(e){
                errorfn(e);
            }
        });
    };

});

let showDevices = function(){
    var deviceId = $(" #deviceId ").val();
    var data = {"data":deviceId};
    $.axspost("/monitor/getDeviceEvenFromDruid",data,function (d) {
        let jsonData = eval(d);

        let columns = [{checkbox:true}];

        columns.push({field:'deviceId',title:"设备编号",align: 'center'});
        columns.push({field:'serverId',title:'服务器编号',align: 'center'});
        columns.push({field:'event',title:"事件",align: 'center'});
        columns.push({field:'__time',title:"时间",align: 'center'});


        $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
    },function () {

    })
};

let TableInit = function(){
    let oTableInit = {};
    oTableInit.Init = function(){
        $('#table').bootstrapTable({
            pagination: true,                   //是否显示分页（*）
            queryParams: oTableInit.queryParams,//传递参数（*）
            sidePagination: "client",           //分页方式：client客户端分页，server服务端分页（*）
            pageNumber: 1,                       //初始化加载第一页，默认第一页
            pageSize: 10,                       //每页的记录行数（*）
            pageList: [10, 25, 50, 100],        //可供选择的每页的行数（*）
            clickToSelect:true,
            showColumns: true,                  //是否显示刷新按钮
            showExport: true,                     //是否显示导出
            exportDataType: "selected",              //basic', 'all', 'selected'.

            columns:[],
        });
    };
    return oTableInit;
};



