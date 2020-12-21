package com.example.academica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentHome extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onBackPressed() {

    }

    public void doLogout(View view){
        mAuth.signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
}