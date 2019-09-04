package com.e.hw2;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Point;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.e.hw2.util.Keys.PLAYER_NAME;


public class WelcomeActivity extends AppCompatActivity {


    private EditText nameEditText;
    private Button btn_startGame;
    private LoginButton loginButton;

    private int width_phone;
    private int height_phone;

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    private static final String TAG = "FACELOG";



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        FacebookSdk.sdkInitialize(getApplicationContext());


        loginButton = findViewById(R.id.login_button);
        btn_startGame=findViewById(R.id.btn_welcome_startGame);
        nameEditText = findViewById(R.id.name_editText);


        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();




        loginButton.setPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //
                Profile profile = Profile.getCurrentProfile();
                nextActivity(profile);

                //
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

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if(currentUser!=null) {
//            updateUI(currentUser);
//            btn_startGame.setClickable(true);
                    openGameActivity();

                }

                if (!checkEditTextNotEmpty())
                    return;

                openGameActivity();

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
//            updateUI(currentUser);
//            btn_startGame.setClickable(true);
        }

    }



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

    private void openGameActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(PLAYER_NAME, nameEditText.getText().toString());
        //startActivity(intent);
        Profile profile = Profile.getCurrentProfile();
        nextActivity(profile);
    }
    private void nextActivity(Profile profile) {
        if(profile != null){
            Intent main = new Intent(WelcomeActivity.this, MainActivity.class);
            String name=nameEditText.getText().toString().trim();
            if (name.length() != 0) {
                main.putExtra("name", name);

            }
            else
            main.putExtra("name", profile.getFirstName());
            main.putExtra("surname", profile.getLastName());
            main.putExtra("imageUrl", profile.getProfilePictureUri(200,200).toString());
            main.putExtra("id",profile.getId());
            startActivity(main);
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        Toast.makeText(WelcomeActivity.this,"You're logged in",Toast.LENGTH_LONG).show();
        openGameActivity();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(WelcomeActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        FirebaseAuth.getInstance().signOut();
//
//    }


    private void init_width_and_height() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width_phone = size.x;
        height_phone = size.y;
        Log.e("Width", "" + width_phone);
        Log.e("height", "" + height_phone);
    }

}
