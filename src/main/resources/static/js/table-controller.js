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
    initSelects();
});

let initSelects = function (e) {
    let selects = $("select"),
        fileds = ['project','province','city','district','community'],
        titles = ['项目','省','城市','地区','小区'],
        params = {},
        data = {};

    let index = 0;
    //获取当前选择的下拉框编号
    $.each(selects,function (idx,item) {
        if(e===item){
            if(selects.eq(idx).find("option:selected").text()===""){
                index = idx;
            }else {
                index = idx+1;
            }
        }
    });
    //更新下拉框状态和内容
    $.each(selects,function (idx,item) {

        if(idx < index){
            params[fileds[idx]] = selects.eq(idx).find("option:selected").text();
            selects.eq(idx).attr("disabled",false);
        }else if(idx === index){
            params[fileds[idx]] = "";
            selects.eq(idx).empty();
            selects.eq(idx).attr("disabled",false);
        }else {
            params[fileds[idx]] = "";
            selects.eq(idx).empty();
            selects.eq(idx).attr("disabled",true);
        }
    });

    data.data = JSON.stringify(params);
    //获取数据
    $.axspost("/monitor/getOffline",data,function (d) {
        let jsonData = eval(d);
        let sort = [];
        let columns = [];
        for (let x in jsonData){
            sort.push(jsonData[x].project||jsonData[x].province||jsonData[x].city||jsonData[x].district||jsonData[x].community);
        }
        let html = "<option value=\"\"></option>";
        $.each(sort,function (idx,item) {
        	html +="<option value=\"\">" + item+"</option>";
        });
        selects.eq(index).empty();
        selects.eq(index).append(html);
        //获取url参数，选择项目
        selectProject();
        //展示设备详情，并将下拉框所选的项目传给后台做筛选条件
        showDevicesList($("#input_project option:selected").text());//拿到选中项的文本
        //更新表格
        columns.push({field:fileds[index],title:titles[index]});
        columns.push({field:'offline',title:'离线设备数量'});
        $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
    },function () {
    })
};

let showDevices = function(){
    let selects = $("select"),
        fileds = ['project','province','city','district','community'],
        params = {},
        data = {};

    $.each(selects,function (idx,item) {
            params[fileds[idx]] = selects.eq(idx).find("option:selected").text();
    });
    params.deviceState = 0;
    data.data = JSON.stringify(params);
    $.axspost("/monitor/getDeviceByParams",data,function (d) {
        let jsonData = eval(d);
        let columns = [];

        columns.push({field:'deviceId',title:"设备编号",align: 'center'});
        columns.push({field:'gatewayId',title:'网关编号',align: 'center'});
        columns.push({field:'simId',title:"SIM卡ID",align: 'center'});
        columns.push({field:'address',title:"地址",align: 'center'});
        columns.push({field:'readState',title:'抄表状态',align: 'center',formatter: 'readStatusFormatter'});
        columns.push({field:'deviceState',title:'设备状态',align: 'center',formatter: 'deviceStatusFormatter'});
        columns.push({field:'valveState',title:'阀门状态',align: 'center',formatter: 'valveStatusFormatter'});
        columns.push({field:'simState',title:'SIM卡状态',align: 'center',formatter: 'simStatusFormatter'});
        columns.push({field:'dataUsed',title:'已使用的流量',align: 'center'});
        columns.push({field:'readFaile',title:'抄表失败次数',align: 'center'});

        $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
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
                //触发onchange事件,已经自动触发了不需要
                $('#input_project').trigger('change');
            }
        }
    }
};
//展示列表详情
let showDevicesList = function(projectTemp){
    var data = {};
    var args = 'offlineDevice';
    if(projectTemp!=null && projectTemp.length!=0 && projectTemp!=''){
        args = args + ','+ projectTemp;
    }
    data.data=args;
    $.axspost("/visual/getDeviceInfosByArgs",data,function (d) {
        let obj = eval(d);
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

        $('#table_list').bootstrapTable("refreshOptions",{columns:columns,data:obj});

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
            exportDataType: "all",              //basic', 'all', 'selected'.
            rowStyle: function (row, index) {
                //这里有5个取值代表5中颜色['active', 'success', 'info', 'warning', 'danger'];
                let strclass = "";
                if (row.offline >0 && row.offline <=30) {
                    strclass = 'warning';
                }else if (row.offline >30) {
                    strclass = 'danger';
                }
                else {
                	return {};
                }
                return { classes: strclass }
            },
            columns:[]
        });
        $('#table_list').bootstrapTable({
            search:true,                        //搜索框
            pagination: true,                   //是否显示分页（*）
            sidePagination: "client",           //分页方式：client客户端分页，server服务端分页（*）
            pageNumber: 1,                       //初始化加载第一页，默认第一页
            pageSize: 10,                       //每页的记录行数（*）
            pageList: [10, 25, 50, 100],        //可供选择的每页的行数（*）
            clickToSelect:true,
            exportDataType: "all",              //basic', 'all', 'selected'.
            showExport: true,                     //是否显示导出
            columns:[]
        });
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


