package com.example.academica;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentHome extends AppCompatActivity {

    RecyclerView recyclerView;
    StudentRecyclerAdapter studentRecyclerAdapter;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.studentrecycler);

        studentRecyclerAdapter = new StudentRecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        recyclerView.setAdapter(studentRecyclerAdapter);
    }

    @Override
    public void onBackPressed() {

    }

    public void doLogout(View view){
        mAuth.signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
}