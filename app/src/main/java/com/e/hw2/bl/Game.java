package com.e.hw2.bl;

public class Game {
    //GAME BL
    private final int COLUMNS = 3;
    private final int ROWS = 3;
    private final int MAX_LIFE = 3;
    private final int INITIAL_SCORE = 0;
    private final int INITIAL_TIME = 30;
    private int time_left = INITIAL_TIME;
    private int score = INITIAL_SCORE;
    private int life = 0;


    public Game() {
    }

    public int getCOLUMNS() {
        return COLUMNS;
    }

    public int getROWS() {
        return ROWS;
    }

    public int getMAX_LIFE() {
        return MAX_LIFE;
    }

    public int getINITIAL_SCORE() {
        return INITIAL_SCORE;
    }

    public int getINITIAL_TIME() {
        return INITIAL_TIME;
    }


    public int getTime_left() {
        return time_left;
    }

    public int getScore() {
        return score;
    }

    public int getLife() {
        return life;
    }

    public int incrementScoreByOne() {
        score++;
        return score;
    }

    public  void  setScoreToZero()
    {
        score=0;
    }

    public void wrongHit()
    {
        if (score >= 3)
            score -= 3;
        else
            score = 0;
    }

    public void onwSecondLeft()
    {
        time_left--;
    }
    public void initTime_left()
    {
        time_left=INITIAL_TIME;
    }

    public void setMaxLife(){
        life=MAX_LIFE;
    }
    public void removeOneLife() {

       life--;
    }
}
