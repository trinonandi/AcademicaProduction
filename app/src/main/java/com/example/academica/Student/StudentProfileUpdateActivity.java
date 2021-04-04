package com.example.academica.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.academica.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class StudentProfileUpdateActivity extends AppCompatActivity {
    private StudentRegDataHelper currentUserData;
    private TextInputLayout nameLayout, univRollLayout, classRollLayout, semLayout;
    private Button deptButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile_update);

        nameLayout = findViewById(R.id.student_profile_update_name);
        univRollLayout = findViewById(R.id.student_profile_update_univRoll);
        classRollLayout = findViewById(R.id.student_profile_update_classRoll);
        semLayout = findViewById(R.id.student_profile_update_sem);
        deptButton = findViewById(R.id.student_profile_update_dept);


        currentUserData = (StudentRegDataHelper)getIntent().getSerializableExtra("UserData");   // retrieve user data object from home activity

        // displaying the existing user data
        Objects.requireNonNull(nameLayout.getEditText()).setText(currentUserData.getFullName());
        Objects.requireNonNull(univRollLayout.getEditText()).setText(currentUserData.getUnivRoll());
        Objects.requireNonNull(classRollLayout.getEditText()).setText(currentUserData.getClassRoll());
        Objects.requireNonNull(semLayout.getEditText()).setText(currentUserData.getSem());
        deptButton.setText(currentUserData.getDept());

        // code to popup Window for Department Selection
        deptButton.setOnClickListener(v -> {
            PopupMenu dept = new PopupMenu(this,deptButton, Gravity.CENTER);
            dept.getMenuInflater().inflate(R.menu.dept_menu,dept.getMenu());
            dept.setOnMenuItemClickListener(item -> {
                deptButton.setText(item.getTitle());
                return true;
            });

            dept.show();
        });
    }

    public void cancel(View view){
        onBackPressed();
        finish();
    }

    public void updateDate(View view){
        String name = Objects.requireNonNull(nameLayout.getEditText()).getText().toString().trim();
        String univRoll = Objects.requireNonNull(univRollLayout.getEditText()).getText().toString().trim();
        String classRoll = Objects.requireNonNull(classRollLayout.getEditText()).getText().toString().trim();
        String sem = Objects.requireNonNull(semLayout.getEditText()).getText().toString().trim();
        String dept = deptButton.getText().toString().trim();

        currentUserData.setClassRoll(classRoll);
        currentUserData.setFullName(name);
        currentUserData.setUnivRoll(univRoll);
        currentUserData.setSem(sem);
        currentUserData.setDept(dept);

        String key = StudentRegDataHelper.generateKeyFromEmail(currentUserData.getEmail());
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(key).setValue(currentUserData).addOnSuccessListener(aVoid -> {
            Toast.makeText(StudentProfileUpdateActivity.this, "Profile updated successfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),StudentProfileActivity.class);
            intent.putExtra("UserData", currentUserData);
            startActivity(intent);
        }).addOnFailureListener(e -> Toast.makeText(StudentProfileUpdateActivity.this, "Cannot update profile", Toast.LENGTH_SHORT).show());
    }
}