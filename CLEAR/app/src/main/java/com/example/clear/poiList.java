package com.example.clear;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link poiList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class poiList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    List<PoiItem> poiList;
    private List<HashMap<String, String>> listString = new ArrayList<>();
    private ListView minputlist;
    private String[] data = { "Apple", "Banana", "Orange", "Watermelon",
            "Pear", "Grape", "Pineapple", "Strawberry", "Cherry", "Mango" };

    public poiList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment poiList.
     */
    // TODO: Rename and change types and number of parameters
    public static poiList newInstance(String param1, String param2) {
        poiList fragment = new poiList();
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
        View v = inflater.inflate(R.layout.fragment_poi_list, container, false);

        Log.i("ListView", "create success");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", "name");
        map.put("address", "address");
        listString.add(map);

//        SimpleAdapter aAdapter = new SimpleAdapter(getContext(), listString, R.layout.fragment_poi_list_item,
//                new String[]{"name", "address"}, new int[]{R.id.poi_field_id, R.id.poi_value_id});

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_list_item_1, data);

//        View account = View.inflate(getContext(), R.layout.fragment_poi_list, null);
        minputlist= v.findViewById(R.id.poi_list);

        minputlist.setAdapter(adapter);
//        adapter.notifyDataSetChanged();

        return v;
    }

}
