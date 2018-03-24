package com.example.tazo.semi_final_bf;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Tazo on 2018-03-24.
 */

public class Place_Info {
    CharSequence place_name;
    LatLng place_latlng;
    Place_Info(CharSequence name, LatLng latLng){
        place_name = name;
        place_latlng = latLng;
    }
    public  CharSequence getPlace_name(){
        return place_name;
    }
    public LatLng getPlace_latlng(){
        return place_latlng;
    }
}
