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
    $.axspost("/monitor/getUsageStatusFailed",data,function (d) {
        let jsonData = eval(d);
        let sort = [];
        let columns = [{checkbox:true}];
        for (let x in jsonData){
            sort.push(jsonData[x].project||jsonData[x].province||jsonData[x].city||jsonData[x].district||jsonData[x].community);
        }
        let html = "<option value=\"\"></option>";
        $.each(sort,function (idx,item) {
        	html +="<option value=\"\">" + item+"</option>";
        });
        selects.eq(index).empty();
        selects.eq(index).append(html);
        //更新表格
        columns.push({field:fileds[index],title:titles[index]});
        columns.push({field:'failed',title:'可疑水表个数'});
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
    data.data = JSON.stringify(params);
    $.axspost("/monitor/getUsageWithProjectByParams",data,function (d) {
        let jsonData = eval(d);
        let columns = [{checkbox:true}];
        columns.push({field:'project',title:"项目",align: 'center',valign : 'middle'});
        columns.push({field:'province',title:"省",align: 'center',valign : 'middle'});
        columns.push({field:'city',title:"市",align: 'center',valign : 'middle'});
        columns.push({field:'district',title:"区",align: 'center',valign : 'middle'});
        columns.push({field:'community',title:"小区",align: 'center',valign : 'middle'});
        columns.push({field:'address',title:"地址",align: 'center',valign : 'middle'});
        columns.push({field:'deviceId',title:"设备编号",align: 'center',valign : 'middle'});
        columns.push({field:'usagepic',title:'用量图', width:550  ,hight:70,events:operateEvents,align: 'center',formatter:AddPicFunction,valign : 'middle'});
        $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
        //将数据发给绘图方法
        getPic(jsonData);
    },function () {
    })
};
//测试绘图方法
let  getPic = function (jsonData) {

    if(jsonData!=null){
        //获取id，循环遍历
        $.each(jsonData,function (index,obj) {
            var deviceId = obj.deviceId;
            var v1 = obj.one;
            var v2 = obj.two;
            var v3 = obj.three;
            var v4 = obj.four;
            var v5 = obj.five;
            var v6 = obj.six;
            var divid = "#"+deviceId;
            //模拟触发div的点击事件，触发events的绘图方法
            $(divid).click();

        });
    }

}
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
            showColumns: false,
            search:true,
            showExport: false,                     //是否显示导出
            clickToSelect: false,                //是否启用点击选中行
            exportDataType: "selected",              //basic', 'all', 'selected'.
            rowStyle: function (row, index) {
                //这里有5个取值代表5中颜色['active', 'success', 'info', 'warning', 'danger'];
                let strclass = "";
                if (row.failed >0 && row.failed <=30) {
                    strclass = 'warning';
                }else if (row.failed >30) {
                    strclass = 'danger';
                }
                else {
                	return {};
                }
                return { classes: strclass }
            },
            columns:[],
        });
    };
    return oTableInit;
};

//表格中增加折线图容器
function AddPicFunction(value,row,index) {
    var deviceId = row.deviceId;
    return[
        '<div class="classOfDiv" id="'+deviceId+'" style="width: 540px;height:90px;" ></div> '
    ].join("");
}

//添加事件
window.operateEvents = {
    "click .classOfDiv":function (e,value,row,index) {
        var v1 = row.one;
        var v2 = row.two;
        var v3 = row.three;
        var v4 = row.four;
        var v5 = row.five;
        var v6 = row.six;
        var deviceId = row.deviceId;
        //折线图配置
        option = {
            tooltip: {
                trigger: 'axis' //鼠标跟随效果
            },
            xAxis: {
                show:false,
                type: 'category',
                splitLine:{show: false},//去除网格线
                axisTick:{show:false},   //x// 轴刻度线
                axisLine:{show:false},   //x轴
                data: ['1','2','3','4','5','6']
            },
            yAxis: {
                show:false,
                type: 'value',
                splitLine:{show: false},//去除网格线
                axisTick:{show:false},   //y轴刻度线
                axisLine:{show:false}   //y轴
            },
            series: [{
                type: 'line',
                symbol: 'emptydiamond',
                //设置折线图中表示每个坐标点的符号 emptycircle：空心圆；emptyrect：空心矩形；circle：实心圆；emptydiamond：菱形
                data: [v1,v2,v3,v4,v5,v6]
            }]
        };
        var myChart = echarts.init(document.getElementById(deviceId));
        myChart.setOption(option);
    }
};

//选中
var selects = $('#table').bootstrapTable('getSelections');