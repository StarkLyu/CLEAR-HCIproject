package com.example.clear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.gson.Gson;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements AMapLocationListener, PoiSearch.OnPoiSearchListener, View.OnClickListener, TextWatcher
{
    // protection level 的区间划分
    private double level_1=1000;
    private double level_2=5000;

    private MapView mapView;
    private AMap aMap;
    private UiSettings mUiSettings;//定义一个UiSettings对象

    private static final int MY_PERMISSIONS_REQUEST_CALL_LOCATION = 1;
    public AMapLocationClient mLocationClient;
    public AMapLocationClientOption mLocationOption = null;

    //定位蓝点
    MyLocationStyle myLocationStyle;
    //地点搜索
    private String keyWord = "";// 要输入的poi搜索关键字
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索

    // 组件
    private AutoCompleteTextView searchText;// 输入搜索关键字
    private TextView editCity;// 要输入的城市名字或者城市区号
    private ListView poiListView;
    TextView startTimeView, endTimeView, timePeriodView, protectLevelView;  // 输入组件

    private boolean poisitionIsChosen;    // 判断是否已经选择地点
    String nowCity; //现在定位的城市
    double nowLat, nowLon;  //现在定位的经纬度

    //搜索周围病例的request
    PositionInfo focusPoi;  //查找的位置
    String startTime, endTime;
    int timePeriod, protectionLevel;
    boolean sendNotice;

    Thread thread1, thread2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thread1=new Thread(runnable);

        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        poiListView = findViewById(R.id.poi_list);
        init();
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
                for (int i=0; i<length; i++){
                    JSONObject obj=jsonArray.getJSONObject(i);
                    double lat=obj.getDouble("latitude");
                    double lon=obj.getDouble("longitude");
                    double level=obj.getDouble("level");
//                    Log.i("one position", i+" "+lat+" "+lon+" "+level);

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
                    String searchAresult=p_pname+" "+p_starttime+" "+p_endtime+" "+p_period+" "+p_plevel;
                    searchResult.add(searchAresult);


//                    Bitmap virusBitmap;
////                    自定义marker
//                    if (level<level_1){
//                        virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus_1);
//                    }
//                    else if(level<level_2){
//                        virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus_2);
//                    }
//                    else{
//                        virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus_3);
//                    }
////                    Bitmap virusBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.virus);
//                    virusBitmap= Bitmap.createScaledBitmap(virusBitmap, 100, 100, false);
//                    BitmapDescriptor virusIcon = BitmapDescriptorFactory.fromBitmap(virusBitmap);
                    LatLng latLng = new LatLng(lat, lon);
                    MarkerOptions markerOptions = new MarkerOptions()
                            //必须，设置经纬度
                            .position(latLng);
//                    markerOptions.icon(virusIcon);

                    aMap.addMarker(markerOptions);
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                }
            } catch (JSONException e) {
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



    private void init(){
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

                    // 开子线程与后台交互数据

                    thread1.start();

                    nowLat=amapLocation.getLatitude();
                    nowLon=amapLocation.getLongitude();

                    nowCity =amapLocation.getCity();
                    editCity = findViewById(R.id.city);
                    editCity.setText(nowCity);

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
     *开始进行poi搜索
     */
    protected void doSearchQuery() {

        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", editCity.getText().toString());// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();

    }

    /**
     *poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        Toast.makeText(MainActivity.this, infomation, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    final List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始

                    //将list数据传到poi list fragment中
                    List poiListResult = new ArrayList<>();
                    for (int i=0; i<poiItems.size(); i++){
                        String item=poiItems.get(i).toString();
//                        Log.i("poiItem"+i, item);
                        poiListResult.add(item);
                    }
                    poisitionIsChosen=false;    // listview点击事件初始为false
//                    Log.i("check line", "poi search");

                    ArrayAdapter<String> adapter = new ArrayAdapter<>
                            (this, android.R.layout.simple_list_item_1, poiListResult);
                    poiListView.setAdapter(adapter);
                    //设置listview点击事件
                    poiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            PoiItem item = poiItems.get(i);
//                            Toast.makeText(MainActivity.this, item.toString(), Toast.LENGTH_SHORT).show();

                            Log.i("choose position",
                                    item.getPoiId()+" "+item.getLatLonPoint().getLatitude()+" "+item.getLatLonPoint().getLongitude()+" "+item.getTitle()+" "+item.getCityName());

                            // 存储搜索的地点信息
                            focusPoi=new PositionInfo();
                            focusPoi.setPositionID(item.getPoiId());
                            focusPoi.setPositionName(item.getTitle());
                            focusPoi.setLatitude(item.getLatLonPoint().getLatitude());
                            focusPoi.setLongitude(item.getLatLonPoint().getLongitude());
                            focusPoi.setCity(item.getCityName());

                            poisitionIsChosen=true;
                            searchText.setText(item.toString());
                        }
                    });

                    // 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
                    if (poiItems != null && poiItems.size() > 0) {
//                        aMap.clear();// 清理之前的图标
//                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
//                        poiOverlay.removeFromMap();
//                        poiOverlay.addToMap();
//                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        Toast.makeText(MainActivity.this, "没有结果", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "没有结果", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, rCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     *设置页面监听
     */
    private void setUpViewListener() {
        Button searButton = findViewById(R.id.search_button);
        searButton.setOnClickListener(this);
        Button nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);
        Button searchIconButton =findViewById(R.id.search_icon);
        searchIconButton.setOnClickListener(this);
        Button changeToListButton =findViewById(R.id.change_to_list);
        changeToListButton.setOnClickListener(this);

        searchText = findViewById(R.id.keyWord);
        searchText.addTextChangedListener(this);// 添加文本输入框监听事件
        editCity = findViewById(R.id.city);
        editCity.setText(nowCity);  // 城市默认为当前定位的城市

        startTimeView=findViewById(R.id.start_time);
        endTimeView=findViewById(R.id.end_time);
        timePeriodView=findViewById(R.id.time_period);
        protectLevelView=findViewById(R.id.protection_level);
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

    /**
     * 点击空白处隐藏Listview
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 隐藏listview布局

            ListView view=poiListView;
            LinearLayout layout=findViewById(R.id.search_input);

            int[] location = {0, 0};
            int[] location2={0,0};

            // 获取当前view在屏幕中离四边的边距
            view.getLocationInWindow(location);
            layout.getLocationInWindow(location2);

            int left = location[0], top = location2[1], right = view.getWidth(),
                    bottom = location[1] + view.getHeight();

            // 判断点击位置是否在view布局范围内
            if (view.getVisibility()==View.VISIBLE){
                if (ev.getRawX() < left || ev.getRawX() > right
                        || ev.getY() < top || ev.getRawY() > bottom) {
                    // 在这里执行你的操作，我的是判断当前布局显示的话，隐藏掉
                    view.setVisibility(View.GONE);
                }
            }
//            else  {
//                if (ev.getY() > top || ev.getRawY() < (location2[1]+layout.getHeight())){
//                    view.setVisibility(View.VISIBLE);
//                    Log.i("show search result", "点击的是搜索框");
//                }
//                else {
//                    view.setVisibility(View.GONE);
//                    Log.i("show search result", "点击的是搜索框外的地图");
//                }
//            }


//            poiListView.setVisibility(View.GONE);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击搜索按钮
            case R.id.search_button:
                searchButton();
                break;
            //点击下一页按钮
            case R.id.next_button:
                nextButton();
                break;
            case R.id.search_icon:
                searchIconButton();
                break;
            case R.id.change_to_list:
                changeResultToList();
                break;
            default:
                break;
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
        timePeriod=Integer.parseInt(timePeriodView.getText().toString());
        Log.i("time period", timePeriod+"");
        protectionLevel=Integer.parseInt(protectLevelView.getText().toString());
        Log.i("protection level", protectionLevel+"");

        thread2=new Thread(runnable2);
        thread2.start();
    }

    /**
     * 点击下一页按钮
     */
    public void nextButton() {
        if (query != null && poiSearch != null && poiResult != null) {
            if (poiResult.getPageCount() - 1 > currentPage) {
                currentPage++;
                query.setPageNum(currentPage);// 设置查后一页
                poiSearch.searchPOIAsyn();
            } else {
                Toast.makeText(this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        poiListView.setVisibility(View.GONE);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String content=s.toString().trim();//获取自动提示输入框的内容
        if ("".equals(content)) {
            Toast.makeText(this, "请输入关键词", Toast.LENGTH_SHORT).show();
//            aMap.clear();
        }
        else {
            keyWord=content;
            String city = editCity.getText().toString();
            if (poisitionIsChosen) {
//                aMap.clear();
                LatLng latLng = new LatLng(focusPoi.getLatitude(), focusPoi.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        //必须，设置经纬度
                        .position(latLng);
                aMap.addMarker(markerOptions);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

            }else{
                doSearchQuery();
            }
            Log.i("城市", city);
            Log.i("自动提示框", content);
        }
    }

    /**
     *在确认点击条目后，搜索栏会隐藏
     */
    @Override
    public void afterTextChanged(Editable s) {
        if(poisitionIsChosen){
            poiListView.setVisibility(View.GONE);
            poisitionIsChosen=false;
//            Log.i("afterTextChanged", "true");
        }
        else{
            poiListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     *处理get请求
     */
    public static String getHttpResult(String urlStr){
        try {
            URL url=new URL(urlStr);
            HttpURLConnection connect=(HttpURLConnection)url.openConnection();
            InputStream input=connect.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;
            System.out.println(connect.getResponseCode());
            StringBuffer sb = new StringBuffer();
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }

    /**
     *post当前位置，获得病例信息
     */
    public static String getHomepage(double lat, double lon, String city){

        String h="http://175.24.72.189/index.php?r=normal/homepage";
        Map<String,Object> mmap=new LinkedHashMap<>();
        mmap.put("latitude", lat);
        mmap.put("longitude", lon);
        mmap.put("city", city);
        Gson gson=new Gson();
        String json=gson.toJson(mmap);
        PostInfo postInfo=new PostInfo(h,json);
        return postInfo.postMethod();

    }

    /**
     *post search的地点，获得病例信息
     */
    public static String getSearchPosition(PositionInfo positionInfo, String starttime, String endtime, int period, int protectionLevel, Boolean notice){

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
        return postInfo.postMethod();

    }

}
