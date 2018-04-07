package com.example.tazo.semi_final_bf;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Choose_Place extends AppCompatActivity  implements com.google.android.gms.location.LocationListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemClickListener {
    private LocationManager locationManager;
    GoogleApiClient googleApiClient = null;

    //LocationListener locationListener;
    LocationRequest locationRequest;
    Location location;

    ListView listView;
    ArrayList<Place_Info> place_name;
    Place_Adapter aa;
    CharSequence name;
    LatLng latLng_place;

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose__place);


        listView = (ListView)findViewById(R.id.listview);
        place_name = new ArrayList<Place_Info>();


        int Fine_Perm_Check = ContextCompat.checkSelfPermission(this,  android.Manifest.permission.ACCESS_FINE_LOCATION);

        if(Fine_Perm_Check == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            },0);
        }

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API) //로케이션정보를 얻겠다
                    .build();
        }

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000)
                .setSmallestDisplacement(100);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Place_Info place_info = new Place_Info( placeLikelihood.getPlace().getName(),placeLikelihood.getPlace().getLatLng());
                    place_name.add(place_info);
                }
                likelyPlaces.release();

                listView.setAdapter(aa);
            }
        });

        aa = new Place_Adapter(this,R.layout.place_row, place_name);

        // getPlace.setText(place_name.get(0).place_name + "-" + place_name.get(0).place_latlng.latitude + "-" + place_name.get(0).place_latlng.longitude);

        listView.setAdapter(aa);
        listView.setOnItemClickListener(this);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,  android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int Fine_Perm_Check = ContextCompat.checkSelfPermission(this,  android.Manifest.permission.ACCESS_FINE_LOCATION);

            if(Fine_Perm_Check == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this,new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                },0);
            }
            return;
        }

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(adapterView == listView){
            name = place_name.get(position).place_name;
            latLng_place = place_name.get(position).place_latlng;
            // getPlace.setText(place_name.get(i).place_name + "-" + place_name.get(i).place_latlng.latitude + "-" + place_name.get(i).place_latlng.longitude);
            //dialog 띄우고 intent 이동시 이름 넘겨주기
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(place_name.get(position).place_name+"을 장소로 정하겠습니까?");
            builder.setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            listView.setAdapter(aa);
                            Intent intent = new Intent(getApplicationContext(), AddMemoActivity.class);
                            intent.putExtra("place_name",name+"-"+latLng_place.latitude+"-"+latLng_place.longitude+"-0");
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            listView.setAdapter(aa);
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            //listView.setAdapter(aa);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
