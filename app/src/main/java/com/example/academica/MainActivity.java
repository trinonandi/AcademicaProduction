package com.example.academica;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.academica.Admin.AdminHomeActivity;
import com.example.academica.Student.StudentHomeActivity;
import com.example.academica.Teacher.TeacherHomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    public final static String splash_run_time = "first_time";

    private SharedPreferences sharedPreferences ;
    private SharedPreferences.Editor editor ;

    private SessionManagement sessionManagement;
    private String session;
    private String check;
    LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        // Adding shared_prefences to show splash screen for the first time

        SharedPreferences sharedPreferences = getSharedPreferences(splash_run_time, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(splash_run_time, splash_run_time);

        editor.commit();
        editor.apply();

        //fetching and checking the string
        check = sharedPreferences.getString(splash_run_time, "");

        if (check == "first_time") {
            editor.putString("not_first_time",splash_run_time);
            editor.commit();
            editor.apply();
            lottieAnimationView = findViewById(R.id.splash_intro);
            new Handler().postDelayed(() -> {

                //checking the string value

                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }, 5000);




        }
        else{
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }


    }


}

// font - coveredByYourGrace