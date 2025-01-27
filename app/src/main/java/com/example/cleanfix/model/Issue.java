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
    private String timezone;
    private String country;
    private String postalCode;
    private String addressText;

    // Default constructor (required for Firebase)
    public Issue() {}

    public Issue(String issueId, String userId, String description, double latitude, double longitude, String addressText,
                 List<String> photoUrls, String status, String timestamp, String timezone, String country, String postalCode) {
        this.issueId = issueId;
        this.userId = userId;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrls = photoUrls;
        this.status = status;
        this.timestamp = timestamp;
        this.timezone = timezone;
        this.country = country;
        this.postalCode = postalCode;
        this.addressText = addressText;
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
    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public String getaddressText() {
        return addressText;
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
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
