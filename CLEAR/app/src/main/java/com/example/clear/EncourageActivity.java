package com.example.clear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EncourageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encourage);

        Button btn_back=findViewById(R.id.btn_back_0);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(EncourageActivity.this,PatientOftenActivity.class);
                startActivity(intent);

            }
        });

        Button btn_next=findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(EncourageActivity.this,PatientPast1Activity.class);
                startActivity(intent);

            }
        });
    }
}
