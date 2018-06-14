
//全局变量地址
//var url = "http://47.93.21.73:18080";
var url = "http://127.0.0.1:18080";

$(function () {

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

    getTotalNumPie();
    getOffDevCountByProject();
    getOffGtwCountByProject();
    getNodeDataByProject();
    getReadFailedByProject();
    getTotalCountByProject();
});



//getOffDevCountByProject获取离线设备个数order by project
var getOffDevCountByProject = function () {
    var data;
    $.axspost("/visual/getOffDevCountByProject",data,function (d) {
        var obj = eval(d);
        var columns = [];
        columns.push({
            field: 'myProject',
            title: "离线设备",
            align: 'center'
        });
        columns.push({
            field: 'myCount',
            title: "个数",
            align: 'center'
        });
        $('#tableOffDevCount').bootstrapTable({
            //加载数据
            columns: columns,
            data:obj,
            //双击事件,跳转到详情页面:row = {myCount: "4", myProject: "王店"}带参数调用页面
            onDblClickRow:function f(row) {
                var project = row.myProject;
                window.open(url+"/offlinetable.html?"+project);
            }
        });
    });
};

//获取离线网关个数order by project
var getOffGtwCountByProject = function () {
    var data;
    $.axspost("/visual/getOffGtwCountByProject",data,function (d) {
        var obj = eval(d);
        var columns = [];
        columns.push({
            field: 'myProject',
            title: "离线网关",
            align: 'center'
        });
        columns.push({
            field: 'myCount',
            title: "个数",
            align: 'center'
        });
        $('#tableOffGtwCount').bootstrapTable({
            //加载数据
            columns: columns,
            data:obj,
            //双击事件,跳转到详情页面:row = {myCount: "4", myProject: "王店"}带参数调用页面
            onDblClickRow:function f(row) {
                var project = row.myProject;
                window.open(url+"/offlinetable.html?"+project);
            }
        });
    });
};

//24小时无数据设备group by project
var getNodeDataByProject = function () {
    var data;
    $.axspost("/visual/getNodeDataByProject",data,function (d) {
        var obj = eval(d);
        var columns = [];
        columns.push({
            field: 'myProject',
            title: "一天内无数据",
            align: 'center'
        });
        columns.push({
            field: 'myCount',
            title: "个数",
            align: 'center'
        });
        $('#tableNodeData').bootstrapTable({
            //加载数据
            columns: columns,
            data:obj,
            //双击事件,跳转到详情页面:row = {myCount: "4", myProject: "王店"}带参数调用页面
            onDblClickRow:function f(row) {
                var project = row.myProject;
                window.open(url+"/nonedata.html?"+project);

            }
        });
    });
};

//抄表失败group by project
var getReadFailedByProject = function () {
    var data;
    $.axspost("/visual/getReadFailedByProject",data,function (d) {
        var obj = eval(d);
        var columns = [];
        columns.push({
            field: 'myProject',
            title: "抄表失败",
            align: 'center'
        });
        columns.push({
            field: 'myCount',
            title: "个数",
            align: 'center'
        });
        $('#tableReadFailed').bootstrapTable({
            //加载数据
            columns: columns,
            data:obj,
            //双击事件,跳转到详情页面:row = {myCount: "4", myProject: "王店"}带参数调用页面
            onDblClickRow:function f(row) {
                var project = row.myProject;
                window.open(url+"/readfailed.html?"+project);
            }
        });
    });
};
//设备总数group by project
var getTotalCountByProject = function () {
    var data;
    $.axspost("/visual/getTotalCountByProject",data,function (d) {
        var obj = eval(d);
        var columns = [];
        columns.push({
            field: 'myProject',
            title: "各项目设备分布",
            align: 'center'
        });
        columns.push({
            field: 'myCount',
            title: "个数",
            align: 'center'
        });
        $('#tableTotalCount').bootstrapTable({
            //加载数据
            columns: columns,
            data:obj
        });
    });
};


/**
 * ===============================
 * 查询设备总数饼图，实现跳转功能

 */

 var getTotalNumPie = function () {
    var data;
    $.axspost("/visual/getTotalNum",data,function(d){
        var obj = eval(d);
        var option = {


            series: [
                //中心
                {
                    type:'pie',
                    radius: [0, '25%'],
                    //设置饼图位置
                    // 禁用饼状图悬浮动画效果
                    animation: false,
                    //设置背景颜色
                    color: 'white',
                    //设置显示内容
                    label: {
                        normal: {
                            show:true,
                            position: 'center',
                            color:'#000000',
                            textStyle:{
                                fontSize:'25',
                                fontWeight:'bold'
                            }
                        }
                    },
                    data:[
                        {value:obj.totalCount,   name:'设备总数 \r\n'+obj.totalCount}
                    ]
                },
                //内圈
                {
                    type:'pie',
                    // 禁用饼状图悬浮动画效果
                    animation: false,
                    radius: ['35%', '82%'],
                    //设置背景颜色
                    color: ['#CAFF70','#9ACD32'],
                    label : {
                        //字体调整，b项目名，c值
                        normal : {
                            position: 'inner',
                            formatter: '{b}\r\n{c}',
                            textStyle : {
                                fontWeight : 'bold',
                                color:'#000000',
                                fontSize : 16
                            }
                        }
                    },
                    data:[
                        {value:obj.offDeviceCount, name:'离线设备'},
                        {value:obj.offGatewayCount, name:'离线网关'}
                    ]
                },
                //外圈
                {
                    type:'pie',
                    // 第一个参数是内圆半径，第二个参数是外圆半径，相对饼图的宿主div大小
                    radius: ['83%', '86%'],
                    avoidLabelOverlap: false,
                    // 禁用饼状图悬浮动画效果
                    animation: false,
                    //设置背景颜色
                    color: ['#BDBDBD','#D9D9D9'],
                    label : {
                        //字体调整，b项目名，c值，换行
                        normal : {
                            formatter: '{b}\n{c}',
                            textStyle : {
                                fontWeight : 'bold',
                                color:'#000000',
                                fontSize : 16
                            }
                        }
                    },
                    data:[
                         {value:obj.readFaileCount, name:'抄表失败'},
                        {value:obj.noneDataCount, name:'24小时无数据'}
                    ]
                }
            ]

        };
        var myChart = echarts.init(document.getElementById('totalNumPie'));
        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
        //点击事件
        myChart.on('click', function (param){
            var name=param.name;
            if(name=='离线设备'){
                window.open(url+"/offlinetable.html");
            }else if(name=='离线网关'){
                window.open(url+"/offlinetable.html");
            }else if(name=="抄表失败"){
                window.open(url+"/readfailed.html");
            }else if(name=="24小时无数据"){
                window.open(url+"/readfailed.html");
            }
        });
    });
};


