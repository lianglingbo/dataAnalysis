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

    getTotalNum();
});


//查询设备总数
var getTotalNum = function () {
    var data;
    $.axspost("/visual/getTotalNum",data,function(d){
        var obj = eval(d);
        var myChart= echarts.init(document.getElementById('totalNum'));
          var option = {
            //option选项
            xAxis: {
                type: 'category',
                data: ['总数','离线设备', '离线网关', '抄表失败', '二十四小时无数据']
            },
            yAxis: {
                type: 'value'
            },
            series: [{
                data: [obj.totalCount, obj.offDeviceCount, obj.offGatewayCount, obj.readFaileCount, obj.noneDataCount ],
                type: 'bar'
            }]

        };
        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
        myChart.on('click', function (param){
            var name=param.name;
            if(name=="总数"){
                alert("总数")
            }else if(name=="二十四小时无数据"){
                alert("二十四小时")
            }
        });
        myChart.on('click',eConsole);
    });

};


