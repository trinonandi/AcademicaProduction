package com.example.academica;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Teacher#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Teacher extends Fragment {


    private static final String TAG = "Teacher";
    private TextInputLayout teacherName, teacherEmail, teacherPwd, teacherAuthID;
    private ExtendedFloatingActionButton teacherRegBtn;
    private Button teacherDeptBtn;
    private FirebaseAuth mAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Teacher() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Teacher.
     */
    // TODO: Rename and change types and number of parameters
    public static Teacher newInstance(String param1, String param2) {
        Teacher fragment = new Teacher();
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

        View view = inflater.inflate(R.layout.fragment_teacher, container, false);
        teacherName = view.findViewById(R.id.teacherReg_name_editText);
        teacherEmail = view.findViewById(R.id.teacherReg_email_editText);
        teacherPwd = view.findViewById(R.id.teacherReg_pwd_editText);
        teacherRegBtn = view.findViewById(R.id.teacherReg_reg_btn);
        teacherDeptBtn = view.findViewById(R.id.teacherReg_dep);
        teacherAuthID= view.findViewById(R.id.teacherReg_authId_editText);

        if (teacherEmail == null) {
            Log.d(TAG, "onCreate: null returned");
        }


        teacherRegBtn.setOnClickListener(v -> doRegistration());

        teacherDeptBtn.setOnClickListener(v -> {
            PopupMenu dept = new PopupMenu(getContext(), teacherDeptBtn);
            dept.getMenuInflater().inflate(R.menu.teacher_dept, dept.getMenu());
            dept.setOnMenuItemClickListener(item -> {
                teacherDeptBtn.setText(item.getTitle());
                return true;
            });
            dept.show();
        });
        //FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();

        // Inflate the layout for this fragment


        return view;
    }

    private void doRegistration() {
        String name = teacherName.getEditText().getText().toString().trim();
        String pwd = teacherPwd.getEditText().getText().toString().trim();
        String email = teacherEmail.getEditText().getText().toString().trim();
        String teacherDep = teacherDeptBtn.getText().toString().trim();
        String currentAuthId = teacherAuthID.getEditText().getText().toString().trim();

        if (!name.isEmpty() && !pwd.isEmpty() && !email.isEmpty()  && !currentAuthId.isEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {   // email pattern checking
                Toast.makeText(getContext(), "Invalid Email ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pwd.length() < 6) { // password must be >=6 char checking
                Toast.makeText(getContext(), "Password length must be at least 6 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            String actualAuthId = getResources().getString(R.string.teacherAuthID); // retrieves the actual authId from strings.xml
            if (!currentAuthId.equals(actualAuthId)) {    // auth id validity checking
                Toast.makeText(getContext(), "Invalid Auth ID", Toast.LENGTH_SHORT).show();
                return;
            }

        } else {
            Toast.makeText(getContext(), "Empty fields", Toast.LENGTH_SHORT).show();
            return;

        }
        mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                emailVerification();

                // sending student data to firebase realtime database
                rootNode = FirebaseDatabase.getInstance(); // getting root instance of DB
                reference = rootNode.getReference(); // getting root reference of the DB

                TeacherRegDataHelper data = new TeacherRegDataHelper(name, email, teacherDep); // helper object to be passed in DB

                String key = TeacherRegDataHelper.generateKeyFromEmail(email);

                reference.child("users").child("teachers").child(key).setValue(data); // sending data to the proper child node under root->users->students


            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(getContext(), "User Already Registered", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void emailVerification() {
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    Toast.makeText(getContext(), "Registration Successful,Verify Email", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    startActivity(new Intent(getContext(), Login.class));
                } else {
                    Toast.makeText(getContext(), "Verification Email Cannot Be Sent", Toast.LENGTH_LONG).show();

                }
            });
        }
    }
}