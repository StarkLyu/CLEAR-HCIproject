package com.example.clear;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchInput#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchInput extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MyListener myListener;

    public interface MyListener{
        public void sendContent(String info);
        public void sendContent_2(String info);
    }

    View v;

    //搜索
    private AutoCompleteTextView searchText;// 输入搜索关键字
    private String keyWord = "";// 要输入的poi搜索关键字
    private EditText editCity;// 要输入的城市名字或者城市区号
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索

    public SearchInput() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchInput.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchInput newInstance(String param1, String param2) {
        SearchInput fragment = new SearchInput();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myListener = (MyListener) getActivity();
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
        v=inflater.inflate(R.layout.fragment_search_input, container, false);

        setUpMap();

        return v;
    }

    /**
     * 设置页面监听
     */
    private void setUpMap() {
        Button searButton = v.findViewById(R.id.searchButton);
        searButton.setOnClickListener(this);
        Button nextButton = v.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        searchText = v.findViewById(R.id.keyWord);
//        searchText.addTextChangedListener((TextWatcher) this);// 添加文本输入框监听事件
        editCity = v.findViewById(R.id.city);
//        aMap.setOnMarkerClickListener((AMap.OnMarkerClickListener) this);// 添加点击marker监听事件
//        aMap.setInfoWindowAdapter((AMap.InfoWindowAdapter) this);// 添加显示infowindow监听事件
    }

    /**
     * 点击搜索按钮
     */
    public void searchButton() {

        keyWord = searchText.getText().toString();

        if ("".equals(keyWord)) {
            Toast.makeText(getActivity(), "请输入关键词", Toast.LENGTH_SHORT).show();

        } else {
            String value=keyWord;
            myListener.sendContent(value);//将内容进行回传
            String city=editCity.getText().toString();
            Log.i("editcity",city);
            myListener.sendContent_2(city);
        }
    }

    /**
     * 点击下一页按钮
     */
    public void nextButton() {
//        if (query != null && poiSearch != null && poiResult != null) {
//            if (poiResult.getPageCount() - 1 > currentPage) {
//                currentPage++;
//                query.setPageNum(currentPage);// 设置查后一页
//                poiSearch.searchPOIAsyn();
//            } else {
//                Toast.makeText(getActivity(), "已经是最后一页了", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**
             * 点击搜索按钮
             */
            case R.id.searchButton:
                searchButton();
                break;
            /**
             * 点击下一页按钮
             */
            case R.id.nextButton:
                nextButton();
                break;
            default:
                break;
        }
    }
}
