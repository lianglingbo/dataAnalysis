package com.joymeter.entity;

/**
 * @ClassName TotalNumBean
 * @Description TODO
 * 用于返回可视化页面所需的各字段
 * @Author liang
 * @Date 2018/6/11 21:42
 * @Version 1.0
 **/
public class TotalNumBean {
    private String totalCount;
    private String offDeviceCount;
    private String offGatewayCount;
    private String readFaileCount;
    private String noneDataCount;

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getOffDeviceCount() {
        return offDeviceCount;
    }

    public void setOffDeviceCount(String offDeviceCount) {
        this.offDeviceCount = offDeviceCount;
    }

    public String getOffGatewayCount() {
        return offGatewayCount;
    }

    public void setOffGatewayCount(String offGatewayCount) {
        this.offGatewayCount = offGatewayCount;
    }

    public String getReadFaileCount() {
        return readFaileCount;
    }

    public void setReadFaileCount(String readFaileCount) {
        this.readFaileCount = readFaileCount;
    }

    public String getNoneDataCount() {
        return noneDataCount;
    }

    public void setNoneDataCount(String noneDataCount) {
        this.noneDataCount = noneDataCount;
    }

    @Override
    public String toString() {
        return "TotalNumBean{" +
                "totalCount='" + totalCount + '\'' +
                ", offDeviceCount='" + offDeviceCount + '\'' +
                ", offGatewayCount='" + offGatewayCount + '\'' +
                ", readFaileCount='" + readFaileCount + '\'' +
                ", noneDataCount='" + noneDataCount + '\'' +
                '}';
    }
}
