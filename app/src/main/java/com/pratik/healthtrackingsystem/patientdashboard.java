package com.pratik.healthtrackingsystem;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class patientdashboard extends AppCompatActivity {


    private MaterialButton healthbutton, historybutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientdashboard);
        healthbutton = findViewById(R.id.Healthbutton);
        historybutton = findViewById(R.id.Historybutton);

        healthbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        historybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
