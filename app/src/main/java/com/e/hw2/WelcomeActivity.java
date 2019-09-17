package com.e.hw2;

import android.content.Intent;
import android.graphics.Point;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import static com.e.hw2.util.Keys.USER_ID;
import static com.e.hw2.util.Keys.USER_IMAGE_URL;
import static com.e.hw2.util.Keys.USER_NAME;
import static com.e.hw2.util.Keys.USER_SURNAME;


public class WelcomeActivity extends AppCompatActivity {


    private EditText nameEditText;
    private Button btn_startGame;
    private LoginButton loginButton;
    private TextView textViewEnterName;

    private int width_phone;
    private int height_phone;

    private FirebaseAuth firebaseAuth;
    private CallbackManager mCallbackManager;

    private static final String TAG = "FACELOG";
    private AccessTokenTracker fbTracker;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        location = new Location();

        loginButton = findViewById(R.id.facebook_login_button);
        btn_startGame = findViewById(R.id.btn_welcome_startGame);
        nameEditText = findViewById(R.id.name_editText);
        textViewEnterName = findViewById(R.id.enter_name_text);


        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();


        loginButton.setPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //
                Profile profile = Profile.getCurrentProfile();
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }


            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


        init_width_and_height();


        btn_startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!checkLocation()) {
                    askLocation();
                    return;
                }

                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    startGameActivity();
                    return;
                }

                if (!checkEditTextNotEmpty()) {
                    return;
                }


                startGameActivity();

            }
        });


        fbTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                if (accessToken2 == null) {
                    Log.d("FB", "User Logged Out.");
                    nameEditText.setVisibility(View.VISIBLE);
                    textViewEnterName.setVisibility(View.VISIBLE);
                    firebaseAuth.signOut();

                }
            }
        };
    }

    private boolean checkLocation() {
        return location.checkLocation(WelcomeActivity.this);
    }

    public void askLocation() {
        location.askLocation(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
//            logInUI(currentUser);
//            btn_startGame.setClickable(true);
            nameEditText.setVisibility(View.GONE);
            textViewEnterName.setVisibility(View.GONE);
        } else {
            nameEditText.setVisibility(View.VISIBLE);
            textViewEnterName.setVisibility(View.VISIBLE);
        }


    }

    //Toast
    private boolean checkEditTextNotEmpty() {


        String name = nameEditText.getText().toString();
        if (name.trim().length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Please enter your name", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, height_phone / 3);
            toast.show();
            return false;
        }
        return true;
    }


    //Activity
    private void startGameActivity() {
        Profile profile = Profile.getCurrentProfile();

        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        if (profile != null) {
            intent.putExtra(USER_NAME, profile.getFirstName());
            intent.putExtra(USER_SURNAME, profile.getLastName());
            intent.putExtra(USER_IMAGE_URL, profile.getProfilePictureUri(200, 200).toString());
            intent.putExtra(USER_ID, profile.getId());
        } else {
            String name = nameEditText.getText().toString().trim();


            if (name.length() != 0) {
                intent.putExtra(USER_NAME, "-Guest- " + name);
                intent.putExtra(USER_SURNAME, "");
                intent.putExtra(USER_IMAGE_URL, "");
                intent.putExtra(USER_ID, "");
            }


        }
        startActivity(intent);

    }

    //UI
    private void logInUI(FirebaseUser currentUser) {
        Toast.makeText(WelcomeActivity.this, "You're logged in", Toast.LENGTH_LONG).show();
        nameEditText.setVisibility(View.GONE);
        textViewEnterName.setVisibility(View.GONE);
    }


    //General
    private void init_width_and_height() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width_phone = size.x;
        height_phone = size.y;
        Log.e("Width", "" + width_phone);
        Log.e("height", "" + height_phone);
    }

    //Facebook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            logInUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(WelcomeActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            logInUI(null);
                        }


                    }
                });
    }


}
