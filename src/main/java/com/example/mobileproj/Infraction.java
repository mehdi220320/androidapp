package com.example.mobileproj;

public class Infraction {
    public String id;
    public String numserie;
    public String date;
    public String location;
    public String photoBase64; // or store photo URL if using Firebase Storage

    public Infraction() {} // Required for Firebase

    public Infraction(String id, String numserie, String date, String location, String photoBase64) {
        this.id = id;
        this.numserie = numserie;
        this.date = date;
        this.location = location;
        this.photoBase64 = photoBase64;
    }
}