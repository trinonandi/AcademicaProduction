package com.example.academica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.android.material.navigation.NavigationView;

public class StudentAttendance extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;

    
    private Toolbar toolbar; DrawerLayout drawerLayout ;NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);


        toolbar=findViewById(R.id.student_attendance_toolbar);
        drawerLayout = findViewById(R.id.student_drawer_attendance);
        navigationView  = findViewById(R.id.student_AllNav_menu);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.draweropen,
                R.string.drawerclose);


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.personalized_attendance:

                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.Personalized_result:
                Toast.makeText(getApplicationContext(),"profile result",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.profile_page:
                Toast.makeText(getApplicationContext(),"profile page",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(StudentAttendance.this,MainActivity.class));

                drawerLayout.closeDrawer(GravityCompat.START);
                break;
        }

        return true;
    }
}