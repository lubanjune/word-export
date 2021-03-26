package com.luban.entity;

public class User {
    private String name;
    private String dept;
    private String sign;

    public User() {
    }

    public User(String name, String dept, String sign) {
        this.name = name;
        this.dept = dept;
        this.sign = sign;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
