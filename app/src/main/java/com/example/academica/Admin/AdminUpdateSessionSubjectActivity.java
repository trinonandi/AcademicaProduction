package com.example.academica.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.academica.Admin.AdminHomeActivity;
import com.example.academica.Admin.AdminRegDataHelper;
import com.example.academica.Admin.RecyclerAdapter;
import com.example.academica.Admin.RecyclerItem;
import com.example.academica.Login;
import com.example.academica.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AdminUpdateSessionSubjectActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private final int idProfilePage = R.id.profile_page, idLogOut = R.id.logout;    // makes the switch case ids final
    private AdminRegDataHelper currentUserData;
    private String sem, dept;
    private DatabaseReference databaseReference;
    private MaterialButton deptButton, semButton, closeBtn, searchBtn;
    private RecyclerView recyclerView;
    private ArrayList<RecyclerItem> recyclerItemsArrayList;
    private RecyclerAdapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private RelativeLayout progressBarLayout;
    private Dialog addItemDialog;
    private boolean addNewSubjectFlag = false;  // a flag used to change the fetched item's colours

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_update_session_subject);

        currentUserData = (AdminRegDataHelper)getIntent().getSerializableExtra("userData");
        mAuth = FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.student_main_drawer);
        drawerLayout = findViewById(R.id.student_drawer_layout);
        navigationView  = findViewById(R.id.student_Nav_menu);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.draweropen,
                R.string.drawerclose);


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        setNavData();

        progressBarLayout = findViewById(R.id.admin_updateSession_subjects_progressBar_layout);
        deptButton = findViewById(R.id.admin_updateSession_subjects_deptBtn);
        semButton = findViewById(R.id.admin_updateSession_subjects_semBtn);
        closeBtn = findViewById(R.id.admin_updateSession_subjects_closeBtn);
        searchBtn = findViewById(R.id.admin_updateSession_subjects_searchBtn);
        deptButton.setOnClickListener(v -> {
            PopupMenu dept = new PopupMenu(this,deptButton, Gravity.CENTER);
            dept.getMenuInflater().inflate(R.menu.dept_menu,dept.getMenu());
            dept.setOnMenuItemClickListener(item -> {
                deptButton.setText(item.getTitle());
                return true;
            });

            dept.show();
        });
        semButton.setOnClickListener(v -> {
            PopupMenu sem = new PopupMenu(this,deptButton, Gravity.CENTER);
            sem.getMenuInflater().inflate(R.menu.sem_menu,sem.getMenu());
            sem.setOnMenuItemClickListener(item -> {
                semButton.setText(item.getTitle());
                return true;
            });

            sem.show();
        });
        closeBtn.setOnClickListener(v -> onBackPressed());
        dept = deptButton.getText().toString();
        sem = semButton.getText().toString();

        // initiating dialog box for data input
        addItemDialog = new Dialog(this);
        addItemDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Recycler View
        recyclerItemsArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.admin_updateSession_subjects_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerAdapter = new RecyclerAdapter(recyclerItemsArrayList);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        // implement the touch helper for swipe actions
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
            startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
            finish();
        });
        addItemDialog.show();
    }

    // left and right swipe on recycler items
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAbsoluteAdapterPosition();
            switch (direction){
                case ItemTouchHelper.LEFT:      // left swipe removes the data
                    RecyclerItem deletedItem = recyclerItemsArrayList.get(position);
                    recyclerItemsArrayList.remove(position);
                    recyclerAdapter.notifyItemRemoved(position);
                    // a snack bar for undo action
                    Snackbar.make(recyclerView, deletedItem.getKey() + " " +deletedItem.getName(), Snackbar.LENGTH_SHORT)
                            .setAction("Undo", v -> {
                                recyclerItemsArrayList.add(position, deletedItem);
                                recyclerAdapter.notifyItemInserted(position);
                            }).show();
                    break;
                case ItemTouchHelper.RIGHT:     // right swipe to edit the data from a dialog box
                    RecyclerItem swipedItem = recyclerItemsArrayList.get(position);
                    addItemDialog.setContentView(R.layout.recycler_add_item_dialog);
                    TextInputLayout rollLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_roll),
                            nameLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_name);
                    Button closeButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_closeBtn),
                            addButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_addBtn);
                    // setting data from the swiped item
                    Objects.requireNonNull(rollLayout.getEditText()).setText(swipedItem.getKey());
                    Objects.requireNonNull(nameLayout.getEditText()).setText(swipedItem.getName());

                    addButton.setOnClickListener(v -> {
                        String roll = Objects.requireNonNull(rollLayout.getEditText()).getText().toString().trim();
                        String name = Objects.requireNonNull(nameLayout.getEditText()).getText().toString().trim();
                        if(roll.length() > 0 && name.length() > 0){
                            swipedItem.setName(name); swipedItem.setKey(roll);
                            recyclerAdapter.notifyItemChanged(position);
                            addItemDialog.dismiss();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Empty Fields", Toast.LENGTH_SHORT).show();
                        }
                    });

                    closeButton.setOnClickListener(v ->{
                        recyclerAdapter.notifyItemChanged(position);
                        addItemDialog.dismiss();
                    });

                    addItemDialog.show();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            // for swipe icons and labels
            new RecyclerViewSwipeDecorator.Builder(getApplicationContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                    .addSwipeLeftLabel("DELETE")
                    .setSwipeLeftLabelColor(ContextCompat.getColor(getApplicationContext(), R.color.white))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue_ripple_custom))
                    .addSwipeRightLabel("EDIT")
                    .setSwipeRightLabelColor(ContextCompat.getColor(getApplicationContext(), R.color.white))
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_edit_24)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case idProfilePage:
                showProfile();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case idLogOut:
                drawerLayout.closeDrawer(GravityCompat.START);
                doLogout();
                break;
            case R.id.admin_AllNav_Attendance:
                drawerLayout.closeDrawer(GravityCompat.START);
                showAttendance();
                break;
            case R.id.admin_AllNav_createStudent:
                drawerLayout.closeDrawer(GravityCompat.START);
                showCreateStudent();
                break;
            case R.id.admin_AllNav_createSubject:
                drawerLayout.closeDrawer(GravityCompat.START);
                showCreateSubject();
                break;
            case R.id.admin_AllNav_UpdateStudent:
                drawerLayout.closeDrawer(GravityCompat.START);
                showUpdateStudent();
                break;
            case R.id.admin_AllNav_UpdateSubject:
                drawerLayout.closeDrawer(GravityCompat.START);

                break;
            case R.id.personalized_home:
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(getApplicationContext(),AdminHomeActivity.class));
                break;

        }
        return true;
    }

    public void doLogout(){
        mAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), Login.class));
    }

    public void showProfile(){
        Intent intent = new Intent(getApplicationContext(), AdminProfileActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    public void showAttendance() {
        Intent intent = new Intent(getApplicationContext(), AdminAttendanceActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    public void showCreateStudent()
    {
        Intent intent = new Intent(getApplicationContext(), AdminCreateSessionStudentActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }
    public void showCreateSubject(){
        Intent intent = new Intent(getApplicationContext(), AdminCreateSessionSubjectActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }

    public void showUpdateStudent(){
        Intent intent = new Intent(getApplicationContext(), AdminUpdateSessionStudentActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }
    public void showUpdateSubject(){
        Intent intent = new Intent(getApplicationContext(), AdminUpdateSessionSubjectActivity.class);
        intent.putExtra("userData", currentUserData);
        startActivity(intent);
    }


    public void searchData(View view) {
        // method to search student data from dept and sem. Executed on SEARCH button click
        progressBarLayout.setVisibility(View.VISIBLE);
        dept = deptButton.getText().toString();
        sem = semButton.getText().toString();
        String semNumber = ""+sem.charAt(0);
        if(dept.toLowerCase().equals("dept") || sem.toLowerCase().equals("sem")){
            Toast.makeText(this, "Choose department and semester", Toast.LENGTH_SHORT).show();
            progressBarLayout.setVisibility(View.GONE);
            return;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("sessions").child(dept).child("subjects").child(semNumber);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    // data is not present in the database
                    Toast.makeText(getApplicationContext(), "Data not found. Create the subjects first", Toast.LENGTH_SHORT).show();
                    progressBarLayout.setVisibility(View.GONE);
                    return;
                }
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();    // getting an iterable getValues() object
                recyclerItemsArrayList = new ArrayList<>();
                while(iterator.hasNext()){
                    DataSnapshot item = iterator.next();
                    String key = item.getKey();
                    String val = (String) item.getValue();
                    RecyclerItem obj = new RecyclerItem(key, val);
                    recyclerItemsArrayList.add(obj);
                }
                if(recyclerItemsArrayList.size() == 0){
                    // database created but no student added
                    Toast.makeText(getApplicationContext(), "Data found with zero entries", Toast.LENGTH_LONG).show();
                }
                recyclerAdapter = new RecyclerAdapter(recyclerItemsArrayList);
                recyclerView.setAdapter(recyclerAdapter);

                // setting empty OnClickListeners so that the data cannot be changed further
                deptButton.setOnClickListener(v -> {});
                semButton.setOnClickListener(v -> {});
                searchBtn.setOnClickListener(v -> {});

                progressBarLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error in data fetching", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addSubject(View view){
        // open the data item adding dialog box and show them on the recyclerView. Runs on ADD button press
        if(dept.toLowerCase().equals("dept") || sem.toLowerCase().equals("sem")){
            Toast.makeText(this, "Search a session first", Toast.LENGTH_SHORT).show();
            return;
        }
        addItemDialog.setContentView(R.layout.recycler_add_item_dialog);
        TextInputLayout codeLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_roll),
                nameLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_name);
        Button closeButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_closeBtn),
                addButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_addBtn);

        codeLayout.setHint(getString(R.string.subject_code));
        nameLayout.setHint(getString(R.string.subject_name));

        closeButton.setOnClickListener(v -> addItemDialog.dismiss());

        addButton.setOnClickListener(v -> {
            String code = Objects.requireNonNull(codeLayout.getEditText()).getText().toString().trim();
            String name = Objects.requireNonNull(nameLayout.getEditText()).getText().toString().trim();
            if(code.length() > 0 && name.length() > 0){
                RecyclerItem item = new RecyclerItem(code,name);
                recyclerItemsArrayList.add(item);
                recyclerAdapter.notifyItemInserted(recyclerItemsArrayList.indexOf(item));

                if(!addNewSubjectFlag){
                    // setting the colours of the existing students to green to segregate from the new students
                    recyclerAdapter.changeColorFlag();
                    addNewSubjectFlag = true;
                }

                addItemDialog.dismiss();
            }
            else{
                Toast.makeText(this, "Empty Fields", Toast.LENGTH_SHORT).show();
            }

        });
        addItemDialog.show();
    }

    public void updateSubjectData(View view) {
        if(dept.toLowerCase().equals("dept") || sem.toLowerCase().equals("sem")){
            Toast.makeText(this, "Choose department and semester", Toast.LENGTH_SHORT).show();
            progressBarLayout.setVisibility(View.GONE);
            return;
        }
        addItemDialog.setContentView(R.layout.instructions_dialog);
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);

        messageView.setText("Confirm update of subject data. Press YES to update database NO to go back");
        noBtn.setOnClickListener(v -> addItemDialog.dismiss());
        yesBtn.setOnClickListener(v ->{
            progressBarLayout.setVisibility(View.VISIBLE);

            TreeMap<String, String> sortedDataMap = new TreeMap<>();
            for(RecyclerItem item : recyclerItemsArrayList){
                sortedDataMap.put(item.getKey(), item.getName());
            }
            databaseReference.setValue(sortedDataMap).addOnSuccessListener(aVoid -> {
                Toast.makeText(getApplicationContext(), "Data updated successfully", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to update data", Toast.LENGTH_SHORT).show());
        });
        addItemDialog.show();

        progressBarLayout.setVisibility(View.GONE);

    }

}