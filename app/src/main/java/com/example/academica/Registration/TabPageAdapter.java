package com.example.academica.Registration;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.academica.Admin.AdminRegistrationFragment;
import com.example.academica.Student.StudentRegistrationFragment;
import com.example.academica.Teacher.TeacherRegistrationFragment;

public class TabPageAdapter extends FragmentStateAdapter {
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new StudentRegistrationFragment();
            case 1:
                return  new TeacherRegistrationFragment();
            default:
                return new AdminRegistrationFragment();

        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public TabPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
}
