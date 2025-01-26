package com.example.cleanfix.model;

import java.util.List;

public class Issue {
    private String issueId;
    private String userId;
    private String description;
    private double latitude;
    private double longitude;
    private List<String> photoUrls;
    private String status;
    private String timestamp;

    // Default constructor (required for Firebase)
    public Issue() {}

    public Issue(String issueId, String userId, String description, double latitude, double longitude,
                 List<String> photoUrls, String status, String timestamp) {
        this.issueId = issueId;
        this.userId = userId;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrls = photoUrls;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
