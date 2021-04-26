package com.example.academica.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academica.Login;
import com.example.academica.R;
import com.example.academica.SessionManagement;
import com.example.academica.Student.StudentRegDataHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AdminHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CardView createStudentsCard, createSubjectsCard, updateStudentsCard, updateSubjectsCard, attendanceCard;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private final int idProfilePage = R.id.profile_page, idLogOut = R.id.logout;    // makes the switch case ids final
    private FirebaseAuth mAuth;
    private DatabaseReference referenceDB;
    private RelativeLayout progressBarLayout;
    private AdminRegDataHelper currentUserData;

    SessionManagement sessionManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);


        progressBarLayout = findViewById(R.id.admin_home_progressBar_layout);
        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.admin_main_drawer);
        drawerLayout = findViewById(R.id.admin_drawer_layout);
        navigationView = findViewById(R.id.admin_allNav_menu);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.draweropen,
                R.string.drawerclose);


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        fetchUserData();

        // adding onClickListeners to the CardViews
        createStudentsCard = findViewById(R.id.admin_home_createadmins_cardView);
        createSubjectsCard = findViewById(R.id.admin_home_createSubjects_cardView);
        updateStudentsCard = findViewById(R.id.admin_home_updateadmins_cardView);
        updateSubjectsCard = findViewById(R.id.admin_home_updateSubjects_cardView);
        attendanceCard = findViewById(R.id.admin_home_attendance);
        createStudentsCard.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AdminCreateSessionStudentActivity.class);
            intent.putExtra("userData", currentUserData);
            startActivity(intent);
        });
        createSubjectsCard.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminCreateSessionSubjectActivity.class);
            intent.putExtra("userData", currentUserData);
            startActivity(intent);
        });
        updateStudentsCard.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminUpdateSessionStudentActivity.class);
            intent.putExtra("userData", currentUserData);
            startActivity(intent);
        });
        updateSubjectsCard.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminUpdateSessionSubjectActivity.class);
            intent.putExtra("userData", currentUserData);
            startActivity(intent);
        });
        attendanceCard.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminAttendanceActivity.class);
            intent.putExtra("userData", currentUserData);
            startActivity(intent);
        });


        sessionManagement = new SessionManagement(getApplicationContext());
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case idProfilePage:
                Intent intent = new Intent(getApplicationContext(), AdminProfileActivity.class);
                intent.putExtra("UserData", currentUserData);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(this,"Profile",Toast.LENGTH_LONG).show();
                break;
            case idLogOut:
                drawerLayout.closeDrawer(GravityCompat.START);
                doLogout();
                finish();
                break;

        }
        return true;
    }

    public void doLogout() {
        mAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), Login.class));
        sessionManagement.setLogin("login");
    }

//    public void showProfile(){
//        Intent intent = new Intent(getApplicationContext(),AdminProfileActivity.class);
//        intent.putExtra("UserData", currentUserData);
//        startActivity(intent);
//        Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
//    }

    private void fetchUserData() { // method to fetch data from firebase

        progressBarLayout.setVisibility(View.VISIBLE);

        // getting user data from firebase
        String dbKey = StudentRegDataHelper.generateKeyFromEmail(Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()));    // generates firebase user key
        referenceDB = FirebaseDatabase.getInstance().getReference("users").child(dbKey);
        referenceDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserData = snapshot.getValue(AdminRegDataHelper.class);    // user data object instantiated
                if (currentUserData == null) {
                    progressBarLayout.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "No data found corresponding to this user", Toast.LENGTH_LONG).show();
                    return;
                }
                setNavData();   // private method to set data on nav header elements
                progressBarLayout.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setNavData() {  // method to set user data in the navigationView
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