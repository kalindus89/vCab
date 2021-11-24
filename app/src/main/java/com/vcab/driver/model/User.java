package com.vcab.driver.model;

public class User {

    private String name;
    private String phone;
    private String email;
    private String profileImage;
    private String firebaseToken;
    private double rating;

    public User() {
    }

    public User(String name, String phone, String email, String profileImage, String firebaseToken, double rating) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.profileImage = profileImage;
        this.firebaseToken = firebaseToken;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
