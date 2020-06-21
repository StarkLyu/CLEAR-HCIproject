package com.example.clear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.yzq.zxinglibrary.encode.CodeCreator;

import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity {

    private Button btn_relogin, btn_return;
    private ImageView img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //用每个用户的device token生成二维码
        SharedPreferences sp2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        String device=sp2.getString("token", null);

        img = findViewById(R.id.myimg);
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Bitmap bitmap = CodeCreator.createQRCode(device, 400, 400, logo);
        img.setImageBitmap(bitmap);

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
                startActivityForResult(intent,100);
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
