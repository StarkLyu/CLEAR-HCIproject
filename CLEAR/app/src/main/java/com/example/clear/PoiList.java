package com.example.clear;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PoiList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PoiList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List poiListResult;
    private ListView poiListView;

    public PoiList() {
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
    public static PoiList newInstance(String param1, String param2) {
        PoiList fragment = new PoiList();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        poiListResult = ((MainActivity) activity).getTitles();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_poi_list, container, false);

//        得到activity中传来的Poi数据
//        Bundle bundle = this.getArguments();
//        String str=bundle.getString("test");
//        Log.i("test", str);
//        if(bundle != null) {
//            poiList = bundle.getParcelableArrayList("poiItems");
//            Log.i("size of poiList", poiList.size()+"");
//        }
//        else {
//            Log.i("poiList","is null");
//        }


        List list=new ArrayList();
        for (int i = 0; i< poiListResult.size(); i++){
            list.add(poiListResult.get(i));
        }

        Log.i("poi ListView", "create success");

//        SimpleAdapter aAdapter = new SimpleAdapter(getContext(), listString, R.layout.fragment_poi_list_item,
//                new String[]{"name", "address"}, new int[]{R.id.poi_field_id, R.id.poi_value_id});

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);

        poiListView = v.findViewById(R.id.poi_list);
        poiListView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();

        return v;
    }

}
