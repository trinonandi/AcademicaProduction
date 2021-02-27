package com.example.academica;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "StudentHomeActivity";

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private DatabaseReference referenceDB;
    private StudentRegDataHelper currentUserData;
    private RelativeLayout progressBarLayout;
    private CardView cardView1;
   // private TextView navUserName;

    private final int idProfilePage = R.id.profile_page, idLogOut = R.id.logout;    // makes the switch case ids final

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        progressBarLayout = findViewById(R.id.student_home_progressBar_layout);

        mAuth = FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.student_main_drawer);
        drawerLayout = findViewById(R.id.student_drawer_layout);
        navigationView  = findViewById(R.id.student_Nav_menu);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.draweropen,
                R.string.drawerclose);


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        fetchUserData();

        cardView1 = findViewById(R.id.student_home_cardView1);
        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttendance(v);
            }
        });

    }

    @Override
    public void onBackPressed() {

    }


    public void doLogout(){
        mAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(),Login.class));
    }

    public void showProfile(){
        Intent intent = new Intent(getApplicationContext(),StudentProfileActivity.class);
        intent.putExtra("UserData", currentUserData);
        startActivity(intent);
    }

    private void showAttendance(View view){
        Intent intent = new Intent(getApplicationContext(), StudentAttendanceActivity.class);
        intent.putExtra("UserData", currentUserData);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case idProfilePage:
                showProfile();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case idLogOut:
                drawerLayout.closeDrawer(GravityCompat.START);
                doLogout();
                break;

        }
        return true;
    }

    private void fetchUserData(){ // method to fetch data from firebase

        progressBarLayout.setVisibility(View.VISIBLE);

        // getting user data from firebase
        String dbKey = StudentRegDataHelper.generateKeyFromEmail(mAuth.getCurrentUser().getEmail());    // generates firebase user key
        referenceDB = FirebaseDatabase.getInstance().getReference("users").child("students").child(dbKey);
        referenceDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserData = snapshot.getValue(StudentRegDataHelper.class);    // user data object instantiated
                if(currentUserData == null){
                    progressBarLayout.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "No data found corresponding to this user", Toast.LENGTH_LONG).show();
                    return;
                }
                setNavData();   // private method to set data on nav header elements
                progressBarLayout.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentHomeActivity.this, "Error : "+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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