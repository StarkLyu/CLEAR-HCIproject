package com.example.clear;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PoiSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

//添加注解
@SuppressLint("ValidFragment")
public class PoiSearchFragment extends Fragment implements TextWatcher, PoiSearch.OnPoiSearchListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View view;

    private Mylistener listener;

    //地点搜索
    private String keyWord = "";// 要输入的poi搜索关键字
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索

    private boolean poisitionIsChosen=false;    // 判断是否已经选择地点
    String nowCity; //现在定位的城市

    //搜索周围病例的request
    PositionInfo focusPoi;  //查找的位置

    // 组件
    private AutoCompleteTextView searchText;// 输入搜索关键字
    private ListView poiListView;

    public interface Mylistener{
        public void fragToAct(PositionInfo info);
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        listener=(Mylistener) activity;
        super.onAttach(activity);
    }

    //添加注解
    @SuppressLint("ValidFragment")
    public PoiSearchFragment(String string) {
        //此时的string就是我们传过来的值 123456
        Log.i("a-f",string);
        nowCity=string;

    }

    public PoiSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PoiSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PoiSearchFragment newInstance(String param1, String param2) {
        PoiSearchFragment fragment = new PoiSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view =inflater.inflate(R.layout.fragment_poi_search, container, false);

        searchText = view.findViewById(R.id.keyWord);
        searchText.addTextChangedListener(this);// 添加文本输入框监听事件
        poiListView = view.findViewById(R.id.poi_list);

        Bundle bundle = getArguments();
        if(bundle!=null){
            String result = bundle.getString("MainActivity_history_position");
//            Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
//            poisitionIsChosen=true;
            searchText.setText(result);
        }

        return view;
    }

    /**
     *开始进行poi搜索
     */
    private void doSearchQuery() {

        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", nowCity);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(view.getContext(), query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
//        Log.i("search", "ok2");

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
        showToast(infomation);
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
                            (getActivity(), android.R.layout.simple_list_item_1, poiListResult);
                    poiListView.setAdapter(adapter);
                    //设置listview点击事件
                    poiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            PoiItem item = poiItems.get(i);

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

                            String code="fragment向activity传值成功";
                            listener.fragToAct(focusPoi);
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
                        showToast("没有结果");
                    }
                }
            } else {
                showToast("没有结果");
            }
        } else {
            showToast(rCode+"");
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     *
     * @param s
     * @param start
     * @param count
     * @param after
     * 下面三个函数都是关于地点搜索框的
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        poiListView.setVisibility(View.GONE);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String content=s.toString().trim();//获取自动提示输入框的内容
        if ("".equals(content)) {
            showToast("请输入关键词");
//            aMap.clear();
        }
        else {
            keyWord=content;
            String city = nowCity;
            if (poisitionIsChosen) {
//                aMap.clear();
                LatLng latLng = new LatLng(focusPoi.getLatitude(), focusPoi.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        //必须，设置经纬度
                        .position(latLng);
//                aMap.addMarker(markerOptions);
//                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                Log.i("search", "no");

            }else{
                Log.i("search", "ok");
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

    private void showToast(String string) {
        Toast.makeText(view.getContext(), string, Toast.LENGTH_LONG).show();
    }
}
