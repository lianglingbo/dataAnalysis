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
//清空按钮
let clearAll = function () {

    $("#event").val('');
    $("#deviceId").val('');
    $(" #datetime1 ").val('');
    $(" #datetime2 ").val('');

}

let showDevices = function(){
    //获取参数
    var deviceId = $(" #deviceId ").val();
    //获取下拉框的值
    var event = $(" #event ").val();
    var datetime1 = $(" #datetime1 ").val();
    var datetime2 = $(" #datetime2 ").val();

    //加单引号解决out of range 的问题
    //deviceId = "\'" +deviceId+"\'";

    var infos = {
        "deviceId":deviceId,
        "event":event,
        "datetime1":datetime1,
        "datetime2":datetime2,
    };
    if(datetime2 < datetime1 && datetime2.length != 0){
        alert("时间选择错误");
        return;
    }
    var data = {};
    data.data = JSON.stringify(infos);

    $.axspost("/monitor/getDeviceEvenFromDruid",data,function (d) {
        let jsonData = eval(d);

        let columns = [{checkbox:true}];

        columns.push({field:'deviceId',title:"设备编号",align: 'center'});
        columns.push({field:'serverId',title:'服务器编号',align: 'center'});
        columns.push({field:'event',title:"事件",align: 'center'});
        columns.push({field:'eventinfo',title:"事件信息",align: 'center', formatter: 'base64Formatter'});
        columns.push({field:'data',title:"数据",align: 'center'});
        columns.push({field:'utf8time',title:"时间",align: 'center'});


        $('#table').bootstrapTable("refreshOptions",{columns:columns,data:jsonData});
    },function () {

    })
};

//解密base64
function base64Formatter(value) {
    if (value != null) {
        //解密,95D1AEA8,27565
        var base64code = value;
        var base = new Base64();
        var result = base.decode(base64code);
        return  result
    }else{
        return ""
    }

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
            clickToSelect:false,
            showColumns: false,                  //是否显示刷新按钮
            showExport: false,                     //是否显示导出
            exportDataType: "selected",              //basic', 'all', 'selected'.

            columns:[],
        });
    };
    return oTableInit;
};

//base64 解密
function Base64() {

    // private property
    _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    // public method for encoding
    this.encode = function (input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;
        input = _utf8_encode(input);
        while (i < input.length) {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);
            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;
            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }
            output = output +
                _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
                _keyStr.charAt(enc3) + _keyStr.charAt(enc4);
        }
        return output;
    }

    // public method for decoding
    this.decode = function (input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
        while (i < input.length) {
            enc1 = _keyStr.indexOf(input.charAt(i++));
            enc2 = _keyStr.indexOf(input.charAt(i++));
            enc3 = _keyStr.indexOf(input.charAt(i++));
            enc4 = _keyStr.indexOf(input.charAt(i++));
            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;
            output = output + String.fromCharCode(chr1);
            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }
        }
        output = _utf8_decode(output);
        return output;
    }

    // private method for UTF-8 encoding
    _utf8_encode = function (string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";
        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);
            if (c < 128) {
                utftext += String.fromCharCode(c);
            } else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            } else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }
        return utftext;
    }

    // private method for UTF-8 decoding
    _utf8_decode = function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;
        while ( i < utftext.length ) {
            c = utftext.charCodeAt(i);
            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            } else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            } else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }
        return string;
    }
}

