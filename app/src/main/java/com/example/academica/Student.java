package com.example.academica;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "Student";
    private EditText studentName, studentEmail, studentPwd,studentUnivRoll,studentClassRoll,studentSem,studentAuthId;
    private TextView studentInstructions;
    private FloatingActionButton studentRegisterBtn;
    private Spinner deptSpin;
    private Dialog instructionDialog;
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

    public Student() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Student.
     */
    // TODO: Rename and change types and number of parameters
    public static Student newInstance(String param1, String param2) {
        Student fragment = new Student();
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
                             Bundle savedInstanceState){
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student, container, false);
        studentName = view.findViewById(R.id.studentReg_name_editText);
        studentEmail = view.findViewById(R.id.studentReg_email_editText);
        studentPwd = view.findViewById(R.id.studentReg_pwd_editText);
        studentClassRoll = view.findViewById(R.id.studentReg_classRoll_editText);
        studentSem = view.findViewById(R.id.studentReg_sem_editText);
        studentUnivRoll = view.findViewById(R.id.studentReg_univRoll_editText);
        studentRegisterBtn = view.findViewById(R.id.studentReg_reg_btn);
        studentAuthId= view.findViewById(R.id.studentReg_authId_editText);

        // code to implement instructions dialog box
        instructionDialog = new Dialog(getContext());
        instructionDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        studentInstructions = view.findViewById(R.id.studentReg_instructions_textView);
        studentInstructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInstructions(v);
            }
        });

        // code to implement the department spinner
        deptSpin = view.findViewById(R.id.studentReg_dept_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),R.array.departments, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deptSpin.setAdapter(adapter);
        deptSpin.setOnItemSelectedListener(this);   // check the overridden method setOnItemSelectedListener() below


        studentRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegistration();
            }
        });

        //FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        return view;
    }

    private void doRegistration(){

        String name = studentName.getText().toString();
        String pwd = studentPwd.getText().toString();
        String email = studentEmail.getText().toString();
        String classRoll = studentClassRoll.getText().toString();
        String univRoll = studentUnivRoll.getText().toString();
        String sem = studentSem.getText().toString();
        String dept = deptSpin.getSelectedItem().toString();
        String currentAuthId = studentAuthId.getText().toString();



        if(!name.isEmpty() && !pwd.isEmpty() && !email.isEmpty() && !classRoll.isEmpty() && !sem.isEmpty() && !currentAuthId.isEmpty()){
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){   // email pattern checking
                Toast.makeText(getContext(), "Invalid Email ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if(pwd.length()<6){ // password must be >=6 char checking
                Toast.makeText(getContext(), "Password length must be at least 6 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            String actualAuthId = getResources().getString(R.string.studentAuthID);    // retrieves the actual authId from strings.xml
            if(!currentAuthId.equals(actualAuthId)){    // auth id validity checking
                Toast.makeText(getContext(), "Invalid Auth ID", Toast.LENGTH_SHORT).show();
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

                    // sending student data to firebase realtime database
                    rootNode = FirebaseDatabase.getInstance(); // getting root instance of DB
                    reference = rootNode.getReference(); // getting root reference of the DB
                    StudentRegDataHelper data = new StudentRegDataHelper(name,email,classRoll,univRoll,sem,dept); // helper object to be passed in DB

                    String key = StudentRegDataHelper.generateKeyFromEmail(email);

                    reference.child("users").child("students").child(key).setValue(data); // sending data to the proper child node under root->users->students


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

    private void showInstructions(View view){
        instructionDialog.setContentView(R.layout.student_instructions_dialog);
        AppCompatButton close = instructionDialog.findViewById(R.id.student_instructions_close_btn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instructionDialog.dismiss();
            }
        });
        instructionDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String dept = parent.getItemAtPosition(position).toString();
        Toast.makeText(getContext(), ""+dept, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}