package com.example.academica;

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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class StudentProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout ;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private final int personalisedAttendanceID = R.id.personalized_attendance,
            personalisedResultID = R.id.Personalized_result,
            profilePageID = R.id.profile_page,logoutID = R.id.logout;
    private StudentRegDataHelper currentUserData;

    private TextView nameTextView, emailTextView, univRollTextView, classRollTextView, semTextView, deptTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        toolbar=findViewById(R.id.student_attendance_toolbar);
        drawerLayout = findViewById(R.id.student_drawer_attendance);
        navigationView  = findViewById(R.id.student_AllNav_menu);

        mAuth = FirebaseAuth.getInstance();

        nameTextView = findViewById(R.id.student_profile_name_textView);
        emailTextView = findViewById(R.id.student_profile_email_textView);
        univRollTextView = findViewById(R.id.student_profile_univRoll_textView);
        classRollTextView = findViewById(R.id.student_profile_classRoll_textView);
        semTextView = findViewById(R.id.student_profile_sem_textView);
        deptTextView = findViewById(R.id.student_profile_dept_textView);

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

        setDataInView(); // sets profile data

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
                Toast.makeText(getApplicationContext(),"profile result",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case profilePageID:
                Toast.makeText(getApplicationContext(),"profile page",Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case logoutID:
                mAuth.signOut();
                startActivity(new Intent(StudentProfileActivity.this,Login.class));
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

    private void setDataInView(){
        nameTextView.setText(currentUserData.getFullName());
        emailTextView.setText(currentUserData.getEmail());
        semTextView.setText(currentUserData.getSem());
        univRollTextView.setText(currentUserData.getUnivRoll());
        deptTextView.setText(currentUserData.getDept());
        classRollTextView.setText(currentUserData.getClassRoll());
    }
}