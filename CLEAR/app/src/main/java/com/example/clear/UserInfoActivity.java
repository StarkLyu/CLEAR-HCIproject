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
import android.widget.TextView;
import android.widget.Toast;

import com.yzq.zxinglibrary.encode.CodeCreator;

import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity {

    private Button btn_relogin, btn_return;
    public TextView roleText;
    private ImageView img;
    int role;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //用每个用户的username生成二维码
        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        String userName=sp.getString("username",null);

        img = findViewById(R.id.myimg);
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Bitmap bitmap = CodeCreator.createQRCode(userName, 400, 400, logo);
        img.setImageBitmap(bitmap);

        init();
    }

    private void init() {
        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        role=sp.getInt("role",0);
        roleText=findViewById(R.id.role);
        switch (role){
            case 0:
                roleText.setText("用户身份：普通用户");
                break;
            case 1:
                roleText.setText("用户身份：患者");
                break;
            case 2:
                roleText.setText("用户身份：医生");
                break;
            default:
                break;
        }

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
                Intent intent = new Intent();
                intent.putExtra("role", role);
//                    intent.putExtra("password",psw);
                setResult(1, intent);
                finish();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && resultCode==1){
            role=data.getIntExtra("role",0);
            roleText=findViewById(R.id.role);
            switch (role){
                case 0:
                    roleText.setText("用户身份：普通用户");
                    break;
                case 1:
                    roleText.setText("用户身份：患者");
                    break;
                case 2:
                    roleText.setText("用户身份：医生");
                    break;
                default:
                    break;
            }
//            Log.i("role transform userinfo",role+"");
        }
    }
}
