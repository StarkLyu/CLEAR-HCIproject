package com.example.clear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class UserLoginActivity extends AppCompatActivity {

    private Button btn_register, btn_login, btn_return;//注册按钮
    //用户名，密码，再次输入的密码的控件
    private EditText et_user_name,et_psw;
    //用户名，密码，再次输入的密码的控件的获取值
    private String userName,psw,device;
    Thread thread1;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        init();
    }


    private void init() {
        //从activity_register.xml 页面中获取对应的UI控件
        btn_register=findViewById(R.id.btn_register);
        btn_login=findViewById(R.id.btn_login);
        btn_return=findViewById(R.id.return_icon);
        progressBar=findViewById(R.id.spin_kit);

        //展示保存的登录信息
        et_user_name=findViewById(R.id.et_user_name);
        et_psw=findViewById(R.id.et_psw);

        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        userName=sp.getString("username", null);
        psw=sp.getString("password",null);

        et_user_name.setText(userName);
        et_psw.setText(psw);

        SharedPreferences sp2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        device=sp2.getString("token", null);

        //注册按钮
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UserLoginActivity.this, UserRegisterActivity.class);
//                startActivity(intent);
                startActivityForResult(intent,100);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEditString();
                //判断输入框内容
                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(UserLoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(UserLoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    Log.i("user info", userName+" "+psw);
                    progressBar.setVisibility(View.VISIBLE);

                    thread1=new Thread(runnable);
                    thread1.start();

//                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    /**
     * 获取控件中的字符串
     */
    private void getEditString(){
        userName=et_user_name.getText().toString().trim();
        psw=et_psw.getText().toString().trim();
    }

    /**
     * 注册后返回注册的账号信息
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode!=100)
            return;
        if(resultCode!=1)
            return;

        userName=data.getStringExtra("username");
        psw=data.getStringExtra("password");

        et_user_name=findViewById(R.id.et_user_name);
        et_psw=findViewById(R.id.et_psw);

        et_user_name.setText(userName);
        et_psw.setText(psw);
    }

    /**
     *获取后台数据
     */
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");

        }
    };

    /**
     * 处理当前定位的请求
     */
    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            String post=userLogin(userName,psw,device);
            Log.i("post request", userName+" "+psw+" "+device);
            Log.i("post response",post);

            if (post.equals("403")){
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"登录失败，用户名不存在",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            else if(post.equals("401")) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"登录失败，密码错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            else{
                //            解析json
                try {
                    JSONObject ob=new JSONObject(post);
                    int role=ob.getInt("userStatus");
                    String auth=ob.getString("authToken");
                    Log.i("role",role+"");
                    SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
                    sp.edit()
                            .putString("username", userName)
                            .putString("password", psw)
                            .putString("authToken", auth)
                            .putBoolean("state",true)
                            .putInt("role",role)
                            .apply();

                    Intent intent = new Intent();
                    intent.putExtra("role", role);
//                    intent.putExtra("password",psw);
                    setResult(1, intent);
                    finish();

                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_SHORT).show();
                    Looper.loop();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }



            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value","请求结果");

            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    /**
     *post登录信息
     */
    public static String userLogin(String username, String password, String device){

        String h="http://175.24.72.189/index.php?r=user/login";
        Map<String,Object> mmap=new LinkedHashMap<>();
        mmap.put("userName", username);
        mmap.put("password", password);
        mmap.put("device", device);
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        return postInfo.postMethod();
    }
}
