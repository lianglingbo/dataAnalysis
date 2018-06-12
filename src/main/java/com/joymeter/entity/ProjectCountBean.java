package com.joymeter.entity;

/**
 * @ClassName ProjectCountBean
 * @Description TODO
 * 各项目数量统计实体类
 * @Author liang
 * @Date 2018/6/12 20:53
 * @Version 1.0
 **/
public class ProjectCountBean {
    private String myProject;
    private String myCount;

    public String getMyProject() {
        return myProject;
    }

    public void setMyProject(String myProject) {
        this.myProject = myProject;
    }

    public String getMyCount() {
        return myCount;
    }

    public void setMyCount(String myCount) {
        this.myCount = myCount;
    }

    @Override
    public String toString() {
        return "ProjectCountBean{" +
                "myProject='" + myProject + '\'' +
                ", myCount='" + myCount + '\'' +
                '}';
    }
}
