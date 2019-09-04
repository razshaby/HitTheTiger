package com.e.hw2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;


public class MainActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;

    private final int maxSounds = 5;
    private final int COLUMNS = 3;
    private final int ROWS = 3;
    private final int MAX_LIFE=3;
    private final int INITIAL_SCORE=0;
    private final int INITIAL_TIME=30;
    private final int WIN_SCORE=30;

    private MediaPlayer victory_sound;
    private MediaPlayer song_sound;

    private MediaPlayer lose_sound;

    private boolean keepPlay=false;
    private boolean keepCount=true;
    private boolean player_playing =true;

    private int time_left=INITIAL_TIME;
    private int score=INITIAL_SCORE;
    private int life=0;
    private int currentSound=0;
    private int width_phone;
    private int height_phone;

    private String userID;

    private  Button playAgainBtn;
    private  Button scoreBtn;

    private TextView message;
    private CountDownTimer countDownTimer;
    private ImageView[][] imageView;
    private int[][] wait;
    private ImageView[] heartView;


    private TextView score_view;
    private TextView time_left_view;

    private  LinearLayout life_view;
    private RelativeLayout mainLayout;
    private Animation animation;
    private  Handler handler = new Handler();
    private GridLayout gridLayout;


    //Database
    DatabaseReference databaseUsers;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //Database
        databaseUsers=FirebaseDatabase.getInstance().getReference("users");




        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_layout);
        gridLayout= initGridLayout(ROWS,COLUMNS);

      //fireBase();


        initSoundSong();
        init_width_and_height();
        initHeartImages();
        life_view=findViewById(R.id.life_container);
        add_hearts_to_life_view();
        //message=new TextView(this);
        message=findViewById(R.id.msg_game);
        mainLayout.removeView(message);
initPlayAgainBtn();
       initScoreBtn();
        score_view=findViewById(R.id.the_score);
        time_left_view=findViewById(R.id.time_left);
        wait=new int[ROWS][COLUMNS];
        lose_sound=MediaPlayer.create(MainActivity.this,R.raw.lose);
        victory_sound=MediaPlayer.create(MainActivity.this,R.raw.victory);
        song_sound=MediaPlayer.create(MainActivity.this,R.raw.song);

        song_sound.setLooping(true);


        //song_sound.start();

        imageView=new ImageView[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                imageView[i][j]=new ImageView(MainActivity.this);
                imageView[i][j].setImageResource(R.drawable.tiger);
                imageView[i][j].setLayoutParams(new ViewGroup.LayoutParams(width_phone/3,height_phone/6));


                imageView[i][j].setOnClickListener(new View.OnClickListener() {
                    public void onClick(final View v) {
                        if(!player_playing)
                            return;
                        final MediaPlayer mp;

                        if(v.getAlpha()!=0) {
                            if(String.valueOf(v.getTag()).equals("1"))
                            {score_view.setText("" + (++score));
                                mp=MediaPlayer.create(MainActivity.this,R.raw.tiger);
                            }
                            else
                            {//removeOneLife();
                                if(score>=3)
                                 score-=3;
                                else
                                score =0;
                               score_view.setText(""+score);
                                mp=MediaPlayer.create(MainActivity.this,R.raw.no);
                            }



//                            if(score==WIN_SCORE)
//                            {
//                                win();
//                                return;
//                            }
                            // mp=new MediaPlayer();

                            animation = AnimationUtils.loadAnimation(MainActivity.this,R.layout.rotation_animation);

                            v.startAnimation(animation);

                            animation.setAnimationListener(new Animation.AnimationListener(){
                                @Override
                                public void onAnimationStart(Animation arg0) {
                                    v.setClickable(false);
                                }
                                @Override
                                public void onAnimationRepeat(Animation arg0) {
                                }
                                @Override
                                public void onAnimationEnd(Animation arg0) {
                                    //  v.setVisibility(View.INVISIBLE);
                                    v.setAlpha(0.0f);
                                    //v.setClickable(true);

                                }
                            });
                        }

                        else {

                            mp=MediaPlayer.create(MainActivity.this,R.raw.ohboy);

                                removeOneLife();


                        }
                        mp.start();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mp.release();
                            }
                        }, 1000);


                    }
                });


                gridLayout.addView(imageView[i][j]);

            }
        }


        handler.postDelayed(showImagesRunnable, 200);
        startTimer();
    }

    private void fireBase() {
//         FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
////        DatabaseReference mDatabaseReference = mDatabase.getReference();
////        mDatabaseReference = mDatabase.getReference().child("name");
////        mDatabaseReference.setValue("Donald Duck");

//        // Write a message to the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");
//
//        myRef.setValue("Hello, World3!" + userID);
        Bundle inBundle = getIntent().getExtras();
        String name = inBundle.get("name").toString();
        String surname = inBundle.get("surname").toString();
        String imageUrl = inBundle.get("imageUrl").toString();
        userID=inBundle.get("id").toString();


        String id=name+" "+surname + ", score: "+ score+ ", time_left: "+time_left+",     "+databaseUsers.push().getKey();
            User user=new User(name + " " + surname,userID,imageUrl,score,time_left);
            databaseUsers.child(id).setValue(user);
    }

    private void removeOneLife() {
        life_view.removeViewAt(--life);
        if(life==0)
            timesUp();
    }

    private void initSoundSong() {
        final ImageView imageView = findViewById(R.id.soundSong);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(song_sound.isPlaying())
                {song_sound.pause();
                    imageView.setImageResource(R.drawable.soundoff);

                }
                else
                    {
                        song_sound.start();
                        imageView.setImageResource(R.drawable.soundon);

                    }


            }
        });
    }


    public void startTimer() {
        time_left=INITIAL_TIME;
        TimerRunnable runnable = new TimerRunnable(INITIAL_TIME);
        new Thread(runnable).start();
    }

    private void add_hearts_to_life_view() {
        for (int i = life; i < MAX_LIFE; i++) {
            life_view.addView(heartView[i]);
        }
        life=MAX_LIFE;
    }


    private void init_width_and_height() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width_phone = size.x;
        height_phone = size.y;
        Log.e("Width", "" + width_phone);
        Log.e("height", "" + height_phone);
    }

    private void initHeartImages() {
        heartView=new ImageView[3];
        for (int i = 0; i < 3; i++) {
            heartView[i]=new ImageView(MainActivity.this);
            heartView[i].setImageResource(R.drawable.heart);
            heartView[i].setLayoutParams(new ViewGroup.LayoutParams(width_phone/10,height_phone/25));
        }
    }

    private void timesUp() {

                    stopGame();
                    showTimesUpMessage();
                    showPlayAgain();
                    showScoreBtn();

//        lose_sound=MediaPlayer.create(MainActivity.this,R.raw.lose);
//        lose_sound.start();


    }


    private void win() {
        stopGame();
        showWineMessage();
        showPlayAgain();
        //final MediaPlayer mp=MediaPlayer.create(MainActivity.this,R.raw.victory);
        victory_sound=MediaPlayer.create(MainActivity.this,R.raw.victory);
        victory_sound.start();



    }

    private void showPlayAgain() {
        mainLayout.addView(playAgainBtn);
    }

    private void showScoreBtn() {
        mainLayout.addView(scoreBtn);
    }


    private  void  initScoreBtn()
{
    scoreBtn=findViewById(R.id.scoreBtn);
    mainLayout.removeView(scoreBtn);
    scoreBtn.setBackgroundResource(R.drawable.btn);
    scoreBtn.setTextSize(30);
    scoreBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }
    });
}
    private void initPlayAgainBtn()
    {
        playAgainBtn=findViewById(R.id.playAgainBtn);
        mainLayout.removeView(playAgainBtn);
        playAgainBtn.setBackgroundResource(R.drawable.btn);
        playAgainBtn.setTextSize(30);
        playAgainBtn.setText("Play again");
        playAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player_playing=true;
                add_hearts_to_life_view();
                handler.post(showImagesRunnable);
                mainLayout.removeView(message);
                mainLayout.removeView(playAgainBtn);
                mainLayout.removeView(scoreBtn);
                score=0;
                score_view.setText(""+score);
                victory_sound.release();
                lose_sound.release();

                startTimer();

            }
        });
    }
    private String getPlayerName() {
//        Bundle extras=getIntent().getExtras();
//        return extras.getString(Keys.PLAYER_NAME);
        Bundle inBundle = getIntent().getExtras();
        String name = inBundle.get("name").toString();
        String surname = inBundle.get("surname").toString();
        String imageUrl = inBundle.get("imageUrl").toString();
        userID=inBundle.get("id").toString();
        fireBase();
return name;
    }
    private void showTimesUpMessage() {
        message.setText(getPlayerName()+",game's over!");
        message.setTextSize(50);
        message.setTextColor(Color.rgb(200,0,0));
        mainLayout.addView(message);
    }
    private void showWineMessage() {
        message.setText("Well done "+getPlayerName()+ " You Win!");
        message.setTextSize(50);
        message.setTextColor(Color.rgb(200,0,0));
        mainLayout.addView(message);
    }

    private void stopGame() {
        player_playing =false;
        handler.removeCallbacksAndMessages(null);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j <COLUMNS ; j++) {
                imageView[i][j].setAlpha(0.0f);
            }
        }
    }

    private GridLayout initGridLayout(int rows, int colums) {
        //ViewGroup.LayoutParams gridLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        GridLayout gridLayout=new GridLayout(this);
        gridLayout=findViewById(R.id.game_grid);
        gridLayout.setColumnCount(colums);
        gridLayout.setRowCount(rows);

      //  gridLayout.setLayoutParams(gridLayoutParams);
        gridLayout.setOrientation(GridLayout.HORIZONTAL);
        return  gridLayout;
    }

    private Runnable showImagesRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {


                    Random rand = new Random();
                    int n = rand.nextInt(9);

                    if(time_left<=2 && score>=25)
                    {

                        imageView[i][j].setClickable(true);
                        imageView[i][j].setAlpha(1.0f);
                        if((j+n)%2==0) {
                            imageView[i][j].setImageResource(R.drawable.tiger);
                            imageView[i][j].setTag(1);
                        }
                        else
                        {
                            imageView[i][j].setImageResource(R.drawable.scar);
                            imageView[i][j].setTag(0);
                        }
                    }
                    else {
                        if (n > 3) {
                            imageView[i][j].setAlpha(0.0f);
                            imageView[i][j].setClickable(true);
                        } else {
                            wait[i][j] += 1;

                            imageView[i][j].setClickable(true);
                            imageView[i][j].setAlpha(1.0f);

                            if (wait[i][j] % 2 == 0) {

                                imageView[i][j].setImageResource(R.drawable.tiger);
                                imageView[i][j].setTag(1);


                            } else {
                                imageView[i][j].setImageResource(R.drawable.scar);
                                imageView[i][j].setTag(0);


                            }


                        }
                    }
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    class TimerRunnable implements Runnable {
        int seconds;

        TimerRunnable(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            while (time_left>=0 && player_playing==true)
            {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if((time_left!=-1)&&keepCount)
                        time_left_view.setText(""+time_left--);
                        if(time_left==4)
                        {
                            final MediaPlayer mp=MediaPlayer.create(MainActivity.this,R.raw.tick);
                            mp.start();

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mp.release();
                                }
                            }, 5000);
                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(player_playing)
                    timesUp();
                }
            });


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        keepCount=false;
        if(song_sound.isPlaying()) {
            song_sound.pause();
            keepPlay = true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        keepCount=true;

        if(keepPlay==true)
        {
            keepPlay=false;
            song_sound.start();;
        }


    }
}
