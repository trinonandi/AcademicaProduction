package com.example.academica.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AdminCreateSessionSubjectActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "ADMIN";

    private Button backButton, semButton, deptButton;
    private HashMap<String, String> studentDataMap;
    private String dept, sem;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private final int idProfilePage = R.id.profile_page, idLogOut = R.id.logout;    // makes the switch case ids final

    private Dialog addItemDialog;
    private ArrayList<RecyclerItem> recyclerItemsArrayList;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_session_subject);

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


        // getting parcelable data
        AdminParcelableStudentData data = getIntent().getParcelableExtra("data");
        dept = data.getDept();
        sem  = data.getSem();
        studentDataMap = data.getStudentDataMap();
        backButton = findViewById(R.id.admin_createSessionSubject_backBtn);
        semButton = findViewById(R.id.admin_createSessionSubject_semBtn);
        deptButton = findViewById(R.id.admin_createSessionSubject_deptBtn);
        deptButton.setText(dept);
        semButton.setText(sem);
        backButton.setOnClickListener(v -> onBackPressed());

        // initiating dialog box for data input
        addItemDialog = new Dialog(this);
        addItemDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Recycler view implementation code
        recyclerItemsArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.admin_createSessionSubject_recyclerView);
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
                    TextInputLayout subjectCodeLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_roll),
                            subjectNameLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_name);
                    Button closeButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_closeBtn),
                            addButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_addBtn);
                    subjectCodeLayout.setHint(R.string.subject_code);
                    subjectNameLayout.setHint(R.string.subject_name);
                    // setting data from the swiped item
                    Objects.requireNonNull(subjectCodeLayout.getEditText()).setText(swipedItem.getKey());
                    Objects.requireNonNull(subjectNameLayout.getEditText()).setText(swipedItem.getName());

                    addButton.setOnClickListener(v -> {
                        String code = Objects.requireNonNull(subjectCodeLayout.getEditText()).getText().toString().trim();
                        String name = Objects.requireNonNull(subjectNameLayout.getEditText()).getText().toString().trim();
                        if(code.length() > 0 && name.length() > 0){
                            swipedItem.setName(name); swipedItem.setKey(code);
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

        }
        return true;
    }

    public void doLogout(){
        mAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), Login.class));
    }

    public void showProfile(){
//        Intent intent = new Intent(getApplicationContext(),StudentProfileActivity.class);
//        intent.putExtra("UserData", currentUserData);
//        startActivity(intent);
        Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
    }

    public void addSubject(View view) {
        // open the data item adding dialog box and show them on the recyclerView
        addItemDialog.setContentView(R.layout.recycler_add_item_dialog);
        TextInputLayout subjectCodeLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_roll),
                subjectNameLayout = addItemDialog.findViewById(R.id.admin_createSession_dialog_name);
        Button closeButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_closeBtn),
                addButton = addItemDialog.findViewById(R.id.admin_createSession_dialog_addBtn);

        subjectCodeLayout.setHint(R.string.subject_code);
        subjectNameLayout.setHint(R.string.subject_name);

        closeButton.setOnClickListener(v -> addItemDialog.dismiss());

        addButton.setOnClickListener(v -> {
            String code = Objects.requireNonNull(subjectCodeLayout.getEditText()).getText().toString().trim();
            String name = Objects.requireNonNull(subjectNameLayout.getEditText()).getText().toString().trim();
            if(code.length() > 0 && name.length() > 0){
                RecyclerItem item = new RecyclerItem(code,name);
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

    public void submitAllData(View view) {
        HashMap<String, String> subjectDataMap = new HashMap<>();
        for(RecyclerItem item : recyclerItemsArrayList){
            subjectDataMap.put(item.getKey(), item.getName());
        }

        // sorting the students according to key ( roll number )
        TreeMap<String, String> sortedStudentDataMap = new TreeMap<>(studentDataMap);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String semNumber = ""+sem.charAt(0);

        // storing the students data
        reference.child("sessions").child(dept).child("students").child(semNumber).setValue(sortedStudentDataMap);
        // storing subject data
        reference.child("sessions").child(dept).child("subjects").child(semNumber).setValue(subjectDataMap);

        Toast.makeText(this, "Session Successfully Saved", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
        finish();
    }
}