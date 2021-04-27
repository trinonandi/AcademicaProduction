package com.example.academica.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academica.Login;
import com.example.academica.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class AdminAttendanceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "ATTENDANCE" ;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private final int idProfilePage = R.id.profile_page, idLogOut = R.id.logout;    // makes the switch case ids final
    private AdminRegDataHelper currentUserData;
    private DatabaseReference studentReference, subjectReference, attendanceReference;
    private TreeMap<String, String> studentMap, subjectMap;
    private ArrayList<String> newAttendanceRollList;
    private TextView infoTextView;
    private Button closeButton, nextButton, semButton, deptButton;
    private RelativeLayout progressBarLayout;
    private String sem, dept;
    private ArrayList<RecyclerItem> recyclerItemsArrayList;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private RecyclerView recyclerView;
    private Dialog addItemDialog;
    // a flag to check if the attendance DB is purely new. If true the add a total field
    private boolean completelyNewSession = false;
    // a flag to check if removal of attendance data is required
    private boolean removeFlag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_attendance);

        currentUserData = (AdminRegDataHelper)getIntent().getSerializableExtra("userData");
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
        setNavData();

        infoTextView = findViewById(R.id.admin_attendance_info_textView);
        progressBarLayout = findViewById(R.id.admin_attendance_progressBar_layout);
        semButton = findViewById(R.id.admin_attendance_semBtn);
        semButton.setOnClickListener(v -> {
            PopupMenu sem = new PopupMenu(this, semButton, Gravity.CENTER);
            sem.getMenuInflater().inflate(R.menu.sem_menu,sem.getMenu());
            sem.setOnMenuItemClickListener(item -> {
                semButton.setText(item.getTitle());
                return true;
            });

            sem.show();
        });

        deptButton = findViewById(R.id.admin_attendance_deptBtn);
        deptButton.setOnClickListener(v -> {
            PopupMenu dept = new PopupMenu(this, deptButton, Gravity.CENTER);
            dept.getMenuInflater().inflate(R.menu.dept_menu,dept.getMenu());
            dept.setOnMenuItemClickListener(item -> {
                deptButton.setText(item.getTitle());
                return true;
            });

            dept.show();
        });
        sem = semButton.getText().toString();
        dept = deptButton.getText().toString();

        closeButton = findViewById(R.id.admin_attendance_closeBtn);
        closeButton.setOnClickListener(v -> onBackPressed());

        nextButton = findViewById(R.id.admin_attendance_nextBtn);
        nextButton.setOnClickListener(v -> updateAttendanceDatabase());
        studentMap = new TreeMap<>();
        subjectMap = new TreeMap<>();
        newAttendanceRollList = new ArrayList<>();

        // initiating dialog box for data input
        addItemDialog = new Dialog(this);
        addItemDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Recycler view implementation code
        recyclerItemsArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.admin_attendance_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerAdapter = new RecyclerAdapter(recyclerItemsArrayList);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }

//    private void submitAttendanceData(){
//
//        attendanceReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.getValue() == null){
//                    // create a completely new attendance database
//
//                    // making a new hash map with key as subject code and initial attendance(value) as 0
//                    HashMap<String, Integer> subjectItemMap = new HashMap<>();
//                    for(String code : subjectMap.keySet()){
//                        subjectItemMap.put(code, 0);
//                    }
//
//                    // setting the initial attendance hash map for all the students
//                    for(String roll : studentMap.keySet()){
//                        attendanceReference.child(roll).setValue(subjectItemMap);
//                    }
//
//                }
//                else{
//                    // attendance is already created
//                    ArrayList<String> currentAttendanceRollList = new ArrayList<>();
//                    for(DataSnapshot attendanceItem : snapshot.getChildren()){
//                        currentAttendanceRollList.add(attendanceItem.getKey());
//                    }
//                    if(currentAttendanceRollList.size() == studentMap.size()){
//                        // no new students are found in student database
//                        Toast.makeText(AdminAttendanceActivity.this, "All students are present in attendance database", Toast.LENGTH_SHORT).show();
//                    }
//                    else{
//                        // will add only the new students found in student database
//
//                        Collections.sort(currentAttendanceRollList);
//                        // making a new hash map with key as subject code and initial attendance(value) as 0
//                        HashMap<String, Integer> subjectItemMap = new HashMap<>();
//                        for(String code : subjectMap.keySet()){
//                            subjectItemMap.put(code, 0);
//                        }
//
//                        // setting the initial attendance hash map for new students only
//                        for(String studentRoll : studentMap.keySet()){
//                            if(!currentAttendanceRollList.contains(studentRoll)){
//                                attendanceReference.child(studentRoll).setValue(subjectItemMap);
//                            }
//                        }
//                        Toast.makeText(AdminAttendanceActivity.this, "New students added successfully", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(AdminAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }

    @Override
    public void onBackPressed() {
        addItemDialog.setContentView(R.layout.instructions_dialog);
        addItemDialog.show();
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);

        messageView.setText("Do you want to close? Any unsaved change will be discarded. Press YES to close NO to go back");
        noBtn.setOnClickListener(v -> addItemDialog.dismiss());
        yesBtn.setOnClickListener(v ->{
            addItemDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
            finish();
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
            case R.id.admin_AllNav_Attendance:
                drawerLayout.closeDrawer(GravityCompat.START);

                break;
            case R.id.admin_AllNav_createStudent:
                drawerLayout.closeDrawer(GravityCompat.START);
                showCreateStudent();
                break;
            case R.id.admin_AllNav_createSubject:
                drawerLayout.closeDrawer(GravityCompat.START);
                showCreateSubject();
                break;
            case R.id.admin_AllNav_UpdateStudent:
                drawerLayout.closeDrawer(GravityCompat.START);
                showUpdateStudent();
                break;
            case R.id.admin_AllNav_UpdateSubject:
                drawerLayout.closeDrawer(GravityCompat.START);
                showUpdateSubject();
                break;
            case R.id.personalized_home:
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(getApplicationContext(),AdminHomeActivity.class));
                break;

        }
        return true;
    }

    public void doLogout(){
        mAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), Login.class));
    }

    public void showProfile(){
        Intent intent = new Intent(getApplicationContext(), AdminProfileActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    public void showAttendance() {
        Intent intent = new Intent(getApplicationContext(), AdminAttendanceActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    public void showCreateStudent()
    {
        Intent intent = new Intent(getApplicationContext(), AdminCreateSessionStudentActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }
    public void showCreateSubject(){
        Intent intent = new Intent(getApplicationContext(), AdminCreateSessionSubjectActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    public void showUpdateStudent(){
        Intent intent = new Intent(getApplicationContext(), AdminUpdateSessionStudentActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }
    public void showUpdateSubject(){
        Intent intent = new Intent(getApplicationContext(), AdminUpdateSessionSubjectActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }


    public void searchData(View view) {
        // method to search student data
        progressBarLayout.setVisibility(View.VISIBLE);
        dept = deptButton.getText().toString();
        sem = semButton.getText().toString();
        String semNumber = ""+sem.charAt(0);
        if(dept.toLowerCase().equals("dept") || sem.toLowerCase().equals("sem")){
            Toast.makeText(this, "Choose department and semester", Toast.LENGTH_SHORT).show();
            progressBarLayout.setVisibility(View.GONE);
            return;
        }
        infoTextView.setVisibility(View.VISIBLE);
        studentReference = FirebaseDatabase.getInstance()
                .getReference("sessions").child(dept).child("students").child(semNumber);

        subjectReference = FirebaseDatabase.getInstance()
                .getReference("sessions").child(dept).child("subjects").child(semNumber);

        attendanceReference = FirebaseDatabase.getInstance()
                .getReference("attendance").child(dept).child(semNumber);

        studentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    Toast.makeText(AdminAttendanceActivity.this, "Student data not found", Toast.LENGTH_SHORT).show();
                    progressBarLayout.setVisibility(View.GONE);
                    return;
                }
                for (DataSnapshot item : snapshot.getChildren()) {
                    String key = item.getKey();
                    String val = (String)item.getValue();
                    studentMap.put(key, val);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
                progressBarLayout.setVisibility(View.GONE);
            }
        });

        subjectReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    Toast.makeText(AdminAttendanceActivity.this, "Subject data not found", Toast.LENGTH_SHORT).show();
                    progressBarLayout.setVisibility(View.GONE);
                    return;
                }
                for (DataSnapshot item : snapshot.getChildren()) {
                    String key = item.getKey();
                    String val = (String) item.getValue();
                    subjectMap.put(key, val);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
                progressBarLayout.setVisibility(View.GONE);
            }
        });

        fetchAttendanceData();  // will match attendance data with student data

    }

    private void fetchAttendanceData(){
        // method to fetch attendance data and match with student data
        attendanceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    // completely new session. No attendance DB exists
                    completelyNewSession = true;
                    for(String roll : studentMap.keySet()){
                        newAttendanceRollList.add(roll);    // keeping track of all the roll to make entries
                        RecyclerItem item = new RecyclerItem(roll, studentMap.get(roll));
                        recyclerItemsArrayList.add(item);
                    }
                    recyclerAdapter = new RecyclerAdapter(recyclerItemsArrayList);
                    recyclerView.setAdapter(recyclerAdapter);
                }
                else{
                    // attendance DB already exists
                    ArrayList<String> currentAttendanceRollList = new ArrayList<>();
                    for(DataSnapshot attendanceItem : snapshot.getChildren()){
                        currentAttendanceRollList.add(attendanceItem.getKey());
                    }
                    if(currentAttendanceRollList.size() - 1 == studentMap.size()){
                        // we are performing size - 1 for the "total" field
                        // no new students are found that does not exist in the attendance DB
                        infoTextView.setText("No new student found. All existing students are present in attendance database");
                    }
                    else if(currentAttendanceRollList.size() > studentMap.size()){
                        // students deleted from database but attendance still remains
                        Collections.sort(currentAttendanceRollList);
                        removeFlag = true;
                        infoTextView.setText("The following attendance sessions will be removed :");
                        for(String attendanceRoll : currentAttendanceRollList){
                            if(!studentMap.containsKey(attendanceRoll) && !attendanceRoll.equals("total")){
                                newAttendanceRollList.add(attendanceRoll);
                                RecyclerItem item = new RecyclerItem("Roll No:", attendanceRoll);
                                recyclerItemsArrayList.add(item);
                            }
                        }
                        recyclerAdapter = new RecyclerAdapter(recyclerItemsArrayList);
                        recyclerView.setAdapter(recyclerAdapter);
                    }
                    else{
                        // new students are found that are on student DB but not on attendance DB
                        Collections.sort(currentAttendanceRollList);
                        recyclerItemsArrayList = new ArrayList<>();
                        for(String studentRoll : studentMap.keySet()){
                            if(!currentAttendanceRollList.contains(studentRoll)){
                                newAttendanceRollList.add(studentRoll);     // keeping track of the new roll
                                RecyclerItem item = new RecyclerItem(studentRoll, studentMap.get(studentRoll));
                                recyclerItemsArrayList.add(item);
                            }
                        }
                        recyclerAdapter = new RecyclerAdapter(recyclerItemsArrayList);
                        recyclerView.setAdapter(recyclerAdapter);
                    }
                }
                progressBarLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
                progressBarLayout.setVisibility(View.GONE);
            }
        });
    }

    private void updateAttendanceDatabase(){
        addItemDialog.setContentView(R.layout.instructions_dialog);
        addItemDialog.show();
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);

        messageView.setText("Confirm update of attendance data. Press YES to update database NO to go back");
        noBtn.setOnClickListener(v -> addItemDialog.dismiss());
        yesBtn.setOnClickListener(v -> {
            progressBarLayout.setVisibility(View.VISIBLE);
            if(newAttendanceRollList.size() == 0){
                // no need to make any change in attendance DB
                Toast.makeText(this, "All students are present in attendance database", Toast.LENGTH_SHORT).show();
                progressBarLayout.setVisibility(View.GONE);
                return;
            }
            if(removeFlag){
                // we need to remove from attendance instead of adding
                for(String roll : newAttendanceRollList){
                    attendanceReference.child(roll).setValue(null);
                }
            }
            else{
                // add all the roll present in newAttendanceRollList
                // setting up initial subject map
                HashMap<String, Integer> subjectItemMap = new HashMap<>();
                for(String code : subjectMap.keySet()){
                    subjectItemMap.put(code, 0);
                }

                // setting the subject map under all roll numbers
                for(String roll : newAttendanceRollList){
                    attendanceReference.child(roll).setValue(subjectItemMap);
                }

                // if completely new session then we need to add total field
                if(completelyNewSession){
                    attendanceReference.child("total").setValue(subjectItemMap);
                }
            }

            Toast.makeText(this, "Attendance database updated successfully", Toast.LENGTH_SHORT).show();
            progressBarLayout.setVisibility(View.GONE);
            addItemDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
        });

    }
}
