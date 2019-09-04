package com.e.hw2;

import java.util.Calendar;
import java.util.Date;

public class User {
    private String name;
    private String id;
    private  String image;
    private  int timeLeft;
private  int score;
    Date currentTime;
    public User()
    {

    }




    public User(String name, String id, String image,int score,int timeLeft) {
        this.name = name;
        this.id = id;
        this.image = image;
        this.score=score;
        this.timeLeft=timeLeft;
        this.currentTime= Calendar.getInstance().getTime();

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

    public String getCurrentTime() {
        return currentTime.toString();
    }
}
