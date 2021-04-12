package com.example.academica.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class AdminProfileUpdateActivity extends AppCompatActivity {

    private AdminRegDataHelper currentUserData;
    private TextInputLayout nameLayout;
    private Button deptButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_update);

        nameLayout = findViewById(R.id.admin_profile_update_name);
        currentUserData = (AdminRegDataHelper) getIntent().getSerializableExtra("UserData");
        Objects.requireNonNull(nameLayout.getEditText()).setText(currentUserData.getFullName());
    }

    public void cancel(View view) {
        onBackPressed();
        finish();
    }

    public void updateDate(View view) {
        String name = Objects.requireNonNull(nameLayout.getEditText()).getText().toString().trim();


        currentUserData.setFullName(name);


        String key = StudentRegDataHelper.generateKeyFromEmail(currentUserData.getEmail());
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(key).setValue(currentUserData).addOnSuccessListener(aVoid -> {
            Toast.makeText(AdminProfileUpdateActivity.this, "Profile updated successfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), StudentProfileActivity.class);
            intent.putExtra("UserData", currentUserData);
            startActivity(intent);
        }).addOnFailureListener(e -> Toast.makeText(AdminProfileUpdateActivity.this, "Cannot update profile", Toast.LENGTH_SHORT).show());

    }
}