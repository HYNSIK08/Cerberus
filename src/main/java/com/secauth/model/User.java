package com.secauth.model;

import java.time.LocalDateTime;

public class User {
    private long   id;
    private String username;
    private String passwordHash;
    private String role;
    private boolean enabled;
    private int    failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public long   getId()           { return id; }
    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole()         { return role; }
    public boolean isEnabled()      { return enabled; }
    public int    getFailedAttempts(){ return failedAttempts; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getLastLogin()   { return lastLogin; }

    public void setId(long id)                     { this.id = id; }
    public void setUsername(String username)       { this.username = username; }
    public void setPasswordHash(String h)          { this.passwordHash = h; }
    public void setRole(String role)               { this.role = role; }
    public void setEnabled(boolean enabled)        { this.enabled = enabled; }
    public void setFailedAttempts(int n)           { this.failedAttempts = n; }
    public void setLockedUntil(LocalDateTime t)    { this.lockedUntil = t; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }
    public void setLastLogin(LocalDateTime t)      { this.lastLogin = t; }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
}
