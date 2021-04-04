package com.example.academica.Admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.academica.Login;
import com.example.academica.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminRegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminRegistrationFragment extends Fragment {

    private static final String TAG = "Admin";
    private TextInputLayout AdminName, AdminEmail, AdminPwd,AdminAuthId;
    private ExtendedFloatingActionButton AdminRegsterBtn;
    private FirebaseAuth mAuth;

    private MaterialTextView adminInstructions;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AdminRegistrationFragment() {
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
    public static AdminRegistrationFragment newInstance(String param1, String param2) {
        AdminRegistrationFragment fragment = new AdminRegistrationFragment();
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
        AdminAuthId = view.findViewById(R.id.adminReg_authId_editText);

        //Admin reg instruction
        adminInstructions = view.findViewById(R.id.reg_instruction);





        adminInstructions.setOnClickListener(v -> {
            MaterialAlertDialogBuilder instruction_dialog = new MaterialAlertDialogBuilder(getContext());
            instruction_dialog.setTitle("Instruction");
            instruction_dialog.setMessage(getString(R.string.instruction_dialog));

            instruction_dialog.setPositiveButton("OkAY", (dialog, which) -> {

            });
            instruction_dialog.show();


        });
        if(AdminEmail == null){
            Log.d(TAG, "onCreate: null returned");
        }

        AdminRegsterBtn.setOnClickListener(v -> doRegistration());

        //FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        return view;
        // Inflate the layout for this fragment

    }
    private void doRegistration(){

        String name = Objects.requireNonNull(AdminName.getEditText().getText()).toString();
        String pwd = Objects.requireNonNull(AdminPwd.getEditText().getText()).toString();
        String email = Objects.requireNonNull(AdminEmail.getEditText().getText()).toString();
        String currentAuthId = Objects.requireNonNull(AdminAuthId.getEditText().getText()).toString();

        Log.i(name, "name");
        if(!name.isEmpty() && !pwd.isEmpty() && !email.isEmpty() && !currentAuthId.isEmpty()){
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(getContext(), "Invalid Email ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if(pwd.length()<6){
                Toast.makeText(getContext(), "Password length must be at least 6 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            String actualAuthId = getResources().getString(R.string.adminAuthID);
            if(!currentAuthId.equals(actualAuthId)){    // auth id validity checking
                Toast.makeText(getContext(), "Invalid Auth ID", Toast.LENGTH_SHORT).show();
                return;
            }
        } else{
            Toast.makeText(getContext(), "Empty fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                emailVerification();

                // sending student data to firebase realtime database
                rootNode = FirebaseDatabase.getInstance(); // getting root instance of DB
                reference = rootNode.getReference(); // getting root reference of the DB

                AdminRegDataHelper data = new AdminRegDataHelper(name, email); // helper object to be passed in DB

                String key = AdminRegDataHelper.generateKeyFromEmail(email);

                reference.child("users").child(key).setValue(data); // sending data to the proper child node under root->users->students


            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(getContext(), "User Already Registered", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void emailVerification(){
        final FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                if(task.isSuccessful()){

                    Toast.makeText(getContext(),"Registration Successful,Verify Email",Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    startActivity(new Intent(getContext(), Login.class));
                }
                else{
                    Toast.makeText(getContext(),"Verification Email Cannot Be Sent",Toast.LENGTH_LONG).show();

                }
            });
        }
    }
}