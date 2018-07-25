var testData = [];
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
            async:false,
            dataType: "json",
            success: function(d){
                successfn(d);
            },
            error: function(e){
                errorfn(e);
            }
        });
    };
    $.axspost("/visual/getDevDistribution","",function (d) {
        testData = d ;
    });
});








var img = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAD4AAAA+CAYAAABzwahEAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NjQzNjQwQjA3NTM4MTFFOEE5MjVEM0IwMjBENkU4RUYiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6NjQzNjQwQjE3NTM4MTFFOEE5MjVEM0IwMjBENkU4RUYiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo2NDM2NDBBRTc1MzgxMUU4QTkyNUQzQjAyMEQ2RThFRiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo2NDM2NDBBRjc1MzgxMUU4QTkyNUQzQjAyMEQ2RThFRiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PqxKGNcAABFSSURBVHja1FtbcBxl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';

function GDMap(params){
    this.params = params;
    this.container = (params.container ? params.container : 'container');
    this.adcode = (params.adcode ? params.adcode : 100000);

    this.map;
    this.mapData;
    this.colorScale;

    return this.initAMap();
}

GDMap.prototype = {

    constructor: GDMap,

    createColorScale: function(){
        var max = d3.max(this.mapData, function(d){ return d.value});
        var min = d3.min(this.mapData, function(d){ return d.value});


        if(max == min) min = min - 1000;
        this.colorScale = d3.scaleLinear().domain([min, max]).range(["#f2f2f2", "#383838"]);
    },

    initAMap: function(){
        var _this = this;

        if(this.map) {
            this.drawOverLay();
        }else{
            var opts = {
                resizeEnable: true,
                isHotspot: false,
                zoomEnable: false,
                zoom: 5,
                center: [104.114129, 34.550339],
                mapStyle: 'amap://styles/2afaefd00ede02c6f6e5ebef5c92c0cc'
            };
            //初始化高德地图
            this.map = new AMap.Map(this.container, opts);

            //加载DistrictExplorer
            AMapUI.loadUI(['geo/DistrictExplorer'], function(DistrictExplorer) {

                //初始化DistrictExplorer
                var districtExplorer = window.districtExplorer = new DistrictExplorer({
                    eventSupport: true,
                    map: _this.map
                });


                districtExplorer.on('featureClick', function(e, feature) {
                    _this.featureClick(feature);
                });


                districtExplorer.on('outsideClick', function(e) {
                    if(_this.adcode != 100000){
                        var acroutes = currentAreaNode.getProps().acroutes;
                        var acrLength = acroutes.length;
                        _this.adcode = acrLength > 1 ? acroutes[1] : acroutes[0];
                        _this.drawOverLay();
                    }
                });

                districtExplorer.on('featureMouseout featureMouseover', function(e, feature) {
                    _this.featureHover(e, feature);
                });

                _this.drawOverLay();
            });

            return this;
        }
    },

    drawOverLay: function(){
        var data = this.getDataByAdcode();

        if(data.length){
            this.mapData = data;
            this.createColorScale();

            var newData = {};
            for(var i = 0; i < data.length; i++){
                newData[data[i].adcode] = data[i];
            }

            this.renderAreaNode(newData);
        }else{
            console.warn("No Data: "+ this.adcode);
        }
    },

    renderAreaNode: function(data){
        districtExplorer.clearFeaturePolygons();
        this.map.clearMap();

        var _this = this;

        //根据城市adcode获取下级城市信息
        districtExplorer.loadAreaNode(_this.adcode, function(error, areaNode) {
            if (error) {
                console.error(error);
                return;
            }

            currentAreaNode = window.currentAreaNode = areaNode;
            districtExplorer.setAreaNodesForLocating([currentAreaNode]);

            //绘制区块
            districtExplorer.renderSubFeatures(areaNode, function(feature, i) {
                var fadcode = feature.properties.adcode;
                var fdata = data[fadcode];
                if(fdata) {
                    var v = fdata.value;
                    var fc = _this.colorScale(v);

                    //绘制marker和text
                    _this.drawMarker(feature);
                    _this.drawText(feature, fdata);

                    return {
                        cursor: 'pointer',
                        bubble: true,
                        strokeColor: '#fff',
                        strokeOpacity: 1,
                        strokeWeight: 1,
                        fillColor: fc,
                        extData: {fillColor: fc},
                        fillOpacity: 1,
                    };
                }
            });

            //根据adcode设置合适的缩放值和中心点
            switch(_this.adcode){
                case 100000:
                    _this.map.setZoomAndCenter(5, [104.114129, 34.550339]);
                    break;
                case 460000:
                    _this.map.setZoomAndCenter(8, [110.33119, 19.031971]);
                    break;
                default:
                    _this.map.setFitView(districtExplorer.getAllFeaturePolygons());
                    break;
            }
        });
    },

    drawText: function(feature, data){
        var position = feature.properties.centroid ? feature.properties.centroid : feature.properties.center;
        var textEle = '<div style="text-align:center;line-height: 13px;">' + data.name + '<br>' + data.value + '</div>';
        var text = new AMap.Text({
            text: textEle,
            textAlign: 'center',
            verticalAlign: 'middle',
            cursor: 'pointer',
            bubble: true,
            style:{
                'background-color':'transparent',
                'border':'solid 0px transparent',
                'padding':'0px',
                'font-size': '10px'
            },
            position: position
        });

        feature.text = text;
        text.setMap(this.map);
    },

    drawMarker: function(feature){
        var position = feature.properties.centroid ? feature.properties.centroid : feature.properties.center;
        var icon = new AMap.Icon({size: new AMap.Size(62, 62), image: img});

        var marker = new AMap.Marker({
            offset: new AMap.Pixel(-31, -34),
            icon: icon,
            bubble: true,
            draggable: false,
            cursor: 'pointer',
            position: position
        });

        feature.marker = marker;
        marker.setMap(this.map);
    },

    featureClick: function(feature){
        var data = this.mapData;
        var props = feature.properties;
        var value = 0;

        for(var i = 0; i < data.length; i++){
            var item = data[i];
            if(item.adcode == props.adcode){
                value = item.value
                break;
            }
        }

        if (props.childrenNum > 0 && value > 0){
            this.adcode = feature.properties.adcode;
            this.drawOverLay();
        }
    },

    featureHover: function(event, feature){
        var marker = feature.marker;
        var text = feature.text;
        if(!marker || !text) return;

        var zindex = 100;
        var isHover = false;
        if(event.type == 'featureMouseover') {
            isHover = true;
            zindex = 110;
        }
        marker.setzIndex(zindex);
        text.setzIndex(zindex);


        var targetAdcode = feature.properties.adcode;
        var polys = districtExplorer.findFeaturePolygonsByAdcode(targetAdcode);

        for (var i = 0, len = polys.length; i < len; i++) {
            var opts = polys[i].getOptions();
            polys[i].setOptions({
                fillColor: (isHover ? '#ccc' : opts.extData.fillColor),
                fillOpacity: (isHover ? 0.9 : 1)
            });
        }

    },
    getDataByAdcode: function(){
        var adcode = this.adcode;
        var res = [];
            for(var i = 0; i < testData.length; i++){
            var item = testData[i];
            if(item.padcode == adcode){
                res.push(item);
            }
        }
        return res;
    }
}

var myGDMap = new GDMap({container: 'chart-panel', adcode: 100000});