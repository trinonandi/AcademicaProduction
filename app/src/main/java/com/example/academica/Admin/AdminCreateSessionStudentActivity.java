package com.example.academica.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.academica.Login;
import com.example.academica.R;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.util.Log.i;

public class AdminCreateSessionStudentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ADMIN CREATE SESSION";
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private final int idProfilePage = R.id.profile_page, idLogOut = R.id.logout;    // makes the switch case ids final
    private Button deptButton, semButton;
    private AdminRegDataHelper currentUserData;
    private ArrayList<RecyclerItem> recyclerItemsArrayList;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private RecyclerView recyclerView;
    private Dialog addItemDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_session_students);

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

        // setting dept menu on the department button
        deptButton = findViewById(R.id.admin_createSession_student_deptBtn);
        deptButton.setOnClickListener(v -> {
            PopupMenu dept = new PopupMenu(this,deptButton, Gravity.CENTER);
            dept.getMenuInflater().inflate(R.menu.dept_menu,dept.getMenu());
            dept.setOnMenuItemClickListener(item -> {
                deptButton.setText(item.getTitle());
                return true;
            });

            dept.show();
        });

        // setting sem menu on the semester button
        semButton = findViewById(R.id.admin_createSession_student_semBtn);
        semButton.setOnClickListener(v -> {
            PopupMenu sem = new PopupMenu(this,semButton, Gravity.CENTER);
            sem.getMenuInflater().inflate(R.menu.sem_menu,sem.getMenu());
            sem.setOnMenuItemClickListener(item -> {
                semButton.setText(item.getTitle());
                return true;
            });

            sem.show();
        });

        // initiating dialog box for data input
        addItemDialog = new Dialog(this);
        addItemDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Recycler view implementation code
        recyclerItemsArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.admin_createSession_student_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerAdapter = new RecyclerAdapter(recyclerItemsArrayList);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        // implement the touch helper for swipe actions
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
    public void onBackPressed() {
        addItemDialog.setContentView(R.layout.instructions_dialog);
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);

        messageView.setText("Do you want to close? Any unsaved change will be discarded. Press YES to close NO to go back");
        noBtn.setOnClickListener(v -> addItemDialog.dismiss());
        yesBtn.setOnClickListener(v ->{
            addItemDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
            finish();
        });
        addItemDialog.show();
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
                showUpdateSubject();
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

    public void addItem(View view){
        // open the data item adding dialog box and show them on the recyclerView. Runs on ADD button press
        addItemDialog.setContentView(R.layout.recycler_add_item_dialog);
        TextInputLayout rollLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_roll),
                nameLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_name);
        Button closeButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_closeBtn),
                addButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_addBtn);

        closeButton.setOnClickListener(v -> addItemDialog.dismiss());

        addButton.setOnClickListener(v -> {
            String roll = Objects.requireNonNull(rollLayout.getEditText()).getText().toString().trim();
            String name = Objects.requireNonNull(nameLayout.getEditText()).getText().toString().trim();
            if(roll.length() > 0 && name.length() > 0){
                RecyclerItem item = new RecyclerItem(roll,name);
                recyclerItemsArrayList.add(item);
                recyclerAdapter.notifyItemInserted(recyclerItemsArrayList.indexOf(item));
                addItemDialog.dismiss();
            }
            else{
                Toast.makeText(this, "Empty Fields", Toast.LENGTH_SHORT).show();
            }
        });

        addItemDialog.show();
    }

    public void closeWindow(View view) {
        onBackPressed();
    }

    public void createStudentSession(View view) {
        // runs on NEXT button press

        addItemDialog.setContentView(R.layout.instructions_dialog);
        addItemDialog.show();
        TextView messageView = addItemDialog.findViewById(R.id.instruction_dialog_textView);
        Button noBtn = addItemDialog.findViewById(R.id.instruction_dialog_noBtn),
                yesBtn = addItemDialog.findViewById(R.id.instruction_dialog_yesBtn);

        messageView.setText("Do you want to close? Any unsaved change will be discarded. Press YES to close NO to go back");
        noBtn.setOnClickListener(v -> addItemDialog.dismiss());
        yesBtn.setOnClickListener(v ->{
            if (deptButton.getText().toString().toLowerCase().equals("department") || semButton.getText().toString().toLowerCase().equals("semester")){
                Toast.makeText(this, "Select department and semester", Toast.LENGTH_SHORT).show();
                return;
            }
            // converting from object to a hash map for database update
            Map<String, String> sortedStudentDataMap = new TreeMap<>();
            for(RecyclerItem item : recyclerItemsArrayList){
                sortedStudentDataMap.put(item.getKey(), item.getName());
            }
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String sem = semButton.getText().toString(), dept = deptButton.getText().toString();
            String semNumber = ""+sem.charAt(0);
            // storing the students data
            reference.child("sessions").child(dept).child("students").child(semNumber).setValue(sortedStudentDataMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AdminCreateSessionStudentActivity.this, "Student data successfully saved", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(AdminCreateSessionStudentActivity.this, "Failed to save data", Toast.LENGTH_SHORT).show());
            addItemDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
            finish();
        });

    }
}
