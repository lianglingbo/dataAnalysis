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
    getWaterMeterFromDruid();

});

//查设备详情
let getDeviceInfoFromDruid = function(){
    var selectContent = $('#table').bootstrapTable('getSelections')[0];
    if(typeof(selectContent) == 'undefined') {

        return false;
    }else{
        console.info(selectContent);
        // $('#item_project_modal').modal('show');     //  面板
        $.axspost("/monitor/getDeviceInfoFromDruid",selectContent,function (d) {
            let jsonData = eval(d);
            let columns = [{checkbox:true}];
            columns.push({
                field:'deviceId',
                title:"设备编号",
                align: 'center'
            });
            columns.push({field:'currentdata',title:'当日凌晨用量',align: 'center',formatter: 'waterFormatter'});
            columns.push({field:'totaldata',title:'累计总量',align: 'center'});
            columns.push({field:'utf8time',title:'时间',align: 'center'});
            $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
        },function () {
        })


    }
    //展示折线图
    getDeviceInfoFromDruidToEchars();
};

//   按钮点击事件,查询最近7天水表，用量
let getWaterMeterFromDruid  = function () {
        $.axspost("/monitor/getWaterMeterFromDruid",null,function (d) {
            let jsonData = eval(d);
            let columns = [{checkbox:true}];
            columns.push({field:'deviceId', title:"设备编号", align: 'center'});
            columns.push({field:'maxUse',title:'当日凌晨用量',align: 'center'});
            $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
        },function () {
        })

}

//   按钮点击事件,查询最近7天水表，频率
let getWaterMeterCountFromDruid  = function () {
    $.axspost("/monitor/getWaterMeterCountFromDruid",null,function (d) {
        let jsonData = eval(d);
        let columns = [{checkbox:true}];
        columns.push({field:'deviceId', title:"设备编号", align: 'center'});
        columns.push({field:'useCount',title:'更新次数',align: 'center'});
        $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
    },function () {
    })

}

//   按钮点击事件,查询最近7天可疑用水的水表
let getExceptionWaterMeter  = function () {
        // $('#item_project_modal').modal('show');     //  面板
        $.axspost("/monitor/getExceptionWaterMeter", null, function (d) {
            let jsonData = eval(d);
            let columns = [{checkbox: true}];
            columns.push({
                field: 'deviceId',
                title: "设备编号",
                align: 'center'
            });
            columns.push({field: 'exceptCount', title: '异常次数', align: 'center'});
            $('#table').bootstrapTable("refreshOptions", {columns: columns, data: jsonData});
        }, function () {
        })


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


//格式化状态
function waterFormatter(value) {
    if (value==-1) {
        return '<span class="label label-danger">异常</span>';
    }else{
        return value;
    }
};


//加载数据到前台图表
//查设备详情
let getDeviceInfoFromDruidToEchars = function(){
    var selectContent = $('#table').bootstrapTable('getSelections')[0];
    if(typeof(selectContent) == 'undefined') {
        return false;
    }else{
        // $('#item_project_modal').modal('show');     //  面板
        $.axspost("/monitor/getDeviceInfoFromDruid",selectContent,function (d) {
            let jsonData = eval(d);
            var usedata = [];
            var time = [];
            //时间和用量单独拿出放入数组
            for(var index in jsonData){
                time.push(jsonData[index].utf8time);
                usedata.push(jsonData[index].currentdata);
            };
            option = {
                title: {
                    text: '夜间用量'
                },
                tooltip: {
                    trigger: 'axis'//鼠标跟随效果
                },
                //右上角工具条
                toolbox: {
                    show: true,
                    feature: {
                        mark: {show: true},
                        dataView: {show: true, readOnly: false},
                        magicType: {show: true, type: ['line', 'bar']}
                    }
                },
                xAxis: {
                    type: 'category',
                    data:time
                },
                yAxis: {
                    type: 'value'
                },
                series: [
                    {
                    name: '用量',
                    type: 'line', symbol: 'emptydiamond',    //设置折线图中表示每个坐标点的符号 emptycircle：空心圆；emptyrect：空心矩形；circle：实心圆；emptydiamond：菱形
                     //stack: '总量',
                    data: usedata
                }
                ]
            };
            var myChart = echarts.init(document.getElementById('lineChart'));
            myChart.setOption(option);
            //触发模态框
            $('#myModal').modal('show');
        },function () {})
    }
};

