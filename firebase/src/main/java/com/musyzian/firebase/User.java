package com.musyzian.firebase;

import android.net.Uri;

import java.util.List;

public class User {

    Uri profileImageUrl;
    String name, age, city;
    List<String> instruments, artists;

    //CONSTRUCTOR


    public User(Uri profileImageUrl, String name, String age, String city, List<String> instruments, List<String> artists) {
        this.profileImageUrl = profileImageUrl;
        this.name = name;
        this.age = age;
        this.city = city;
        this.instruments = instruments;
        this.artists = artists;
    }

    //GETTERS & SETTERS
    public Uri getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(Uri profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public List<String> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<String> instruments) {
        this.instruments = instruments;
    }

    public List<String> getArtists() {
        return artists;
    }

    public void setArtists(List<String> artists) {
        this.artists = artists;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
