package com.linhua.locky.bean;

public class LockDevice {
    private String id;
    private String name;
    private Boolean hasBLE = false;

    public Boolean getHasBLE() {
        return hasBLE;
    }

    public void setHasBLE(Boolean hasBLE) {
        this.hasBLE = hasBLE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
