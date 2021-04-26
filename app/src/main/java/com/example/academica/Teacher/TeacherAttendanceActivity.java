package com.example.academica.Teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;


public class TeacherAttendanceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "ATTENDANCE";
    private Dialog addItemDialog;
    private Button semButton, deptButton, closeButton, searchButton, updateButton, subjectButton;
    private RelativeLayout progressBarLayout;
    private LinearLayout subjectLayout;
    private String sem ,dept;
    private DatabaseReference attendanceReference, studentReference, subjectReference;
    private HashMap<String, Long> fetchedAttendanceMap = new HashMap<>();
    private HashMap<String, String> fetchedStudentMap = new HashMap<>();
    private DataSnapshot fetchedAttendanceData;
    private final ArrayList<String> subjectCodeList = new ArrayList<>();
    private boolean searchSuccessfulFlag = false;

    private HashSet<String> absentSet = new HashSet<>();
    private ListView listView;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> listViewItemList;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    TeacherRegDataHelper currentUserData;
    private final int idProfilePage = R.id.profile_page, idLogOut = R.id.logout;    // makes the switch case ids final

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_attendance);

        currentUserData = (TeacherRegDataHelper) getIntent().getSerializableExtra("userData");
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

        progressBarLayout = findViewById(R.id.teacher_attendance_progressBar_layout);
        subjectLayout = findViewById(R.id.teacher_attendance_subjectLayout);
        subjectButton = findViewById(R.id.teacher_attendance_subjectBtn);
        semButton = findViewById(R.id.teacher_attendance_semBtn);
        deptButton = findViewById(R.id.teacher_attendance_deptBtn);
        closeButton = findViewById(R.id.teacher_attendance_closeBtn);
        searchButton = findViewById(R.id.teacher_attendance_searchBtn);
        updateButton = findViewById(R.id.teacher_attendance_updateBtn);



        deptButton.setOnClickListener(v -> {
            PopupMenu dept = new PopupMenu(this,deptButton, Gravity.CENTER);
            dept.getMenuInflater().inflate(R.menu.dept_menu,dept.getMenu());
            dept.setOnMenuItemClickListener(item -> {
                deptButton.setText(item.getTitle());
                return true;
            });

            dept.show();
        });
        semButton.setOnClickListener(v -> {
            PopupMenu sem = new PopupMenu(this,semButton, Gravity.CENTER);
            sem.getMenuInflater().inflate(R.menu.sem_menu,sem.getMenu());
            sem.setOnMenuItemClickListener(item -> {
                semButton.setText(item.getTitle());
                return true;
            });

            sem.show();
        });
        searchButton.setOnClickListener(v -> searchData());
        closeButton.setOnClickListener(v -> onBackPressed());
        updateButton.setOnClickListener(v -> updateAttendance());
        dept = deptButton.getText().toString();
        sem = semButton.getText().toString();

        // initiating dialog box for data input
        addItemDialog = new Dialog(this);
        addItemDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        // list view implementation
        listViewItemList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, listViewItemList);
        listView = findViewById(R.id.teacher_attendance_recyclerView);
        listView.setAdapter(listAdapter);
    }

    @Override
    public void onBackPressed() {
        addItemDialog.setContentView(R.layout.instructions_dialog);
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);

        messageView.setText("Do you want to close? Any unsaved change will be discarded. Press YES to close NO to go back");
        noBtn.setOnClickListener(v -> addItemDialog.dismiss());
        yesBtn.setOnClickListener(v ->{
            addItemDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), TeacherHomeActivity.class));
            finish();
        });
        addItemDialog.show();
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
            case R.id.personalized_home:
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(TeacherAttendanceActivity.this,TeacherHomeActivity.class));
                finish();
                break;
            case idProfilePage:
                showProfile();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case idLogOut:
                drawerLayout.closeDrawer(GravityCompat.START);
                doLogout();
                break;
            case R.id.teacher_AllNav_attendance:

                drawerLayout.closeDrawer(GravityCompat.START);
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
        Intent intent = new Intent(getApplicationContext(), TeacherProfileActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    private void searchData() {
        final boolean[] studentFoundFlag = new boolean[1];
        final boolean[] subjectFoundFlag = new boolean[1];
//        final boolean[] attendanceFoundFlag = new boolean[1];
        progressBarLayout.setVisibility(View.VISIBLE);
        dept = deptButton.getText().toString();
        sem = semButton.getText().toString();
        String semNumber = ""+sem.charAt(0);
        if(dept.toLowerCase().equals("dept") || sem.toLowerCase().equals("sem")){
            Toast.makeText(this, "Choose department and semester", Toast.LENGTH_SHORT).show();
            progressBarLayout.setVisibility(View.GONE);
            return;
        }
        attendanceReference = FirebaseDatabase.getInstance().getReference("attendance").child(dept).child(semNumber);
        studentReference = FirebaseDatabase.getInstance().getReference("sessions").child(dept).child("students").child(semNumber);
        subjectReference = FirebaseDatabase.getInstance().getReference("sessions").child(dept).child("subjects").child(semNumber);

        studentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    Toast.makeText(TeacherAttendanceActivity.this, "Student Data Not Found", Toast.LENGTH_SHORT).show();
                    progressBarLayout.setVisibility(View.GONE);
                    return;
                }
                // getting an iterable getValues() object
                for (DataSnapshot item : snapshot.getChildren()) {
                    String key = item.getKey();
                    String val = (String) item.getValue();
                    fetchedStudentMap.put(key, val);
                    listViewItemList.add(key + "   " + val);    // separated by 3 spaces
                }
                if(listViewItemList.size() == 0){
                    // database created but no student added
                    Toast.makeText(getApplicationContext(), "Data found with zero entries", Toast.LENGTH_LONG).show();
                }

                studentFoundFlag[0] = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        subjectReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    Toast.makeText(TeacherAttendanceActivity.this, "Subject Data Not Found", Toast.LENGTH_SHORT).show();
                    progressBarLayout.setVisibility(View.GONE);
                    return;
                }
                subjectLayout.setVisibility(View.VISIBLE);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String code = dataSnapshot.getKey();
                    subjectCodeList.add(code);
                }
                subjectFoundFlag[0] = true;
                setSubjects();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        attendanceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    Toast.makeText(TeacherAttendanceActivity.this, "Attendance Data Not Found", Toast.LENGTH_SHORT).show();
                    progressBarLayout.setVisibility(View.GONE);
                    return;
                }

                fetchedAttendanceData = snapshot;
                // setting all the selection buttons to empty clickListener if all the data are found
                searchSuccessfulFlag = true;
                if(studentFoundFlag[0] && subjectFoundFlag[0]){
                    semButton.setOnClickListener(v -> {});
                    deptButton.setOnClickListener(v -> {});
                    searchButton.setOnClickListener(v -> {});
                }
                progressBarLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherAttendanceActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setSubjects(){     // sets the subject data on the pop up menu to choose from
        PopupMenu subjectMenu = new PopupMenu(this, subjectButton, Gravity.CENTER);
        for(String code : subjectCodeList){
            subjectMenu.getMenu().add(code);
        }
        subjectButton.setOnClickListener(v -> {
            subjectMenu.setOnMenuItemClickListener(item -> {
                subjectButton.setText(item.getTitle());
                return true;
            });
            subjectMenu.show();
        });
    }

    private void updateAttendance(){
        if(!searchSuccessfulFlag){
            Toast.makeText(this, "Search for an attendance session", Toast.LENGTH_SHORT).show();
            return;
        }
        if(subjectButton.getText().toString().toLowerCase().equals("subject")){
            Toast.makeText(this, "Select a subject code", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBarLayout.setVisibility(View.VISIBLE);
        String subjectCode = subjectButton.getText().toString();
        addItemDialog.setContentView(R.layout.instructions_dialog);
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);
        messageView.setText("Do you want to update attendance of " + subjectCode + " ? Press YES to update NO to go back");
        noBtn.setOnClickListener(v -> {
            addItemDialog.dismiss();
            progressBarLayout.setVisibility(View.GONE);
        });
        yesBtn.setOnClickListener(v ->{
            // making an attendance map for the database  fetched data
            for(DataSnapshot item : fetchedAttendanceData.getChildren()){
                String key = item.getKey();
                for(DataSnapshot innerItem : item.getChildren()){
                    String code = innerItem.getKey();
                    assert code != null;
                    if(code.equals(subjectCode)){
                        fetchedAttendanceMap.put(key, (Long) innerItem.getValue());
                    }
                }
            }

            // retrieving the absent rolls from ListView
            SparseBooleanArray checkedArray = listView.getCheckedItemPositions();
            for(int i=0;i<listView.getCount(); i++){
                if(!checkedArray.get(i)){
                    String[] arr = listViewItemList.get(i).split(" {3}");   //
                    absentSet.add(arr[0]);
                }
            }

            // setting date key adn reference for absent database
            String semNumber = sem.charAt(0) + "";
            HashMap<String, String> absentMap = new HashMap<>();
            Date c = Calendar.getInstance().getTime();
            System.out.println("Current time => " + c);
            SimpleDateFormat df = new SimpleDateFormat("dd,MMM,yyyy", Locale.getDefault());
            String formattedDate = df.format(c);
            DatabaseReference absentsReference = FirebaseDatabase.getInstance().getReference("absents").child(dept).child(semNumber).child(formattedDate);

            // increasing attendance of present rolls and total field
            // also storing the values to database
            for(String roll : fetchedAttendanceMap.keySet()){
                if(!absentSet.contains(roll)){
                    Long currentValue = fetchedAttendanceMap.get(roll);
                    assert currentValue != null;
                    fetchedAttendanceMap.put(roll , currentValue + 1);

                    attendanceReference.child(roll).child(subjectCode).setValue(fetchedAttendanceMap.get(roll));
                }
                else{
                    absentMap.put(roll, fetchedStudentMap.get(roll));
                }
            }
            absentsReference.child(subjectCode).setValue(absentMap);
            addItemDialog.dismiss();
            progressBarLayout.setVisibility(View.GONE);
            Toast.makeText(this, "Attendance Successfully updated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), TeacherHomeActivity.class));
            finish();
        });
        addItemDialog.show();


    }


}

