package com.example.academica.Teacher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.academica.R;
import com.example.academica.Student.StudentProfileActivity;
import com.example.academica.Student.StudentProfileUpdateActivity;
import com.example.academica.Student.StudentRegDataHelper;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class TeacherProfileUpdateActivity extends AppCompatActivity {

    private TeacherRegDataHelper currentUserData;
    private TextInputLayout nameLayout;
    private Button deptButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile_update);


        nameLayout = findViewById(R.id.teacher_profile_update_name);

        deptButton = findViewById(R.id.teacher_profile_update_dept);

        currentUserData = (TeacherRegDataHelper) getIntent().getSerializableExtra("userData");

        Objects.requireNonNull(nameLayout.getEditText()).setText(currentUserData.getFullName());

        deptButton.setText(currentUserData.getDept());

        deptButton.setOnClickListener(v -> {
            PopupMenu dept = new PopupMenu(this, deptButton, Gravity.CENTER);
            dept.getMenuInflater().inflate(R.menu.dept_menu, dept.getMenu());
            dept.setOnMenuItemClickListener(item -> {
                deptButton.setText(item.getTitle());
                return true;
            });

            dept.show();
        });
    }

    public void cancel(View view) {
        onBackPressed();
        finish();
    }

    public void updateDate(View view) {
        String name = Objects.requireNonNull(nameLayout.getEditText()).getText().toString().trim();

        String dept = deptButton.getText().toString().trim();


        currentUserData.setFullName(name);
        currentUserData.setDept(dept);

        String key = StudentRegDataHelper.generateKeyFromEmail(currentUserData.getEmail());
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(key).setValue(currentUserData).addOnSuccessListener(aVoid -> {
            Toast.makeText(TeacherProfileUpdateActivity.this, "Profile updated successfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), TeacherProfileActivity.class);
            intent.putExtra("UserData", currentUserData);
            startActivity(intent);
        }).addOnFailureListener(e -> Toast.makeText(TeacherProfileUpdateActivity.this, "Cannot update profile", Toast.LENGTH_SHORT).show());
    }
}