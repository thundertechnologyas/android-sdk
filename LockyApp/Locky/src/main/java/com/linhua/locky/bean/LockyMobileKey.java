package com.linhua.locky.bean;

public class LockyMobileKey {

    private String token;
    private String tenantId;

    public LockyMobileKey(String tenantId, String token) {
        this.token = token;
        this.tenantId = tenantId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
