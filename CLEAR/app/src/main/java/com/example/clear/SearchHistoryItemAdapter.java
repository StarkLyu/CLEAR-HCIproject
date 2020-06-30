package com.example.clear;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.Map;

public class SearchHistoryItemAdapter extends SimpleAdapter {
    //上下文
    Context context ;
    public SearchHistoryItemAdapter(Context context,
                                    List<? extends Map<String, ?>> data, int resource, String[] from,
                                    int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
    }
    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        View view = super.getView(i, convertView, viewGroup);
        final Button btn= view.findViewById(R.id.notice_button);

        btn.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i("您点击了","button");
            }
        });
        return view;
    }
}
