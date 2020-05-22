
package com.example.clear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

public class PatientPast4Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_past4);

        Button btn_new=findViewById(R.id.btn_new);
        btn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PatientPast4Activity.this,PatientPast4Activity.class);
                startActivity(intent);
            }
        });

        Button btn_next=findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PatientPast4Activity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Calendar c = Calendar.getInstance();
        String mYear = String.valueOf(c.get(Calendar.YEAR));
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String mHour =String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        EditText date1=findViewById(R.id.ed_gender);
        EditText date2=findViewById(R.id.ed_phone);
        date1.setText(mYear + "." + mMonth + "." + mDay + " "+ mHour+" : 00");
        date2.setText(mYear + "." + mMonth + "." + mDay + " "+ mHour+" : 00");
    }
}
