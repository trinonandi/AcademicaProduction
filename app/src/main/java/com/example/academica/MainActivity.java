package com.example.academica;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText emailEditText, pwdEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.main_email_editText);
        pwdEditText = findViewById(R.id.main_pwd_editText);
        mAuth = FirebaseAuth.getInstance();

    }

    public void startRegActivity(View view) {
        startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
    }

    public void doSignIn(View view){
        String email = emailEditText.getText().toString();
        String pwd = pwdEditText.getText().toString();

        if(!email.isEmpty() && !pwd.isEmpty()){
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(getApplicationContext(), "Invalid Email ID", Toast.LENGTH_SHORT).show();
                return;
            }
            if(pwd.length()<6){
                Toast.makeText(getApplicationContext(), "Password must be six digits", Toast.LENGTH_SHORT).show();
                return;
            }
        } else{
            Toast.makeText(getApplicationContext(), "Empty fields", Toast.LENGTH_SHORT).show();
            return;
        }


        mAuth.signInWithEmailAndPassword(email,pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(emailVerificationStatus()){
                            Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),StudentHome.class);
                            startActivity(intent);
                        } else{
                            Toast.makeText(getApplicationContext(),"User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        emailEditText.setText(null);
        pwdEditText.setText(null);
    }


    private boolean emailVerificationStatus(){
        boolean flag = false;
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser.isEmailVerified()){
            flag = true;
        } else{
            Toast.makeText(getApplicationContext(), "Verify the email", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
        return flag;
    }
}

// font - coveredByYourGrace