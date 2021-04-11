package com.example.academica.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academica.Login;
import com.example.academica.R;
import com.google.firebase.auth.FirebaseAuth;

import com.google.android.material.navigation.NavigationView;

public class StudentAttendanceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "StudentAttendance";
    
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout ;
    private NavigationView navigationView;
    private final int personalisedAttendanceID = R.id.personalized_attendance,
    personalisedResultID = R.id.Personalized_result,
    profilePageID = R.id.profile_page,logoutID = R.id.logout;
    private StudentRegDataHelper currentUserData;

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
                R.string.drawerclose
        );


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //set data to nav headers
        currentUserData = (StudentRegDataHelper)getIntent().getSerializableExtra("UserData");   // retrieve user data object from home activity
        setNavData();   // private method to set nav header data : declared below

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case personalisedAttendanceID:
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(getApplicationContext(),StudentHomeActivity.class));
                break;
            case personalisedResultID:
                Toast.makeText(getApplicationContext(),"result",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case profilePageID:
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case logoutID:
                mAuth.signOut();
                startActivity(new Intent(StudentAttendanceActivity.this, Login.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
        }

        return true;
    }

    private void setNavData(){  // method to set user data in the navigationView
        // getting the navigation element's references
        View navHeaderView = navigationView.getHeaderView(0);
        TextView navHeaderUserName = navHeaderView.findViewById(R.id.nav_header_userName);
        TextView navHeaderEmail = navHeaderView.findViewById(R.id.nav_header_email);
        navHeaderUserName.setTextColor(Color.parseColor("#FFFFFF"));
        navHeaderUserName.setText(currentUserData.getFullName());
        navHeaderEmail.setTextColor(Color.parseColor("#FFFFFF"));
        navHeaderEmail.setText(currentUserData.getEmail());

    }
}