package com.musyzian.firebase;

import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class User implements Parcelable {

    Uri profileImageUrl;
    String name, age, city;
    List<String> instruments, artists;
    Location location;
    Float distance;
    String id;

    //CONSTRUCTOR


    public User(Uri profileImageUrl, String name, String age, String city, List<String> instruments, List<String> artists, Location location, Float distance, String id) {
        this.profileImageUrl = profileImageUrl;
        this.name = name;
        this.age = age;
        this.city = city;
        this.instruments = instruments;
        this.artists = artists;
        this.location = location;
        this.distance = distance;
        this.id = id;
    }

    protected User(Parcel in) {
        profileImageUrl = in.readParcelable(Uri.class.getClassLoader());
        name = in.readString();
        age = in.readString();
        city = in.readString();
        instruments = in.createStringArrayList();
        artists = in.createStringArrayList();
        location = in.readParcelable(Location.class.getClassLoader());
        if (in.readByte() == 0) {
            distance = null;
        } else {
            distance = in.readFloat();
        }
        id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(profileImageUrl, flags);
        dest.writeString(name);
        dest.writeString(age);
        dest.writeString(city);
        dest.writeStringList(instruments);
        dest.writeStringList(artists);
        dest.writeParcelable(location, flags);
        if (distance == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(distance);
        }
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
