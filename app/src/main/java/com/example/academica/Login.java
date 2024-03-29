package com.example.academica;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;


import com.example.academica.Admin.AdminHomeActivity;
import com.example.academica.Registration.RegistrationActivity;
import com.example.academica.Student.StudentHomeActivity;
import com.example.academica.Student.StudentRegDataHelper;
import com.example.academica.Teacher.TeacherHomeActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class Login extends AppCompatActivity {


    private static final String TAG = "Login Page";
    private TextInputLayout emailEditText, pwdEditText;
    private AppCompatTextView forgotPwdTextView;
    private FirebaseAuth mAuth;
    private Dialog forgotPwdDialogue;   // forgot password dialogue box reference

    private CardView linearProgressIndicator;


    private SessionManagement sessionManagement;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.main_email_editText);
        pwdEditText = findViewById(R.id.main_pwd_editText);
        mAuth = FirebaseAuth.getInstance();

        /* forgot password dialogue box setup START */
        forgotPwdDialogue = new Dialog(this);   // instantiates dialogue box
        forgotPwdDialogue.getWindow().setBackgroundDrawableResource(android.R.color.transparent);   // sets the background to transparent
        forgotPwdTextView = findViewById(R.id.main_forgotPwd_textView);
        // as it is a TextView so setOnClickListener is required
        forgotPwdTextView.setOnClickListener(v -> {
            openForgetPwdDialogue(); // private method defined below
        });
        /* forgot password dialogue box setup END */

        linearProgressIndicator = findViewById(R.id.login_progressbar);


        sessionManagement = new SessionManagement(getApplicationContext());



    }





    public void startRegActivity(View view) {
        startActivity(new Intent(Login.this, RegistrationActivity.class));
    }


    public void doSignIn(View view) {

        String email = Objects.requireNonNull(emailEditText.getEditText()).getText().toString().trim();
        String pwd = Objects.requireNonNull(pwdEditText.getEditText()).getText().toString().trim();

        if (!email.isEmpty() && !pwd.isEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getApplicationContext(), "Invalid Email ID", Toast.LENGTH_SHORT).show();
                linearProgressIndicator.setVisibility(View.GONE);
                return;
            }
            if (pwd.length() < 6) {
                Toast.makeText(getApplicationContext(), "Password must be six digits", Toast.LENGTH_SHORT).show();
                linearProgressIndicator.setVisibility(View.GONE);
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Empty fields", Toast.LENGTH_SHORT).show();
            linearProgressIndicator.setVisibility(View.GONE);
            return;
        }


        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {


                    linearProgressIndicator.setVisibility(View.VISIBLE);
                    linearProgressIndicator.setBackgroundResource(android.R.color.transparent);



                    if (mAuth.getCurrentUser() == null) {
                        linearProgressIndicator.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Incorrect UserID or Password", Toast.LENGTH_SHORT).show();
                    } else if (emailVerificationStatus()) {

                        String key = StudentRegDataHelper.generateKeyFromEmail(email);
                        DatabaseReference accountData = FirebaseDatabase.getInstance().getReference("users").child(key).child("type");
                        accountData.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String accountType = snapshot.getValue(String.class);
                                Intent intent = new Intent(getApplicationContext(), StudentHomeActivity.class);
                                assert accountType != null;
                                switch (accountType) {
                                    case "STUDENT":

                                        intent = new Intent(getApplicationContext(), StudentHomeActivity.class);

                                        sessionManagement.setLogin("STUDENT");
                                        break;
                                    case "TEACHER":

                                        intent = new Intent(getApplicationContext(), TeacherHomeActivity.class);

                                        sessionManagement.setLogin("TEACHER");
                                        break;
                                    case "ADMIN":

                                        intent = new Intent(getApplicationContext(), AdminHomeActivity.class);

                                        sessionManagement.setLogin("ADMIN");
                                        break;
                                }
                                finish();
                                Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                                startActivity(intent);


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getApplicationContext(), "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                linearProgressIndicator.setVisibility(View.GONE);
                            }
                        });


                    } else {
                        Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                        linearProgressIndicator.setVisibility(View.GONE);
                    }

//                    circularProgressIndicator.setVisibility(View.GONE);
                });

        linearProgressIndicator.setVisibility(View.GONE);
        Objects.requireNonNull(emailEditText.getEditText()).setText("");
        Objects.requireNonNull(pwdEditText.getEditText()).setText("");
    }

    private boolean emailVerificationStatus() {
        boolean flag = false;
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            return false;
        }
        if (firebaseUser.isEmailVerified()) {
            flag = true;
        } else {
            Toast.makeText(getApplicationContext(), "Verify the email", Toast.LENGTH_SHORT).show();
            linearProgressIndicator.setVisibility(View.GONE);
            mAuth.signOut();
        }
        return flag;
    }

    private void openForgetPwdDialogue() { // method for complete forgot password feature
        forgotPwdDialogue.setContentView(R.layout.forgot_password_dialog); // setting the layout for the dialogue box
        EditText resetEmail = forgotPwdDialogue.findViewById(R.id.forgot_password_reset_email); // getting id reference through the dialogue reference
        AppCompatButton reset = forgotPwdDialogue.findViewById(R.id.forgot_password_reset_btn);
        AppCompatButton close = forgotPwdDialogue.findViewById(R.id.forgot_password_close_btn);


        // local onClickListener for convenience
        close.setOnClickListener(v -> {
            forgotPwdDialogue.dismiss();    // closes the dialogue box
        });

        reset.setOnClickListener(v -> {
            String email = resetEmail.getText().toString();
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                mAuth.sendPasswordResetEmail(email).addOnSuccessListener(aVoid -> Toast.makeText(Login.this, "Reset email sent successfully", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(Login.this, "Reset email cannot be sent: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                Toast.makeText(getApplicationContext(), "Invalid Email ID", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPwdDialogue.show();   // this method is mandatory to show the dialogue box
    }
}

// font - coveredByYourGrace