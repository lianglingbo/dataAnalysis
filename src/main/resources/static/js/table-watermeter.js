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
    showDevices();

});

let showDevices = function(){
    var deviceId = $(" #deviceId ").val();
    //加单引号解决out of range 的问题
    var tempdate = "\'" +deviceId+"\'";
    var data = {"data":tempdate};
    $.axspost("/monitor/getWaterMeterFromDruid",data,function (d) {
        let jsonData = eval(d);

        let columns = [{checkbox:true}];

        columns.push({
            field:'deviceId',
            title:"设备编号",
            align: 'center'

        });
        columns.push({field:'maxUse',title:'用量',align: 'center'});



        $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
    },function () {

    })
};

//   按钮点击事件
let getDeviceId  = function () {
    var selectContent = $('#table').bootstrapTable('getSelections')[0];
    if(typeof(selectContent) == 'undefined') {
        selectContent=null;
        showDevices();
        return false;
    }else{
        console.info(selectContent);

        // $('#item_project_modal').modal('show');     // 项目立项面板
        $.axspost("/monitor/getWaterMeterFromDruid",selectContent,function (d) {
            let jsonData = eval(d);

            let columns = [{checkbox:true}];

            columns.push({
                field:'deviceId',
                title:"设备编号",
                align: 'center'

            });
            columns.push({field:'currentdata',title:'当日用量',align: 'center'});
            columns.push({field:'totaldata',title:'总量',align: 'center'});
            columns.push({field:'utf8time',title:'时间',align: 'center'});


            $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
        },function () {

        })


    }
}

let TableInit = function(){
    let oTableInit = {};
    oTableInit.Init = function(){
        $('#table').bootstrapTable({
            search: true,                     // 搜索框

            singleSelect: true,              // 单选checkbox
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



