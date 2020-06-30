package com.example.clear;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.services.core.PoiItem;
import com.example.clear.danger.DangerCalculation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity {

    private Button btn_relogin, btn_return;
    public TextView roleText;
    private ImageView img;
    int role, recordID;
    Boolean sendNotice;
    String authToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        init();
    }

    private void init() {
        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        authToken =sp.getString("authToken",null);
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

        //用每个用户的username生成二维码
        String userName=sp.getString("username",null);
        img = findViewById(R.id.myimg);
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Bitmap bitmap = CodeCreator.createQRCode(userName, 400, 400, logo);
        img.setImageBitmap(bitmap);

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

        TextView textView=findViewById(R.id.history_title);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView resultListView;
                resultListView = findViewById(R.id.history_search_result_list);
                if(resultListView.getVisibility()==View.VISIBLE){
                    resultListView.setVisibility(View.GONE);
                }
                else {
                    resultListView.setVisibility(View.VISIBLE);
                }

            }
        });

        Thread thread1;
        thread1=new Thread(runnable);
        thread1.start();
    }

    /**
     * 返回该用户的搜索记录
     */
    public String userSearchHistory(){

        String h="http://175.24.72.189/index.php?r=normal/history";
        Map<String,Object> mmap=new LinkedHashMap<>();
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        postInfo.setToken(authToken);
        return postInfo.postMethod();

    }

    /**
     * 更改对应搜索记录的通知状态
     */
    public String changeSearchRecordNotice(int recordID, Boolean notice){
        String h="http://175.24.72.189/index.php?r=normal/notice";
        Map<String,Object> mmap=new LinkedHashMap<>();
        mmap.put("recordID", recordID);
        mmap.put("notice", notice);
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        postInfo.setToken(authToken);
        return postInfo.postMethod();
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
            String post = userSearchHistory();
            Log.i("post response", post);

            if (!post.equals("401")) {
                //            解析json
                final List searchHistory = new ArrayList<>();
                final List<SearchResultUnit> results = new ArrayList<>();
                final ArrayList <Map<String,Object>> history_results = new ArrayList<>();

                try {
                    JSONArray jsonArray = new JSONArray(post);
                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String p_starttime = obj.getString("startTime");
                        String p_endtime = obj.getString("endTime");
                        int p_period = obj.getInt("timePeriod");
                        int p_plevel = obj.getInt("protectionLevel");
                        int search_record_id=obj.getInt("sr_id");
                        int send_notice=obj.getInt("sendNotice");
                        String p_poiid=obj.getString("positionID");
                        JSONObject p_position = obj.getJSONObject("position");
                        String p_pname = p_position.getString("positionName");
                        double lat=p_position.getDouble("latitude");
                        double lon=p_position.getDouble("longitude");
                        String city=p_position.getString("city");
                        PositionInfo p_info=new PositionInfo(p_poiid, p_pname, lat, lon, city);

                        p_period = p_period / 60;
                        String p_level_detail;
                        switch (p_plevel) {
                            case 0:
                                p_level_detail = "无防护措施";
                                break;
                            case 1:
                                p_level_detail = "口罩";
                                break;
                            default:
                                p_level_detail = "防护服";
                                break;
                        }
                        String notice_status="";
                        if (send_notice==0){
                            notice_status="未通知";
                        }
                        else{
                            notice_status="已通知";
                        }

                        results.add(new SearchResultUnit(p_info, p_starttime, p_endtime,search_record_id,send_notice, p_period, p_plevel));
                        String searchAresult = "地点：" + p_pname +
                                "\n时间：" + p_starttime + " 到 " + p_endtime +
                                "\n停留时长：" + p_period +
                                "分钟\n防护措施：" + p_level_detail+
                                "\n是否通知："+notice_status;
                        searchHistory.add(searchAresult);

//                        Map<String, Object> map;
//                        map = new HashMap<>();
//                        map.put("name", searchAresult);
//                        if (send_notice==1){
//                            map.put("button", "取消通知");
//                        }
//                        else{
//                            map.put("button", "通知");
//                        }
//
//                        history_results.add(map);
                    }

                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

                //展示在listview中
                UserInfoActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Collections.reverse(history_results);
                        Collections.reverse(searchHistory);
                        ListView resultListView;
                        resultListView = findViewById(R.id.history_search_result_list);

//                        SearchHistoryItemAdapter adapter = new SearchHistoryItemAdapter(UserInfoActivity.this,history_results,R.layout.item_search_history,
//                                new String[]{"name", "button"},
//                                new int[]{R.id.search_history, R.id.notice_switch});
//                        resultListView.setAdapter(adapter);
//                        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//
//                            @Override
//                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                                Log.i("我点击了item",i+"");
//                            }
//                        });

                        ArrayAdapter<String> adapter = new ArrayAdapter<>
                                (UserInfoActivity.this, android.R.layout.simple_list_item_1, searchHistory);
                        resultListView.setAdapter(adapter);

                        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                final SearchResultUnit item = results.get(searchHistory.size()-1-i);

                                final String[] items = { "返回搜索状态","更改通知状态"};
                                AlertDialog.Builder listDialog =
                                        new AlertDialog.Builder(UserInfoActivity.this);
                                listDialog.setTitle("选择你的操作");
                                listDialog.setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Log.i("您点击了", items[which]);
                                        recordID=item.recordId;
                                        if(item.sendNotice==1){
                                            sendNotice=false;
                                        }
                                        else{
                                            sendNotice=true;
                                        }

                                        Log.i("post information", recordID+" "+authToken);

                                        if (which==0){
                                            Intent intent = new Intent();
                                            intent.putExtra("record id", item.recordId);
                                            intent.putExtra("position", item.position.positionName);
                                            intent.putExtra("starttime",item.starttimeStr);
                                            intent.putExtra("endtime",item.endtimeStr);
                                            intent.putExtra("period",item.period);
                                            intent.putExtra("protection",item.protectionLevel);
                                            setResult(2, intent);
                                            finish();
                                        }
                                        else{
                                            Thread thread2;
                                            thread2=new Thread(runnable2);
                                            thread2.start();
                                        }
                                    }
                                });
                                listDialog.show();
                            }
                        });
                    }
                });

                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("value", "请求结果");

                msg.setData(data);
                handler.sendMessage(msg);
            }
        }

    };

    Handler handler2= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");

        }
    };

    /**
     * 更改用户通知状态
     */
    Runnable runnable2=new Runnable(){
        @Override
        public void run() {
            String post=changeSearchRecordNotice(recordID, sendNotice);
            Log.i("post response : changeSearchRecordNotice",post);

            switch (post){
                case "success":
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"更改成功",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case "400":
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"搜索记录不正确",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                default:

                    break;
            }

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value","请求结果");

            msg.setData(data);
            handler.sendMessage(msg);
        }
    };
}
