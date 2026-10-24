package com.example.midterm.model;

public class LoginLog {
    private String id;
    private String userId;
    private String username;
    private long timestamp;
    private String deviceInfo;

    public LoginLog(){
    }

    public LoginLog(String id, String userId, String username, long timestamp, String deviceInfo){
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.timestamp = timestamp;
        this.deviceInfo = deviceInfo;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public long getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public String getDeviceInfo(){
        return deviceInfo;
    }
    public void setDeviceInfo(String deviceInfo){
        this.deviceInfo = deviceInfo;
    }
}