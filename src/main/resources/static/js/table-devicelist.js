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
    var data = {};
    var args = 'readFailedDevice';
    data.data=args;
    $.axspost("/visual/getDeviceInfosByArgs",data,function (d) {
        let jsonData = eval(d);
        let columns = [];

        columns.push({field:'project',title:"项目",align: 'center'});
        columns.push({field:'province',title:"省",align: 'center'});
        columns.push({field:'city',title:"市",align: 'center'});
        columns.push({field:'district',title:"区",align: 'center'});
        columns.push({field:'community',title:"小区",align: 'center'});
        columns.push({field:'address',title:"地址",align: 'center'});
        columns.push({field:'lastUpdate',title:'最后更新时间',align: 'center'});
        columns.push({field:'diffTime',title:'时差(h)',align: 'center'});
        columns.push({field:'deviceId',title:"设备编号",align: 'center'});
        columns.push({field:'gatewayId',title:'网关编号',align: 'center'});
        columns.push({field:'address',title:"地址",align: 'center'});
        columns.push({field:'deviceState',title:'设备状态',align: 'center',formatter: 'deviceStatusFormatter'});
        columns.push({field:'readState',title:'抄表状态',align: 'center',formatter: 'readStatusFormatter'});
        columns.push({field:'readFaile',title:'抄表失败次数',align: 'center'});


        $('#table').bootstrapTable({
            search:true,                        //搜索框
            pagination: true,                   //是否显示分页（*）
            queryParams: oTableInit.queryParams,//传递参数（*）
            sidePagination: "client",           //分页方式：client客户端分页，server服务端分页（*）
            pageNumber: 1,                       //初始化加载第一页，默认第一页
            pageSize: 10,                       //每页的记录行数（*）
            pageList: [10, 25, 50, 100],        //可供选择的每页的行数（*）
            clickToSelect:true,
            exportDataType: "selected",              //basic', 'all', 'selected'.

            columns:[],
        });

    },function () {

    })
};
//全局变量使用一次,同一界面，再次触发方法无效
var url = location.search; //获取url中"?"符后的字串
//获取url中的参数，赋值给select下拉框
var selectProject = function () {
    //获取url的参数，自动选择项目
     if (url != null && url != '' && url.indexOf("?") != -1) {
        //中文解码，得到项目名
        var getProject = decodeURI(url.substr(1));
        //销毁url
         url=null;
        //获取下拉框元素
        var selectElement = document.getElementById("input_project");
        //遍历下拉框options，将项目选中
        for(i=0;i<selectElement.length;i++){//给select赋值
            if(getProject==selectElement.options[i].text){
                selectElement.options[i].selected=true;
                //触发onchange事件
                $('#input_project').trigger('change');
            }
        }
    }
};
let TableInit = function(){
    let oTableInit = {};
    oTableInit.Init = function(){

    };
    return oTableInit;
};


//格式化状态
function readStatusFormatter(value) {
    if (value === "1") {
        return '<span class="label label-danger">失败</span>';
    } else if(value === "0"){
        return '<span class="label label-success">成功</span>';
    }
};
function valveStatusFormatter(value) {
    if (value === "1") {
        return '<span class="label label-success">开</span>';
    } else if(value === "0"){
        return '<span class="label label-default">关</span>';
    }
};
function deviceStatusFormatter(value) {
    if (value === "1") {
        return '<span class="label label-success">在线</span>';
    } else if(value === "0"){
        return '<span class="label label-default">离线</span>';
    }
};
function simStatusFormatter(value) {
    if (value === "1") {
        return '<span class="label label-warning">欠费</span>';
    } else if(value === "0"){
        return '<span class="label label-info">正常</span>';
    }
};


