package com.example.tazo.semi_final_bf;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class SM_main extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener, OnMapReadyCallback {
    PlaceAutocompleteFragment autocompleteFragment;
    MapFragment mapFragment;
    GoogleMap map;
    GoogleApiClient googleApiClient = null;

    Button search_button;
    SpeechRecognizer recognizer;
    boolean SSTsted = false;
    Intent i;

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sm_main);

        search_button = (Button)findViewById(R.id.search_button);

        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_search);
        autocompleteFragment.setOnPlaceSelectedListener(this);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API) //로케이션정보를 얻겠다
                    .build();
        }

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(listner);

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SSTsted == false){
                    recognizer.startListening(i);
                    //Toast.makeText(view.getContext(),"시작",Toast.LENGTH_LONG).show();
                    SSTsted = true;
                    //Toast.makeText(getApplicationContext(), "여기까지 오긴 함1",Toast.LENGTH_SHORT).show();

                }
                else{
                    recognizer.stopListening();
                    SSTsted = false;
                    //Toast.makeText(getApplicationContext(), "여기까지 오긴 함3",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    private RecognitionListener listner = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int i) {

        }

        @Override
        public void onResults(Bundle bundle) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mresult = bundle.getStringArrayList(key);
            String[] rs = new String[mresult.size()];
            mresult.toArray(rs);

            //Toast.makeText(getApplicationContext(), rs[0],Toast.LENGTH_SHORT).show();
            //Toast.makeText(getApplicationContext(), "여기까지 오긴 함2",Toast.LENGTH_SHORT).show();
            autocompleteFragment.setText(""+rs[0]);

        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };
    @Override
    public void onPlaceSelected(Place place) {
        Toast.makeText(this, place.getName(),
                Toast.LENGTH_SHORT).show();
        updateMap(place);
    }

    @Override
    public void onError(Status status) {
        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    public void updateMap(Place place){
        map.clear();

        final LatLng Loc = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(Loc, 16));

        MarkerOptions options = new MarkerOptions();
        options.position(Loc);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        options.title(String.valueOf("홍대"));
        //options.snippet("내 위치");
        map.addMarker(options);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
