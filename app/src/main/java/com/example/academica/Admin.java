package com.example.academica;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Admin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Admin extends Fragment {

    private static final String TAG = "Admin";
    private TextInputLayout AdminName, AdminEmail, AdminPwd;
    private ExtendedFloatingActionButton AdminRegsterBtn;
    private FirebaseAuth mAuth;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Admin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Admin.
     */
    // TODO: Rename and change types and number of parameters
    public static Admin newInstance(String param1, String param2) {
        Admin fragment = new Admin();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        AdminName = view.findViewById(R.id.adminReg_name_editText);
        AdminEmail = view.findViewById(R.id.adminReg_email_editText);
        AdminPwd = view.findViewById(R.id.adminReg_pwd_editText);
        AdminRegsterBtn = view.findViewById(R.id.adminReg_reg_btn);

        if(AdminEmail == null){
            Log.d(TAG, "onCreate: null returned");
        }

        AdminRegsterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegistration();
            }
        });

        //FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        return view;
         // Inflate the layout for this fragment

    }
    private void doRegistration(){

        String name = AdminName.getEditText().toString();
        String pwd = AdminPwd.getEditText().toString();
        String email = AdminEmail.getEditText().toString();

        Log.i(name, "name");
        if(!name.isEmpty() && !pwd.isEmpty() && !email.isEmpty()){
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(getContext(), "Invalid Email ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if(pwd.length()<6){
                Toast.makeText(getContext(), "Password length must be at least 6 digits", Toast.LENGTH_SHORT).show();
                return;
            }
        } else{
            Toast.makeText(getContext(), "Empty fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    emailVerification();
                } else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                    Toast.makeText(getContext(), "User Already Registered", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(getContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


    }

    private void emailVerification(){
        final FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        Toast.makeText(getContext(),"Registration Successful,Verify Email",Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(getContext(),MainActivity.class));
                    }
                    else{
                        Toast.makeText(getContext(),"Verification Email Cannot Be Sent",Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}