package com.luban.entity;

import fr.opensagres.xdocreport.document.images.AbstractImageProvider;

public class UserAvatar {
    private String dept;
    private String name;
    private AbstractImageProvider avatar;

    public UserAvatar() {
    }

    public UserAvatar(String dept, String name, AbstractImageProvider avatar) {
        this.dept = dept;
        this.name = name;
        this.avatar = avatar;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractImageProvider getAvatar() {
        return avatar;
    }

    public void setAvatar(AbstractImageProvider avatar) {
        this.avatar = avatar;
    }
}
