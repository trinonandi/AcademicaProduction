package com.example.academica.Student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.academica.Login;
import com.example.academica.R;
import com.example.academica.Teacher.TeacherProfileActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentAttendanceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "StudentAttendance";

    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private final int personalisedAttendanceID = R.id.personalized_attendance,
            personalisedResultID = R.id.Personalized_result,
            profilePageID = R.id.profile_page, logoutID = R.id.logout;
    private StudentRegDataHelper currentUserData;
    private RelativeLayout progressBarLayout;
    private Map<String, Long> userAttendanceMap, totalAttendanceMap;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);


        toolbar = findViewById(R.id.student_attendance_toolbar);
        drawerLayout = findViewById(R.id.student_drawer_attendance);
        navigationView = findViewById(R.id.student_AllNav_menu);
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
        currentUserData = (StudentRegDataHelper) getIntent().getSerializableExtra("UserData");   // retrieve user data object from home activity
        setNavData();   // private method to set nav header data : declared below

        mAuth = FirebaseAuth.getInstance();
        //  progressBarLayout = findViewById(R.id.student_attendance_progressBar_layout);
        userAttendanceMap = new HashMap<>();
        totalAttendanceMap = new HashMap<>();


        fetchAttendanceData();

        studentChart();

    }

    void studentChart() {
        ArrayList<String> labels = new ArrayList<>(userAttendanceMap.keySet());
        ArrayList<Long> percent = new ArrayList<>();
        ArrayList<Long> uval = new ArrayList<>(userAttendanceMap.values());
        ArrayList<Long> tval = new ArrayList<>(totalAttendanceMap.values());


        for (int i = 0; i < userAttendanceMap.size(); i++) {


            percent.add(uval.get(i) * 100 / tval.get(i));
        }
        ArrayList<PieEntry> studentData = new ArrayList<>();

        for (int i = 0; i < userAttendanceMap.size(); i++) {
            studentData.add(new PieEntry(percent.get(i), labels.get(i)));
        }

        PieChart studentChart = findViewById(R.id.student_attendance_pieChart);
        PieDataSet pieDataSet = new PieDataSet(studentData, "Student Attendance");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(16f);
        PieData pieData = new PieData(pieDataSet);

        studentChart.setData(pieData);
        studentChart.getDescription().setEnabled(false);
        studentChart.setCenterText("Attendance");
        studentChart.animate();
    }

    public void showProfile(){
        Intent intent = new Intent(getApplicationContext(), StudentProfileActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case personalisedAttendanceID:
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(getApplicationContext(), StudentHomeActivity.class));
                break;
            case personalisedResultID:
                Toast.makeText(getApplicationContext(), "result", Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case profilePageID:
                showProfile();
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

    private void fetchAttendanceData() {
        // progressBarLayout.setVisibility(View.VISIBLE);
        String roll = currentUserData.getClassRoll();
        String sem = currentUserData.getSem();
        String dept = currentUserData.getDept();


        DatabaseReference userAttendanceReference = FirebaseDatabase.getInstance().getReference("attendance").child(currentUserData.getDept()).child(currentUserData.getSem()).child(currentUserData.getClassRoll());
        DatabaseReference totalAttendanceReference = FirebaseDatabase.getInstance().getReference("attendance").child(currentUserData.getDept()).child(currentUserData.getSem()).child("total");

        totalAttendanceReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    Toast.makeText(StudentAttendanceActivity.this, "Data Not Found", Toast.LENGTH_SHORT).show();

                    return;
                }


                for (DataSnapshot item : snapshot.getChildren()) {
                    String code = item.getKey();
                    Long attendanceValue = (Long) item.getValue();
                    totalAttendanceMap.put(code, attendanceValue);
                }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();

            }
        });
        userAttendanceReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    Toast.makeText(StudentAttendanceActivity.this, "Data Not Found humba", Toast.LENGTH_SHORT).show();
                    // progressBarLayout.setVisibility(View.GONE);
                    return;
                }


                for (DataSnapshot item : snapshot.getChildren()) {
                    String code = item.getKey();

                    Long attendanceValue = (Long) item.getValue();
                    userAttendanceMap.put(code, attendanceValue);

                }


                studentChart();
                //  progressBarLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
                //  progressBarLayout.setVisibility(View.GONE);
            }


        });



    }
}