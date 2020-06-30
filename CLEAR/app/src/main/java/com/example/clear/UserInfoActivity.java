package com.example.clear;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Context;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity {

    private Button btn_relogin, btn_return;
    public TextView roleText;
    private ImageView img;
    int role;
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

                        results.add(new SearchResultUnit(p_info, p_starttime, p_endtime,search_record_id, p_period, p_plevel));
                        String searchAresult = "地点：" + p_pname + "\n时间：" + p_starttime + " 到 " + p_endtime + "\n停留时长：" + p_period + "分钟\n防护措施：" + p_level_detail;
                        searchHistory.add(searchAresult);

                    }

                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
                //展示在listview中
                UserInfoActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>
                                (UserInfoActivity.this, android.R.layout.simple_list_item_1, searchHistory);
                        ListView resultListView;
                        resultListView = findViewById(R.id.history_search_result_list);
                        resultListView.setAdapter(adapter);

                        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                SearchResultUnit item = results.get(i);

                                Log.i("choose position",
                                        item.ResultString());

                                Intent intent = new Intent();
                                intent.putExtra("record id", item.recordId);
                                intent.putExtra("position", item.position.positionName);
                                intent.putExtra("starttime",item.starttimeStr);
                                intent.putExtra("endtime",item.endtimeStr);
                                intent.putExtra("period",item.period);
                                intent.putExtra("protection",item.protectionLevel);
                                setResult(2, intent);
                                finish();

//                                // 存储搜索的地点信息
//                                focusPoi=new PositionInfo();
//                                focusPoi.setPositionID(item.getPoiId());
//                                focusPoi.setPositionName(item.getTitle());
//                                focusPoi.setLatitude(item.getLatLonPoint().getLatitude());
//                                focusPoi.setLongitude(item.getLatLonPoint().getLongitude());
//                                focusPoi.setCity(item.getCityName());
//
//                                poisitionIsChosen=true;
//                                searchText.setText(item.toString());
//
//                                String code="fragment向activity传值成功";
//                                listener.fragToAct(focusPoi);
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
}
