package com.e.hw2;

import java.util.Calendar;
import java.util.Date;

public class User implements Comparable<User> {
    private String key;
    private String name;
    private String id;
    private  String image;
    private  int timeLeft;
    private  int score;
    private long currentTime;
    private double latitude;
    private double longitude;
    public User()
    {

    }




    public User(String name, String id, String image,int score,int timeLeft,double latitude,double longitude,String key) {
        this.name = name;
        this.id = id;
        this.image = image;
        this.score=score;
        this.timeLeft=timeLeft;
        this.currentTime= Calendar.getInstance().getTimeInMillis();
        this.latitude=latitude;
        this.longitude=longitude;
        this.key=key;
    }

    public String getKey() {
        return key;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public int getScore() {
        return score;
    }

    public Long getCurrentTime() {
        return currentTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int compareTo(User o) {
        return o.getScore()-this.score ;
    }
}
