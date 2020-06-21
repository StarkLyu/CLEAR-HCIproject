package com.example.clear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity {

    private Button btn_relogin, btn_return;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        init();
    }

    private void init() {

        btn_relogin=findViewById(R.id.btn_re_login);
        btn_return=findViewById(R.id.return_icon);

        //注册按钮
        btn_relogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入在相应控件中的字符串
                Intent intent=new Intent(UserInfoActivity.this, UserLoginActivity.class);
                startActivity(intent);
            }
        });

        btn_return.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String city="kong";
        city+=Objects.requireNonNull(data.getStringExtra("city"));
        Log.i("transform", city);
    }
}
