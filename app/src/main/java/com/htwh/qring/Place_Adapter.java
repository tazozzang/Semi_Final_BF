package com.htwh.qring;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Tazo on 2018-04-07.
 */

public class Place_Adapter extends ArrayAdapter<Place_Info> {
    ArrayList<Place_Info> items;
    Context context;
    public Place_Adapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Place_Info> objects) {
        super(context, resource, objects);
        this.items = objects;
        this.context = context;
    }

    public View getView(int position, View view, ViewGroup parent){
        View v = view;
        if(v == null){
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.place_row, null);
        }

        Place_Info place_info = items.get(position);
        if( place_info!= null ){
            TextView p_n = (TextView)v.findViewById(R.id.place_name);
            if(p_n != null){
                p_n.setText(place_info.getPlace_name());
            }
            TextView p_t = (TextView)v.findViewById(R.id.place_type);
            if(p_t != null){
                p_t.setText(place_info.getPlace_type().toString());
            }
        }

        return v;
    }
}
