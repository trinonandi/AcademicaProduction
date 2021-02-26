package com.example.academica;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.card.MaterialCardView;

public class StudentProfileActivity extends AppCompatActivity {

    ConstraintLayout expandableView;
    Button arrowBtn;
    MaterialCardView cardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

            expandableView = findViewById(R.id.expandableView);
            arrowBtn = findViewById(R.id.arrowBtn);
            cardView = findViewById(R.id.StudentProfileCard);
            arrowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( expandableView.getVisibility()== View.GONE){
                        TransitionManager.beginDelayedTransition(cardView , new AutoTransition());
                        expandableView.setVisibility(View.VISIBLE);
                    }

                    else{
                        TransitionManager.beginDelayedTransition(cardView , new AutoTransition());
                        expandableView.setVisibility(View.GONE);
                    }
                }
            });

        }
}