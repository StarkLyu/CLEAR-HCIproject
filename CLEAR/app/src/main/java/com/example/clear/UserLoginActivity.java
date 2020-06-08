package com.example.clear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserLoginActivity extends AppCompatActivity {

    private Button btn_register, btn_login;//注册按钮
    //用户名，密码，再次输入的密码的控件
    private EditText et_user_name,et_psw;
    //用户名，密码，再次输入的密码的控件的获取值
    private String userName,psw;
    Boolean state=false;
    Thread thread1;

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
        et_user_name=findViewById(R.id.et_user_name);
        et_psw=findViewById(R.id.et_psw);

        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        et_user_name.setText(sp.getString("username", null));
        et_psw.setText(sp.getString("password", null));
        state=sp.getBoolean("state", false);

//        if(state){
//            Toast.makeText(UserLoginActivity.this, "已登录", Toast.LENGTH_SHORT).show();
//            Intent intent=new Intent(UserLoginActivity.this, UserInfoActivity.class);
//            startActivity(intent);
//        }

        //注册按钮
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UserLoginActivity.this, UserRegisterActivity.class);
                startActivity(intent);
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
                    thread1=new Thread(runnable);
                    thread1.start();

//                    if (!state) {
//                        Toast.makeText(UserLoginActivity.this, "该用户不存在", Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(UserLoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
//
//                    }
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            //是获取注册界面回传过来的用户名
            Toast.makeText(UserLoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
            // getExtra().getString("***");
            String userName=data.getStringExtra("userName");
            if(!TextUtils.isEmpty(userName)){
                //设置用户名到 et_user_name 控件
                et_user_name.setText(userName);
                //et_user_name控件的setSelection()方法来设置光标位置
                et_user_name.setSelection(userName.length());
            }
        }
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
            String post=userLogin(userName,psw);
            Log.i("post request", userName+" "+psw);
            Log.i("post response",post);


//            解析json
            try {
//                JSONArray jsonArray=new JSONArray(post);

                if (post.equals("not exsits")){
                    state=false;
                }
                else{
                    state=true;
                    SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
                    sp.edit().putString("username", userName).putString("password", psw).putBoolean("state",true).apply();
                    UserLoginActivity.this.finish();
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
    public static String userLogin(String username, String password){

        String h="http://175.24.72.189/index.php?r=user/login";
        Map<String,Object> mmap=new LinkedHashMap<>();
        mmap.put("userName", username);
        mmap.put("password", password);
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        return postInfo.postMethod();
    }
}
