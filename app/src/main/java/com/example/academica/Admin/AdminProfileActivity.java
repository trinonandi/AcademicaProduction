package com.example.academica.Admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.academica.Login;
import com.example.academica.R;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class AdminProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;

    private AdminRegDataHelper currentUserData;
    private Dialog resetPwdDialog;
    private TextView nameTextView, emailTextView;

    private final int personalisedAttendanceID = R.id.personalized_home,
            personalisedResultID = R.id.Personalized_result,
            profilePageID = R.id.profile_page, logoutID = R.id.logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);


        toolbar = findViewById(R.id.admin_attendance_toolbar);
        drawerLayout = findViewById(R.id.admin_drawer_profile);
        navigationView = findViewById(R.id.admin_AllNav_menu);

        mAuth = FirebaseAuth.getInstance();


        nameTextView = findViewById(R.id.admin_profile_name_textView);
        emailTextView = findViewById(R.id.admin_profile_email_textView);



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
        currentUserData = (AdminRegDataHelper)getIntent().getSerializableExtra("userData");   // retrieve user data object from home activity
        setNavData();   // private method to set nav header data : declared below

        setDataInView(); // sets profile data

        /* reset password dialogue box setup START */
        resetPwdDialog = new Dialog(this);   // instantiates dialogue box
        resetPwdDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);   // sets the background to transparent


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case personalisedAttendanceID:
                startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);

                Toast.makeText(this,"Profile",Toast.LENGTH_LONG).show();
                break;

            case profilePageID:
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case logoutID:
                mAuth.signOut();
                startActivity(new Intent(AdminProfileActivity.this, Login.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
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

    private void setDataInView() {   // method to set profile data
        nameTextView.setText(currentUserData.getFullName());
        emailTextView.setText(currentUserData.getEmail());



    }

    public void updateProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), AdminProfileUpdateActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    public void resetPassword(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        resetPwdDialog.setContentView(R.layout.reset_password_dialog);
        TextInputLayout newPasswordLayout = resetPwdDialog.findViewById(R.id.reset_pwd_editText);
        AppCompatButton reset = resetPwdDialog.findViewById(R.id.reset_password_reset_btn);
        AppCompatButton close = resetPwdDialog.findViewById(R.id.reset_password_close_btn);

        close.setOnClickListener(v -> {
            resetPwdDialog.dismiss();    // closes the dialogue box
        });
        reset.setOnClickListener(v -> {
            String pwd = Objects.requireNonNull(newPasswordLayout.getEditText()).getText().toString();
            assert user != null;
            user.updatePassword(pwd).addOnSuccessListener(aVoid -> {
                Toast.makeText(AdminProfileActivity.this, "Password successfully updated", Toast.LENGTH_SHORT).show();
                resetPwdDialog.dismiss();
                mAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), Login.class));
            }).addOnFailureListener(e -> Toast.makeText(AdminProfileActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show());
        });

        resetPwdDialog.show();
    }
}