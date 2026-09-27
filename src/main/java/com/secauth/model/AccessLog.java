package com.secauth.model;

import java.time.LocalDateTime;

public class AccessLog {
    private long   id;
    private String username;
    private String ipAddress;
    private String action;
    private String result;
    private String detail;
    private LocalDateTime loggedAt;

    public long   getId()        { return id; }
    public String getUsername()  { return username; }
    public String getIpAddress() { return ipAddress; }
    public String getAction()    { return action; }
    public String getResult()    { return result; }
    public String getDetail()    { return detail; }
    public LocalDateTime getLoggedAt() { return loggedAt; }

    public void setId(long id)               { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setIpAddress(String ip)      { this.ipAddress = ip; }
    public void setAction(String action)     { this.action = action; }
    public void setResult(String result)     { this.result = result; }
    public void setDetail(String detail)     { this.detail = detail; }
    public void setLoggedAt(LocalDateTime t) { this.loggedAt = t; }
}
