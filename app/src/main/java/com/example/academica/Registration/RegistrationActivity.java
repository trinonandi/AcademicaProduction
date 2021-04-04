package com.example.academica.Registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.WindowManager;

import com.example.academica.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RegistrationActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ViewPager2 viewPager2 = findViewById(R.id.viewPager2);
        viewPager2.setAdapter(new TabPageAdapter(this));
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        TabLayoutMediator tabLayoutMediator1 = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position)
            {
                case 0:
                    tab.setText("Student");
                    break;
                case 1:
                    tab.setText("Teacher");
                    break;
                case 2:
                    tab.setText("Admin");
                    break;
            }
        });
        tabLayoutMediator1.attach();
    }




}
