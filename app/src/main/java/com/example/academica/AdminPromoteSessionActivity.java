package com.example.academica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academica.Admin.AdminHomeActivity;
import com.example.academica.Admin.AdminRegDataHelper;
import com.example.academica.Admin.AdminUpdateSessionSubjectActivity;
import com.example.academica.Teacher.TeacherHomeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdminPromoteSessionActivity extends AppCompatActivity {

    private static final String TAG = "PROMOTE";
    private Button deptButton, searchButton, closeButton, promoteButton;
    private ListView listView;
    private String dept;
    private HashMap<String, Object> oldMap = new HashMap<>();
    private ArrayList<String> listViewItemList;
    private ArrayAdapter<String> listViewArrayAdapter;
    private Dialog addItemDialog;
    private RelativeLayout progressBarLayout;
    private AdminRegDataHelper userData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_promote_session);

        userData = (AdminRegDataHelper)getIntent().getSerializableExtra("userData");

        progressBarLayout = findViewById(R.id.admin_promote_progressBar_layout);
        deptButton = findViewById(R.id.admin_promote_deptBtn);
        searchButton = findViewById(R.id.admin_promote_searchBtn);
        closeButton = findViewById(R.id.admin_promote_closeBtn);
        promoteButton = findViewById(R.id.admin_promote_promoteBtn);

        deptButton.setOnClickListener(v -> {
            PopupMenu dept = new PopupMenu(this,deptButton, Gravity.CENTER);
            dept.getMenuInflater().inflate(R.menu.dept_menu,dept.getMenu());
            dept.setOnMenuItemClickListener(item -> {
                deptButton.setText(item.getTitle());
                return true;
            });

            dept.show();
        });
        searchButton.setOnClickListener(v -> {
            searchSemesters();
        });
        closeButton.setOnClickListener(v ->{
            onBackPressed();
        });
        promoteButton.setOnClickListener(v -> {
            promote();
        });
        dept = deptButton.getText().toString();

        // initiating dialog box for data input
        addItemDialog = new Dialog(this);
        addItemDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // list view implementation
        listViewItemList = new ArrayList<>();
        listViewArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, listViewItemList);
        listView = findViewById(R.id.admin_promote_listView);
        listView.setAdapter(listViewArrayAdapter);
    }


    @Override
    public void onBackPressed() {
        addItemDialog.setContentView(R.layout.instructions_dialog);
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);

        messageView.setText("Do you want to close? Any unsaved change will be discarded. Press YES to close NO to go back");
        noBtn.setOnClickListener(v -> addItemDialog.dismiss());
        yesBtn.setOnClickListener(v ->{
            addItemDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), TeacherHomeActivity.class));
            finish();
        });
        addItemDialog.show();
    }

    public void searchSemesters(){
        dept = deptButton.getText().toString();
        if(dept.toLowerCase().equals("dept")){
            Toast.makeText(this, "Select a department", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("sessions").child(dept).child("students");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    Toast.makeText(AdminPromoteSessionActivity.this, "No session available", Toast.LENGTH_SHORT).show();
                    return;
                }
                for(DataSnapshot item : snapshot.getChildren()){
                    if(item.getKey().equals("passout")) continue;

                    listViewItemList.add(item.getKey());
                    oldMap.put(item.getKey(), item.getValue());
                }
                listViewArrayAdapter.notifyDataSetChanged();
                deptButton.setOnClickListener(v -> {});
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void promote(){
        progressBarLayout.setVisibility(View.VISIBLE);
        addItemDialog.setContentView(R.layout.instructions_dialog);
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);
        messageView.setText("Do you want to promote the selected session's students to next semester? Press YES to promote NO to go back");
        noBtn.setOnClickListener(v -> {
            addItemDialog.dismiss();
            progressBarLayout.setVisibility(View.GONE);
        });
        yesBtn.setOnClickListener(v ->{
            if(dept.toLowerCase().equals("dept")){
                Toast.makeText(this, "Select department", Toast.LENGTH_SHORT).show();
                progressBarLayout.setVisibility(View.GONE);
                return;
            }

            if(listView.getCheckedItemCount() == 0){
                Toast.makeText(this, "Select the semesters to promote", Toast.LENGTH_SHORT).show();
                progressBarLayout.setVisibility(View.GONE);
                return;
            }

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("sessions").child(dept).child("students");
            HashMap<String,Object> newList = new HashMap<>();
            SparseBooleanArray checkedArray = listView.getCheckedItemPositions();
            for(int i=0;i<listView.getCount();i++){
                if(checkedArray.get(i)){
                    String checkedKey = listViewItemList.get(i);
                    Object newValue = oldMap.get(checkedKey);
                    String newKey = String.valueOf(Integer.parseInt(checkedKey) + 1);
                    if(Integer.parseInt(newKey) == 9){
                        newKey = "passout";
                    }
                    newList.put(newKey, newValue);
                }
            }

            reference.setValue(newList)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AdminPromoteSessionActivity.this, "Selected sessions successfully promoted", Toast.LENGTH_SHORT).show();
                        showSuccessDialog();
                        progressBarLayout.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AdminPromoteSessionActivity.this, "Failed to promote sessions", Toast.LENGTH_SHORT).show();
                        progressBarLayout.setVisibility(View.GONE);
                    });

            addItemDialog.dismiss();
        });

        addItemDialog.show();
    }

    private void showSuccessDialog(){
        addItemDialog.setContentView(R.layout.instructions_dialog);
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);
        noBtn.setText("Later");
        yesBtn.setText("Ok");
        messageView.setText("Students are promoted successfully. Update the subjects for new semester.");
        noBtn.setOnClickListener(v -> {
            addItemDialog.dismiss();
            finish();
            Intent intent = new Intent(getApplicationContext(), AdminHomeActivity.class);
            intent.putExtra("userData", userData);
            startActivity(intent);
        });
        yesBtn.setOnClickListener(v -> {
            addItemDialog.dismiss();
            finish();
            Intent intent = new Intent(getApplicationContext(), AdminUpdateSessionSubjectActivity.class);
            intent.putExtra("userData", userData);
            startActivity(intent);
        });
        addItemDialog.show();
    }
}