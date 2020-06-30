package com.example.clear.questionSystem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.clear.MainActivity;
import com.example.clear.PatientPast1Activity;

public class RecordOption extends Option {


    public RecordOption(String text, QuestionSystem system) {
        super(text, OptionType.RECORD, system);
    }

    @Override
    public void onSelected() {
        //Todo: 弹出输入患者信息的表单
        Activity activity=new MainActivity();
        Intent intent=new Intent(activity,PatientPast1Activity.class);
        activity.getApplication().startActivity(intent);
        super.onSelected();
    }

}
