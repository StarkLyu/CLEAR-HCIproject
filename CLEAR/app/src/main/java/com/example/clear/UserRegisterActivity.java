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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.TileOverlayOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity {

    private Button btn_register;//注册按钮
    //用户名，密码，再次输入的密码的控件
    private EditText et_user_name,et_psw,et_tel;
    //用户名，密码，再次输入的密码的控件的获取值
    private String userName,psw, tel;
    Thread thread1;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        init();
    }

    private void init() {
        //从activity_register.xml 页面中获取对应的UI控件
        btn_register=findViewById(R.id.btn_register);
        et_user_name=findViewById(R.id.et_user_name);
        et_psw=findViewById(R.id.et_psw);
        et_tel=findViewById(R.id.et_tel);
        progressBar=findViewById(R.id.spin_kit);

        //注册按钮
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入在相应控件中的字符串
                getEditString();
                //判断输入框内容
                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(UserRegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(UserRegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(tel)){
                    Toast.makeText(UserRegisterActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    thread1=new Thread(runnable);
                    thread1.start();
                }
            }
        });
    }
    /**
     * 获取控件中的字符串
     */
    private void getEditString(){
        userName=et_user_name.getText().toString().trim();
        psw=et_psw.getText().toString().trim();
        tel= et_tel.getText().toString().trim();
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
            String post=userRegister(userName,psw,tel,"0");
            Log.i("post request", userName+" "+psw+" "+tel+" 0");
            Log.i("post response",post);

//            解析json
            try {
//                JSONArray jsonArray=new JSONArray(post);

                if(post.equals("success")){

                    SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
                    sp.edit().putString("username", userName).putString("password", psw).putBoolean("state",true).apply();

                    // 表示此页面下的内容操作成功将data返回到上一页面，如果是用back返回过去的则不存在用setResult传递data值
                    UserRegisterActivity.this.finish();

                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else{
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"注册失败",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value","请求结果");

            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    /**
     *post注册信息
     */
    public static String userRegister(String username, String password, String tel, String device){

        String h="http://175.24.72.189/index.php?r=user/register";
        Map<String,Object> mmap=new LinkedHashMap<>();
        mmap.put("userName", username);
        mmap.put("password", password);
        mmap.put("tel", tel);
        mmap.put("device", device);
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        return postInfo.postMethod();
    }



}
