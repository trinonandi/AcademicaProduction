package com.example.academica;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Login extends AppCompatActivity {
    private TextInputLayout emailEditText, pwdEditText;
    private AppCompatTextView forgotPwdTextView;
    private FirebaseAuth mAuth;
    private Dialog forgotPwdDialogue;   // forgot password dialogue box reference
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(getApplicationContext(), StudentHomeActivity.class));
        }
    }

    public void startRegActivity(View view) {
        startActivity(new Intent(Login.this, RegistrationActivity.class));
    }


    public void doSignIn(View view){
        String email = emailEditText.getEditText().getText().toString().trim();
        String pwd = pwdEditText.getEditText().getText().toString().trim();

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
                .addOnCompleteListener(task -> {
                    if(mAuth.getCurrentUser() == null){
                        Toast.makeText(getApplicationContext(), "Incorrect UserID or Password", Toast.LENGTH_SHORT).show();
                    }
                    else if(emailVerificationStatus()){
                        Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(getApplicationContext(), StudentHomeActivity.class);
                        startActivity(intent);
                    } else{
                        Toast.makeText(getApplicationContext(),"User not found", Toast.LENGTH_SHORT).show();
                    }
                });

        emailEditText.getEditText().setText("");
        pwdEditText.getEditText().setText("");
    }

    private boolean emailVerificationStatus(){
        boolean flag = false;
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser == null){
            return false;
        }
        if(firebaseUser.isEmailVerified()){
            flag = true;
        } else{
            Toast.makeText(getApplicationContext(), "Verify the email", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
        return flag;
    }

    private void openForgetPwdDialogue(){ // method for complete forgot password feature
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
            if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                mAuth.sendPasswordResetEmail(email).addOnSuccessListener(aVoid -> Toast.makeText(Login.this, "Reset email sent successfully", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(Login.this, "Reset email cannot be sent: " +e.getMessage(), Toast.LENGTH_LONG).show());
            } else{
                Toast.makeText(getApplicationContext(), "Invalid Email ID", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPwdDialogue.show();   // this method is mandatory to show the dialogue box
    }
}

// font - coveredByYourGrace