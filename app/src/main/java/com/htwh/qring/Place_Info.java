package com.htwh.qring;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Tazo on 2018-03-24.
 */

public class Place_Info {
    CharSequence place_name;
    LatLng place_latlng;
    List<Integer> place_type;
    Place_Info(CharSequence name, LatLng latLng, List<Integer> type){
        place_name = name;
        place_latlng = latLng;
        place_type = type;
    }
    public  CharSequence getPlace_name(){
        return place_name;
    }
    public LatLng getPlace_latlng(){
        return place_latlng;
    }
    public List<Integer> getPlace_type(){
        return place_type;
    }
}
