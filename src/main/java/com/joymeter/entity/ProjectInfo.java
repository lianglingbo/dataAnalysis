package com.joymeter.entity;

/**
 * @ClassName ProjectInfo
 * @Description TODO
 * 对应数据库表：project_info
 * @Author liang
 * @Date 2018/7/11 13:37
 * @Version 1.0
 **/
public class ProjectInfo {
    private String project;
    private String nickname;
    private String url;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "project='" + project + '\'' +
                ", nickname='" + nickname + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
