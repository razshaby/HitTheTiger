package com.e.hw2;

import androidx.fragment.app.FragmentActivity;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.e.hw2.dl.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private User theCurrentUser;
    private ArrayList<User> usersList;
    private GoogleMap mMap;
    //Database
    //private DatabaseReference dataBaseUsersReference;
    private Firebase firebase;

    private Handler handler;
    private ListView listView;
    private String[] usersArray;
    private int place = 1;
    private boolean firstMarker = true;
    private int theCurrentPlace;
    private TextView textViewPlace;

    //sounds
    private MediaPlayer victory_sound;
    private MediaPlayer lose_sound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_maps);

        //sounds
        victory_sound = MediaPlayer.create(MapsActivity.this, R.raw.victory);
        lose_sound = MediaPlayer.create(MapsActivity.this, R.raw.lose);


        //database
        firebase=new Firebase();
        Button start_btn = findViewById(R.id.btn_start_animation);
        textViewPlace = findViewById(R.id.place_txt);

        handler = new Handler();
        listView = findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPosition(position);
            }
        });
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                //setMarkers(usersList);
                handler.removeCallbacksAndMessages(null);

                showAnimation();
            }
        });

        Button stop_btn = findViewById(R.id.btn_stop_animation);
        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                handler.removeCallbacksAndMessages(null);
                setMarkers(usersList);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dataBase();

    }

    private void showPosition(int pos) {

        User user = usersList.get(pos);

        LatLng latLng = new LatLng(user.getLatitude(), user.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        victory_sound.release();
        lose_sound.release();
    }

    private void dataBase() {
        firebase.getUsersTable().orderByChild("score").limitToLast(10).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
//                        Log.d("raazz:  ", "onDataChange: "+String.valueOf(dataSnapshot.getValue()));
//
//                        List<User> list = new LinkedList<>();
//
//                        for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
//                            User user = userSnapshot.getValue(User.class);
//                            list.add(user);
//
//                        }
//                        Log.d("wwoww", "collectUsersInfo: "+list);

                        //   collectUsersInfo((Map<String,User>) dataSnapshot.getValue());
                        //   setMarkers();


                        usersList = sortUsers(dataSnapshot.getChildren());
                        usersArray = new String[usersList.size()];
                        setMarkers(usersList);

                        ArrayAdapter arrayAdapter = new ArrayAdapter(MapsActivity.this, android.R.layout.simple_list_item_1, usersArray);
                        listView.setAdapter(arrayAdapter);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }


    private ArrayList<User> sortUsers(Iterable<DataSnapshot> children) {
        ArrayList<User> list = new ArrayList<>();
        for (DataSnapshot child : children) {
            User user = child.getValue(User.class);
            list.add(user);
        }

        Collections.sort(list, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.compareTo(o2);
            }
        });

        return list;
    }


    private void showAnimation() {
        //run every one single
        place = 1;
        Iterator<User> userIterator = usersList.iterator();
        Runnable task = new Runnable() {
            @Override
            public void run() {


                if (userIterator.hasNext()) {
                    User user = userIterator.next();
                    Date d = new Date(user.getCurrentTime());
                    String text = (place++) + "." + user.getName() + ", score:" + user.getScore() + ", " + d;

                    LatLng latLng = new LatLng(user.getLatitude(), user.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(text);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    mMap.addMarker(markerOptions);

                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                }

                handler.postDelayed(this, 2000);
            }
        };
        handler.post(task);
    }

    private void setMarkers(List<User> usersList) {
        place = 1;
        for (User user : usersList) {

            Date d = new Date(user.getCurrentTime());
            String text = (place++) + "." + user.getName() + ", score:" + user.getScore() + ", " + d;

            LatLng latLng = new LatLng(user.getLatitude(), user.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(text);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            mMap.addMarker(markerOptions);
            if (firstMarker)
                usersArray[place - 2] = text;
        }
        if (firstMarker) {
            findUserPlace();
            if (theCurrentPlace == 1)
                victory_sound.start();
            else if (theCurrentPlace > 10) {
                lose_sound.start();
                textViewPlace.setText("Sorry, you not in the first 10!");

            }
            if (theCurrentPlace <= 10)
                textViewPlace.setText("Your rank is " + theCurrentPlace);
        }
        if (theCurrentPlace < 11)
            showPosition(theCurrentPlace - 1);
        else
            showPosition(1);
        firstMarker = false;


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle inBundle = getIntent().getExtras();
//        String locLat = inBundle.get("lat").toString();
//        String locLong = inBundle.get("long").toString();
//
//        LatLng latLng = new LatLng(Double.parseDouble(locLat),Double.parseDouble(locLong));
//
//        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("new try");
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
//
//        mMap.addMarker(markerOptions);
//        Toast.makeText(getApplicationContext(), "new:"  + "" + locLat+","+locLong, Toast.LENGTH_SHORT).show();


    }

    void findUserPlace() {
        int index = 1;
        Bundle inBundle = getIntent().getExtras();
        String key = inBundle.get("key").toString();
        for (User user : usersList) {


            if (user.getKey().equals(key)) {
                theCurrentUser = user;
                theCurrentPlace = index;

                return;
            }
            index++;
        }
        theCurrentPlace=11;
    }


}