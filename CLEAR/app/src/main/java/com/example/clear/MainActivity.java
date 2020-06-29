package com.example.clear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.TileOverlayOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.clear.danger.DangerCalculation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;


import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements AMapLocationListener, View.OnClickListener, PoiSearchFragment.Mylistener
{
    // protection level 的区间划分
    private double level_1=1000;
    private double level_2=5000;
    private int mode=0; // 0表示热图，1表示点

    private MapView mapView;
    private AMap aMap;
    private UiSettings mUiSettings;//定义一个UiSettings对象

    private static final int MY_PERMISSIONS_REQUEST_CALL_LOCATION = 1;
    public AMapLocationClient mLocationClient;
    public AMapLocationClientOption mLocationOption = null;

    //定位蓝点
    MyLocationStyle myLocationStyle;

    // 组件
    TextView startTimeView, endTimeView, timePeriodView;  // 输入组件
    private Calendar cal;   //当前时间
    private int year,month,day;
    private FragmentManager fragmentManager;  //Fragment 管理器
    private FragmentTransaction fragmentTransaction;  //Fragment 事务处理
    PoiSearchFragment fragment1;

    String nowCity; //现在定位的城市
    double nowLat, nowLon;  //现在定位的经纬度

    //搜索周围病例的request
    PositionInfo focusPoi;  //查找的位置
    String startTime, endTime, authToken, scanUserName;
    int timePeriod, protectionLevel, role=0;  //role代表用户角色
    boolean sendNotice, isLogin, isLocated=false;

    Thread thread1, thread2, thread3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thread1=new Thread(runnable);

        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        loginState();
        mapInit();
        getDate();
        setUpViewListener();

        //检查版本是否大于M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_CALL_LOCATION);
            } else {
                //"权限已申请";
                showLocation();
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
            String post=getHomepage(nowLat,nowLon,nowCity);
            Log.i("post request", nowLat+" "+nowLon+" "+nowCity);
            Log.i("post response",post);

//            解析json
            try {
                JSONArray jsonArray=new JSONArray(post);
                int length=jsonArray.length();

                LatLng[] latlngs = new LatLng[length];

                for (int i=0; i<length; i++){
                    JSONObject obj=jsonArray.getJSONObject(i);
                    double lat=obj.getDouble("latitude");
                    double lon=obj.getDouble("longitude");
                    double level=obj.getDouble("level");
//                    Log.i("one position", i+" "+lat+" "+lon+" "+level);

                    latlngs[i] = new LatLng(lat, lon);


                    if(mode==1){
                        Bitmap virusBitmap;
//                    自定义marker
                        if (level<level_1){
                            virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus_1);
                        }
                        else if(level<level_2){
                            virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus_2);
                        }
                        else{
                            virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus_3);
                        }
//                    Bitmap virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus);
                        virusBitmap= Bitmap.createScaledBitmap(virusBitmap, 100, 100, false);
                        BitmapDescriptor virusIcon = BitmapDescriptorFactory.fromBitmap(virusBitmap);
                        LatLng latLng = new LatLng(lat, lon);
                        MarkerOptions markerOptions = new MarkerOptions()
                                //必须，设置经纬度
                                .position(latLng);
                        markerOptions.icon(virusIcon);

                        aMap.addMarker(markerOptions);

                    }


                }
                if(mode==0){
                    // 构建热力图 HeatmapTileProvider
                    HeatmapTileProvider.Builder builder = new HeatmapTileProvider.Builder();
                    builder.data(Arrays.asList(latlngs)); // 设置热力图渐变，有默认值 DEFAULT_GRADIENT，可不设置该接口
                    // Gradient 的设置可见参考手册
                    // 构造热力图对象
                    HeatmapTileProvider heatmapTileProvider = builder.build();
                    // 初始化 TileOverlayOptions
                    TileOverlayOptions tileOverlayOptions = new TileOverlayOptions();
                    tileOverlayOptions.tileProvider(heatmapTileProvider); // 设置瓦片图层的提供者
                    // 向地图上添加 TileOverlayOptions 类对象
                    aMap.addTileOverlay(tileOverlayOptions);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value","请求结果");

            msg.setData(data);
            handler.sendMessage(msg);
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

    Runnable runnable2=new Runnable(){
        @Override
        public void run() {
            String post=getSearchPosition(focusPoi, startTime, endTime, timePeriod, protectionLevel, false);
            Log.i("post response",post);
            final List searchResult = new ArrayList<>();
            List<SearchResultUnit> results = new ArrayList<>();
            aMap.clear();

//            解析json
            try {
                JSONArray jsonArray=new JSONArray(post);
                int length=jsonArray.length();
                for (int i=0; i<length; i++){
                    JSONObject obj=jsonArray.getJSONObject(i);
                    String p_starttime=obj.getString("startTime");
                    String p_endtime=obj.getString("endTime");
                    int p_period=obj.getInt("timePeriod");
                    int p_plevel=obj.getInt("protectionLevel");
                    String p_poiid=obj.getString("positionID");
                    JSONObject p_position=obj.getJSONObject("position");
                    String p_pname=p_position.getString("positionName");
                    double lat=p_position.getDouble("latitude");
                    double lon=p_position.getDouble("longitude");
                    String city=p_position.getString("city");
                    PositionInfo p_info=new PositionInfo(p_poiid, p_pname, lat, lon, city);
                    String searchAresult="地点："+p_pname+" 时间："+p_starttime+" "+p_endtime+" "+p_period+" "+p_plevel;
                    searchResult.add(searchAresult);

                    results.add(new SearchResultUnit(p_info, p_starttime, p_endtime, p_period, p_plevel));

//                    自定义marker
                    Bitmap virusBitmap;
                    virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus_2);
                    virusBitmap= Bitmap.createScaledBitmap(virusBitmap, 100, 100, false);
                    BitmapDescriptor virusIcon = BitmapDescriptorFactory.fromBitmap(virusBitmap);
                    LatLng latLng = new LatLng(lat, lon);
                    MarkerOptions markerOptions = new MarkerOptions()
                            //必须，设置经纬度
                            .position(latLng);
                    markerOptions.icon(virusIcon);

                    aMap.addMarker(markerOptions);
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                }

                // 这里把DangerCalculation的构造函数参数补上，然后算出来的dangerRate应该就是算出来的结果了
//                DangerCalculation dangerCal = new DangerCalculation();
//                float dangerRate = dangerCal.Danger(results);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
            //展示在listview中
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>
                            (MainActivity.this, android.R.layout.simple_list_item_1, searchResult);
                    ListView resultListView;
                    resultListView=findViewById(R.id.search_result_list);
                    resultListView.setAdapter(adapter);
                    resultListView.setVisibility(View.GONE);
                }
            });
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value","请求结果");

            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    Handler handler3= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");

        }
    };

    Runnable runnable3=new Runnable(){
        @Override
        public void run() {
            String post=changeUserToPatient(scanUserName);
            Log.i("post response",post);

            switch (post){
                case "200":
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"授权患者成功",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case "400":
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"未输入患者的用户名",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case "401":
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"未登录或不是医生",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case "403":
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"患者不存在",Toast.LENGTH_SHORT).show();
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

    private void loginState(){
        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        role=sp.getInt("role",0);
        authToken =sp.getString("authToken",null);
        isLogin=sp.getBoolean("state", false);
    }

    private void mapInit(){
        if(aMap ==null){
            aMap = mapView.getMap();
            mUiSettings=aMap.getUiSettings();

            //设置地图的放缩级别
            aMap.moveCamera(CameraUpdateFactory.zoomTo(14));

            //蓝点初始化
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.showMyLocation(true);
//            自定义定位图标
            Bitmap locateBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.location);
            locateBitmap= Bitmap.createScaledBitmap(locateBitmap, 100, 100, false);
            BitmapDescriptor myLocationIcon = BitmapDescriptorFactory.fromBitmap(locateBitmap);
            myLocationStyle.myLocationIcon(myLocationIcon);
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 自定义精度范围的圆形边框颜色
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//圆圈的颜色,设为透明的时候就可以去掉园区区域了
            myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
            aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style

//            高德地图控件
            mUiSettings.setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
            mUiSettings.setZoomControlsEnabled(false);
            mUiSettings.setCompassEnabled(true);
            aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //"权限已申请"
                showLocation();
            } else {
                showToast("权限已拒绝,不能定位");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showLocation() {
        try {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            mLocationClient.setLocationListener(this);
            //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(5000);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            //启动定位
            mLocationClient.startLocation();

        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
        // 停止定位
        if (null != mLocationClient) {
            mLocationClient.stopLocation();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        try {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息

                    //获取当前定位结果来源，如网络定位结果，详见定位类型表
                    /*
                    Log.i("定位id", amapLocation.getAoiName()+ "");
                    Log.i("定位类型", amapLocation.getLocationType() + "");
                    Log.i("获取纬度", amapLocation.getLatitude() + "");
                    Log.i("获取经度", amapLocation.getLongitude() + "");
                    Log.i("获取精度信息", amapLocation.getAccuracy() + "");
*/
                    //如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    /*
                    Log.i("地址", amapLocation.getAddress());
                    Log.i("国家信息", amapLocation.getCountry());
                    Log.i("省信息", amapLocation.getProvince());
                    Log.i("城市信息", amapLocation.getCity());
                    Log.i("城区信息", amapLocation.getDistrict());
                    Log.i("街道信息", amapLocation.getStreet());
                    Log.i("街道门牌号信息", amapLocation.getStreetNum());
                    Log.i("城市编码", amapLocation.getCityCode());
                    Log.i("地区编码", amapLocation.getAdCode());
                    Log.i("获取当前定位点的AOI信息", amapLocation.getAoiName());
                    Log.i("获取当前室内定位的建筑物Id", amapLocation.getBuildingId());
                    Log.i("获取当前室内定位的楼层", amapLocation.getFloor());
                    Log.i("获取GPS的当前状态", amapLocation.getGpsAccuracyStatus() + "");
*/
                    //获取定位时间
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(amapLocation.getTime());

                    Log.i("获取定位时间", df.format(date));

                    nowLat=amapLocation.getLatitude();
                    nowLon=amapLocation.getLongitude();

                    nowCity =amapLocation.getCity();

                    if(!isLocated){
                        fragment1=new PoiSearchFragment(nowCity);
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_poi_search,fragment1);
                        fragmentTransaction.commit();
                    }

                    isLocated=true;
                    mode=1;
                    // 开子线程与后台交互数据
                    thread1.start();

                    // 停止定位
//                    mLocationClient.stopLocation();
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        } catch (Exception e) {
        }
    }

    private void showToast(String string) {
        Toast.makeText(MainActivity.this, string, Toast.LENGTH_LONG).show();
    }

    /**
     *设置页面监听
     */
    private void setUpViewListener() {
        Button userButton=findViewById(R.id.user_icon);
        userButton.setOnClickListener(this);
        Button searButton = findViewById(R.id.search_button);
        searButton.setOnClickListener(this);
        Button searchIconButton =findViewById(R.id.search_icon);
        searchIconButton.setOnClickListener(this);
        Button changeToListButton =findViewById(R.id.change_to_list);
        changeToListButton.setOnClickListener(this);
        Button mapIconButton =findViewById(R.id.map_icon);
        mapIconButton.setOnClickListener(this);
        FloatingActionButton scanButton=findViewById(R.id.scan);
        scanButton.setOnClickListener(this);
        FloatingActionButton addButton=findViewById(R.id.fab_addTask);
        addButton.setOnClickListener(this);

        //角色不同，可见控件不同
        if(role==1){
            addButton.setVisibility(View.VISIBLE);
        }
        else if (role==2){
            addButton.setVisibility(View.VISIBLE);
            scanButton.setVisibility(View.VISIBLE);
        }

        startTimeView=findViewById(R.id.start_time);
        startTimeView.setOnClickListener(this);
        endTimeView=findViewById(R.id.end_time);
        endTimeView.setOnClickListener(this);
        timePeriodView=findViewById(R.id.time_period);
//        protectLevelView=findViewById(R.id.protection_level);

        // 设置下拉框的监听
        Spinner mSpinner = findViewById(R.id.protect_level_spinner);
        ArrayList<String> list = new ArrayList<>();

        list.add("请选择您的防护措施");
        //为下拉列表定义一个适配器
        final ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        //设置下拉菜单样式。
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //添加数据
        mSpinner.setAdapter(ad);
        list.add("无防护措施");
        list.add("口罩");
        list.add("防护服");
        mSpinner.setSelection(0,true);//选中默认值
        list.remove(0);
        //点击响应事件
        mSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                protectionLevel=arg2;
//                showToast(protectionLevel+"");
            }
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    /**
     * 获取当前日期
     */
    private void getDate() {
        cal=Calendar.getInstance();
        year=cal.get(Calendar.YEAR);       //获取年月日时分秒
        month=cal.get(Calendar.MONTH);   //获取到的月份是从0开始计数
        day=cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 按下返回回到桌面
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击用户按钮
            case R.id.user_icon:
                changeToUser();
                break;
            //点击搜索按钮
            case R.id.search_button:
                searchButton();
                break;
            case R.id.search_icon:
                searchIconButton();
                break;
            case R.id.change_to_list:
                changeResultToList();
                break;
            case R.id.map_icon:
                showInitMap();
                break;
            case R.id.start_time:
                showStartTime();
                break;
            case R.id.end_time:
                showEndTime();
                break;
            case R.id.scan:
                scan();
                break;
            case R.id.fab_addTask:
                addPatientInfo();
                break;
            default:
                break;
        }
    }

    /**
     * 点击扫描二维码的按钮
     */
    public void scan(){
        Log.i("scan", "点击了扫描二维码按钮");
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, 200);
    }

    /**
     * 点击添加信息按钮
     */
    public void addPatientInfo(){
        Intent intent=new Intent(MainActivity.this,PatientInActivity.class);
        startActivity(intent);
    }

    /**
     *点击用户按钮
     */
    public void changeToUser() {

        if(!isLogin){
            Intent intent=new Intent(MainActivity.this, UserLoginActivity.class);
            startActivityForResult(intent,100);
        }
        else{
            Intent intent=new Intent(MainActivity.this, UserInfoActivity.class);
            startActivityForResult(intent,100);
        }

    }

    /**
     *点击搜索按钮
     */
    public void searchButton() {

        startTime=startTimeView.getText().toString();
        Log.i("start time",startTime);
        endTime=endTimeView.getText().toString();
        Log.i("end time", endTime);
        timePeriod=Integer.parseInt(timePeriodView.getText().toString())*60;
        Log.i("time period", timePeriod+"");
//        protectionLevel=Integer.parseInt(protectLevelView.getText().toString());
        Log.i("protection level", protectionLevel+"");

        thread2=new Thread(runnable2);
        thread2.start();
    }

    /**
     * 点击search icon
     */
    public void searchIconButton(){
        LinearLayout layout=findViewById(R.id.whole_search);
        if (layout.getVisibility()==View.VISIBLE){
            layout.setVisibility(View.GONE);
        }
        else{
            layout.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 点击以列表展示按钮
     */
    public void changeResultToList(){
        ListView resultListView;
        resultListView=findViewById(R.id.search_result_list);
        if (resultListView.getVisibility()==View.VISIBLE){
            resultListView.setVisibility(View.GONE);
            Log.i("search result", "gone");
        }
        else{
            resultListView.setVisibility(View.VISIBLE);
            Log.i("search result", "visible");
        }
    }

    /**
     * 点击显示最初的定位地图
     */
    public void showInitMap(){
        aMap.clear();
        mode=1-mode;
        Thread thread =new Thread(runnable);
        thread.start();

    }

    /**
     * 点击start time文本框弹出日期选择器
     */
    public void showStartTime(){
        final int[] y = new int[1];
        final int[] m = new int[1];
        final int[] d = new int[1];
        TimePickerDialog.OnTimeSetListener timeListener=new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hour,min;
                if (hourOfDay<10) {
                    hour="0"+hourOfDay;
                }else {
                    hour=hourOfDay+"";
                }
                if(minute<10){
                    min="0"+minute;
                }else{
                    min=minute+"";
                }
                startTimeView.setText(y[0]+"-"+(++m[0])+"-"+d[0]+" "+hour+":"+min+":00");
            }
        };
        TimePickerDialog dialog1=new TimePickerDialog(this, TimePickerDialog.THEME_HOLO_LIGHT, timeListener, 0,0,true);
        dialog1.show();

        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker arg0, int year, int month, int day) {
//                startTimeView.setText(year+"-"+(++month)+"-"+day);      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
                y[0] =year;
                m[0] =month;
                d[0] =day;
            }
        };
        //主题在这里！后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
        DatePickerDialog dialog=new DatePickerDialog(MainActivity.this, DatePickerDialog.THEME_HOLO_LIGHT,listener,year,month,day);
        dialog.show();

    }

    /**
     * 点击end time文本框弹出日期选择器
     */
    public void showEndTime(){
        final int[] y = new int[1];
        final int[] m = new int[1];
        final int[] d = new int[1];
        TimePickerDialog.OnTimeSetListener timeListener=new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hour,min;
                if (hourOfDay<10) {
                    hour="0"+hourOfDay;
                }else {
                    hour=hourOfDay+"";
                }
                if(minute<10){
                    min="0"+minute;
                }else{
                    min=minute+"";
                }
                endTimeView.setText(y[0]+"-"+(++m[0])+"-"+d[0]+" "+hour+":"+min+":00");
            }
        };
        TimePickerDialog dialog1=new TimePickerDialog(this, TimePickerDialog.THEME_HOLO_LIGHT, timeListener, 0,0,true);
        dialog1.show();

        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker arg0, int year, int month, int day) {
//                startTimeView.setText(year+"-"+(++month)+"-"+day);      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
                y[0] =year;
                m[0] =month;
                d[0] =day;
            }
        };
        //主题在这里！后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
        DatePickerDialog dialog=new DatePickerDialog(MainActivity.this, DatePickerDialog.THEME_HOLO_LIGHT,listener,year,month,day);
        dialog.show();

    }

    /**
     *post当前位置，获得病例信息
     */
    public String getHomepage(double lat, double lon, String city){

        String h="http://175.24.72.189/index.php?r=normal/homepage";
        Map<String,Object> mmap=new LinkedHashMap<>();
        mmap.put("latitude", lat);
        mmap.put("longitude", lon);
        mmap.put("city", city);
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        postInfo.setToken(authToken);
        return postInfo.postMethod();

    }

    /**
     *post search的地点，获得病例信息
     */
    public String getSearchPosition(PositionInfo positionInfo, String starttime, String endtime, int period, int protectionLevel, Boolean notice){

        String h="http://175.24.72.189/index.php?r=normal/search";
        Map<String,Object> mmap=new LinkedHashMap<>();
        mmap.put("position", positionInfo);
        mmap.put("startTime", starttime);
        mmap.put("endTime", endtime);
        mmap.put("timePeriod", period);
        mmap.put("protectionLevel",protectionLevel);
        mmap.put("sendNotice",notice);
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        postInfo.setToken(authToken);
        return postInfo.postMethod();

    }

    /**
     * post扫描得到的结果，让他变成患者
     */
    public String changeUserToPatient(String username){
        String h="http://175.24.72.189/index.php?r=patient/recognize";
        Map<String,Object> mmap=new LinkedHashMap<>();
        mmap.put("userName", username);
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        postInfo.setToken(authToken);
        return postInfo.postMethod();
    }

    @Override
    public void fragToAct(PositionInfo code) {
        focusPoi=code;
        Log.i("f-a", "已收到Fragment的消息");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //子activity传回来的角色信息
        if(requestCode==100 && resultCode==1){
            role=data.getIntExtra("role",0);

            FloatingActionButton scanButton=findViewById(R.id.scan);
            scanButton.setOnClickListener(this);
            FloatingActionButton addButton=findViewById(R.id.fab_addTask);
            addButton.setOnClickListener(this);

            //角色不同，可见控件不同
            if(role==1){
                addButton.setVisibility(View.VISIBLE);
            }
            else if (role==2){
                addButton.setVisibility(View.VISIBLE);
                scanButton.setVisibility(View.VISIBLE);
            }
            Log.i("role transform main",role+"");
        }

        // 扫描二维码/条码回传
        if (requestCode == 200 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
//                result.setText("扫描结果为：" + content);
                showToast(content);
                scanUserName=content;
                thread3=new Thread(runnable3);
                thread3.start();
            }
        }

    }
}
