package com.example.clear;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PatientOftenActivity extends AppCompatActivity {


    private int year,month,day;
    private Calendar cal;

    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private String keyWord="";
    private boolean poisitionIsChosen;    // 判断是否已经选择地点

    ListView poiListView;
    PositionInfo focusPoi;  //查找的位置
    private AutoCompleteTextView searchText;// 输入搜索关键字

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_often);

        Button btn_new=findViewById(R.id.btn_new);
        btn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PatientOftenActivity.this,PatientOftenActivity.class);
                startActivity(intent);
            }
        });

        Button btn_next=findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PatientOftenActivity.this,PatientPast1Activity.class);
                startActivity(intent);
            }
        });

        Button btn_back=findViewById(R.id.btn_back_1);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PatientOftenActivity.this,PatientInActivity.class);
                startActivity(intent);
            }
        });

        getDate();
//        Calendar c = Calendar.getInstance();
//
//        String mYear = String.valueOf(c.get(Calendar.YEAR));
//        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);
//        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
//        String mHour =String.valueOf(c.get(Calendar.HOUR_OF_DAY));

        final EditText date1=findViewById(R.id.ed_gender);
        final EditText date2=findViewById(R.id.ed_phone);

        poiListView=findViewById(R.id.search_list);


        date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        date1.setText(y[0]+"-"+(++m[0])+"-"+d[0]+" "+hour+":"+min+":00");
                    }
                };
                TimePickerDialog dialog1=new TimePickerDialog(PatientOftenActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, timeListener, 0,0,true);
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
                DatePickerDialog dialog=new DatePickerDialog(PatientOftenActivity.this, DatePickerDialog.THEME_HOLO_LIGHT,listener,year,month,day);
                dialog.show();
            }
        });


        date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        date2.setText(y[0]+"-"+(++m[0])+"-"+d[0]+" "+hour+":"+min+":00");
                    }
                };
                TimePickerDialog dialog1=new TimePickerDialog(PatientOftenActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, timeListener, 0,0,true);
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
                DatePickerDialog dialog=new DatePickerDialog(PatientOftenActivity.this, DatePickerDialog.THEME_HOLO_LIGHT,listener,year,month,day);
                dialog.show();
            }
        });

//        onPoiSearched(poiResult,rCode);
    }


    private void getDate() {
        cal=Calendar.getInstance();
        year=cal.get(Calendar.YEAR);       //获取年月日时分秒
        month=cal.get(Calendar.MONTH);   //获取到的月份是从0开始计数
        day=cal.get(Calendar.DAY_OF_MONTH);
    }

//    protected void doSearchQuery() {
//
//        currentPage = 0;
//        query = new PoiSearch.Query(keyWord, "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
//        query.setPageSize(10);// 设置每页最多返回多少条poiitem
//        query.setPageNum(currentPage);// 设置查第一页
//
//        poiSearch = new PoiSearch(this, query);
//        poiSearch.setOnPoiSearchListener(this);
//        poiSearch.searchPOIAsyn();

//    }
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
//                        showSuggestCity(suggestionCities);
                    } else {
//                        showToast("没有结果");
                    }
                }
            } else {
//                showToast("没有结果");
            }
        } else {
//            showToast(rCode+"");
        }
    }

}
