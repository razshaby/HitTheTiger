package com.e.hw2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.Toast;

import com.e.hw2.bl.Game;
import com.e.hw2.dl.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import static com.e.hw2.util.Keys.USER_ID;
import static com.e.hw2.util.Keys.USER_IMAGE_URL;
import static com.e.hw2.util.Keys.USER_NAME;
import static com.e.hw2.util.Keys.USER_SURNAME;


public class MainActivity extends AppCompatActivity implements LocationListener {


    private Game game;
    private boolean keepPlay = false;
    private boolean keepCount = true;
    private boolean player_playing = true;
    private int[][] wait;
    private Handler handler = new Handler();

    //GUI
    private Button playAgainBtn;
    private Button scoreBtn;
    private ImageView[][] imageView;
    private ImageView[] heartView;
    private TextView message;
    private TextView score_view;
    private TextView time_left_view;
    private LinearLayout life_view;
    private RelativeLayout mainLayout;
    private Animation animation;
    private GridLayout gridLayout;

    //Database
    private Firebase firebase;

    //Location
    private String locationLatitude = "";
    private String locationLongitude = "";
    private LocationManager locationManager;
    private int askLocationInterval = 3000; // 3 seconds by default, can be changed later
    private Handler locationHandler;

    //Sounds
    private MediaPlayer song_sound;

    //General
    private int width_phone;
    private int height_phone;
//------------------------------------------------------------functions-----------------------------------------------------------------//
    //Activity functions
            @Override
            protected void onPause() {
                super.onPause();
                keepCount = false;
                if (song_sound.isPlaying()) {
                    song_sound.pause();
                    keepPlay = true;
                }

            }
            @Override
            protected void onResume() {
                super.onResume();
                keepCount = true;

                if (keepPlay == true) {
                    keepPlay = false;
                    song_sound.start();
                    ;
                }


            }
            @Override
            public void onDestroy() {
                super.onDestroy();
                stopRepeatingTask();
            }
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);


                init_width_and_height();
                game = new Game();

                //Location
                setLocation();

                //Database
                firebase=new Firebase();

                //UI
                setUI();



                //Game Bl
                wait = new int[game.getROWS()][game.getCOLUMNS()];

                //Sounds
                setSounds();

                setImages();
                startTimer();
            }


    //Location functions
                private void setLocation() {
                    locationHandler = new Handler();
                    checkLocation();
                }
                private void checkLocation() {
                    startRepeatingTask();
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

                    }
                }
                @Override
                public void onLocationChanged(Location location) {
                    locationLatitude = location.getLatitude() + "";
                    locationLongitude = location.getLongitude() + "";
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }
                @Override
                public void onProviderEnabled(String provider) {
            //        startAgainAfterEnableLocation();
            //        checkLocation();
                }
                @Override
                public void onProviderDisabled(String provider) {
                    Toast.makeText(MainActivity.this, "Please Enable GPS", Toast.LENGTH_SHORT).show();
                    //stopGame();
                    finish();
                }
                Runnable mStatusChecker = new Runnable() {
                    @Override
                    public void run() {


                        try {
                            getLocation(); //this function can change value of askLocationInterval.

                            if (locationLatitude == "") {
                                // if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))

                                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                    // ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                                    //startAgainAfterEnableLocation();
                                    Toast.makeText(getApplicationContext(), "Approve location!", Toast.LENGTH_LONG).show();

                                    finish();
                                } else {

                                }
                            } else {

                                Log.d("current location", locationLatitude + "," + locationLongitude);

                            }
                        } finally {

                            locationHandler.postDelayed(mStatusChecker, askLocationInterval);
                        }
                    }
                };

                void startRepeatingTask() {
                    mStatusChecker.run();
                }

                void stopRepeatingTask() {
                    locationHandler.removeCallbacks(mStatusChecker);
                }
                void getLocation() {
                    try {
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, (LocationListener) this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }



    //UI functions
                private void setUI() {
                    mainLayout = findViewById(R.id.main_layout);
                    message = findViewById(R.id.msg_game);
                    mainLayout.removeView(message);
                    life_view = findViewById(R.id.life_container);
                    gridLayout = initGridLayout(game.getROWS(), game.getCOLUMNS());
                    score_view = findViewById(R.id.the_score);
                    time_left_view = findViewById(R.id.time_left);
                    initHeartImages();
                    add_hearts_to_life_view();
                    initPlayAgainBtn();
                    initScoreBtn();
                }
                private void setImages() {
                    imageView = new ImageView[game.getROWS()][game.getCOLUMNS()];
                    for (int i = 0; i < game.getROWS(); i++) {
                        for (int j = 0; j < game.getCOLUMNS(); j++) {
                            imageView[i][j] = new ImageView(MainActivity.this);

                            //Set image size
                            imageView[i][j].setLayoutParams(new ViewGroup.LayoutParams(width_phone / 3, height_phone / 6));

                            imageView[i][j].setOnClickListener(new View.OnClickListener() {
                                @SuppressLint("ResourceType")
                                public void onClick(final View v) {
                                    if (!player_playing)
                                        return;
                                    final MediaPlayer mp;

                                    if (v.getAlpha() != 0) {
                                        if (String.valueOf(v.getTag()).equals("1")) {
                                            score_view.setText("" + (game.incrementScoreByOne()));
                                            mp = MediaPlayer.create(MainActivity.this, R.raw.tiger);
                                        } else {
                                            game.wrongHit();
                                            score_view.setText("" + game.getScore());
                                            mp = MediaPlayer.create(MainActivity.this, R.raw.no);
                                        }


                                        //                            if(score==WIN_SCORE)
                                        //                            {
                                        //                                win();
                                        //                                return;
                                        //                            }
                                        // mp=new MediaPlayer();

                                        animation = AnimationUtils.loadAnimation(MainActivity.this, R.layout.rotation_animation);

                                        v.startAnimation(animation);

                                        animation.setAnimationListener(new Animation.AnimationListener() {
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
                                    } else {

                                        mp = MediaPlayer.create(MainActivity.this, R.raw.ohboy);

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

                            //Add images to gridLayout
                            gridLayout.addView(imageView[i][j]);

                        }
                    }

                    handler.postDelayed(showImagesRunnable, 200);

                }
                private void add_hearts_to_life_view() {
                    for (int i =game.getLife(); i < game.getMAX_LIFE(); i++) {
                        life_view.addView(heartView[i]);
                    }
                    game.setMaxLife();
                }
                private void initHeartImages() {
                    heartView = new ImageView[3];
                    for (int i = 0; i < 3; i++) {
                        heartView[i] = new ImageView(MainActivity.this);
                        heartView[i].setImageResource(R.drawable.heart);
                        heartView[i].setLayoutParams(new ViewGroup.LayoutParams(width_phone / 10, height_phone / 25));
                    }
                }
                private void showPlayAgain() {
                    mainLayout.addView(playAgainBtn);
                }
                private void showScoreBtn() {
                    mainLayout.addView(scoreBtn);
                }
                private void initScoreBtn() {
                    scoreBtn = findViewById(R.id.scoreBtn);
                    mainLayout.removeView(scoreBtn);
                    scoreBtn.setBackgroundResource(R.drawable.btn);
                    scoreBtn.setTextSize(30);
                    scoreBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //            if((!isGPSLocation)&&(!isNetworkLocation))
                            //                initMAP();
                            scoreBtn.setClickable(false);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    scoreBtn.setClickable(true);
                                }
                            }, 3000);
                            if (locationLatitude == "") {
                                Toast.makeText(getApplicationContext(), "Please wait, trying to retrieve coordinates.", Toast.LENGTH_LONG).show();

                            } else {
                                String userKey = fireBase();
                                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                intent.putExtra("lat", locationLatitude);
                                intent.putExtra("long", locationLongitude);
                                intent.putExtra("key", userKey);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
                private void initPlayAgainBtn() {
                    playAgainBtn = findViewById(R.id.playAgainBtn);
                    mainLayout.removeView(playAgainBtn);
                    playAgainBtn.setBackgroundResource(R.drawable.btn);
                    playAgainBtn.setTextSize(30);
                    playAgainBtn.setText("Play again");
                    playAgainBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startNewGame();


                        }
                    });
                }
                private GridLayout initGridLayout(int rows, int colums) {
                    //ViewGroup.LayoutParams gridLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    GridLayout gridLayout = new GridLayout(this);
                    gridLayout = findViewById(R.id.game_grid);
                    gridLayout.setColumnCount(colums);
                    gridLayout.setRowCount(rows);

                    //  gridLayout.setLayoutParams(gridLayoutParams);
                    gridLayout.setOrientation(GridLayout.HORIZONTAL);
                    return gridLayout;
                }
                private void showTimesUpMessage() {
        message.setText(getPlayerName() + ",game's over!");
        message.setTextSize(50);
        message.setTextColor(Color.rgb(200, 0, 0));
        mainLayout.addView(message);
    }

    //Sounds functions
                private void setSounds() {
                    initSoundSong();
                    song_sound = MediaPlayer.create(MainActivity.this, R.raw.song);
                    song_sound.setLooping(true);
                }

                private void initSoundSong() {
                    final ImageView imageView = findViewById(R.id.soundSong);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            if (song_sound.isPlaying()) {
                                song_sound.pause();
                                imageView.setImageResource(R.drawable.soundoff);

                            } else {
                                song_sound.start();
                                imageView.setImageResource(R.drawable.soundon);

                            }


                        }
                    });
                }

                public void startTimer() {
                    game.initTime_left();
                    TimerRunnable runnable = new TimerRunnable(game.getINITIAL_TIME());
                    new Thread(runnable).start();
                }




    //Game functions
            private void startNewGame() {
                player_playing = true;
                add_hearts_to_life_view();
                handler.post(showImagesRunnable);
                mainLayout.removeView(message);
                mainLayout.removeView(playAgainBtn);
                mainLayout.removeView(scoreBtn);
                game.setScoreToZero();
                score_view.setText("" + game.getScore());


                startTimer();
            }
            private void stopGame() {
                player_playing = false;
                handler.removeCallbacksAndMessages(null);
                for (int i = 0; i < game.getROWS(); i++) {
                    for (int j = 0; j < game.getCOLUMNS(); j++) {
                        imageView[i][j].setAlpha(0.0f);
                    }
                }
            }
            private void removeOneLife() {
                game.removeOneLife();
                life_view.removeViewAt(game.getLife());
                if (game.getLife() == 0)
                    timesUp();
            }
            private Runnable showImagesRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < game.getROWS(); i++) {
                for (int j = 0; j < game.getCOLUMNS(); j++) {


                    Random rand = new Random();
                    int n = rand.nextInt(9);

                    if (game.getTime_left() <= 28 && game.getScore() >= 1) {

                        imageView[i][j].setClickable(true);
                        imageView[i][j].setAlpha(1.0f);
                        if ((j + n) % 2 == 0) {
                            imageView[i][j].setImageResource(R.drawable.tiger);
                            imageView[i][j].setTag(1);
                        } else {
                            imageView[i][j].setImageResource(R.drawable.scar);
                            imageView[i][j].setTag(0);
                        }
                    } else {
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
            private void timesUp() {
        stopGame();
        showTimesUpMessage();
        showPlayAgain();
        showScoreBtn();
    }

    //Timer functions
            class TimerRunnable implements Runnable {
        int seconds;

        TimerRunnable(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            while (game.getTime_left() >= 0 && player_playing == true) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if ((game.getTime_left() != -1) && keepCount) {
                            time_left_view.setText("" + game.getTime_left());
                            game.onwSecondLeft();
                        }

                        if (game.getTime_left() == 4) {
                            final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.tick);
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
                    if (player_playing)
                        timesUp();
                }
            });


        }
    }

    //General functions
            private void init_width_and_height() {
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    width_phone = size.x;
    height_phone = size.y;
    Log.e("Width", "" + width_phone);
    Log.e("height", "" + height_phone);
}


    //database functions
    private String fireBase() {

        Bundle inBundle = getIntent().getExtras();
        String name = inBundle.get(USER_NAME).toString();
        String surname = inBundle.get(USER_SURNAME).toString();
        String imageUrl = inBundle.get(USER_IMAGE_URL).toString();
        String userID = inBundle.get(USER_ID).toString();


        String userKey = name + " " + surname + ", score: " + game.getScore() + ", time_left: " + game.getTime_left() + ",     " + firebase.getUsersTable().push().getKey();
        User user = new User(name + " " + surname, userID, imageUrl, game.getScore(), game.getTime_left(), Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude), userKey);
        firebase.getUsersTable().child(userKey).setValue(user);
        return userKey;
    }

    //data from other activities
    private String getPlayerName() {
        Bundle inBundle = getIntent().getExtras();
        String name = inBundle.get(USER_NAME).toString();
        return name;
    }



}
