package com.htwh.qring;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
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

import java.io.File;
import java.util.ArrayList;

public class SM_main extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener, OnMapReadyCallback, TextToSpeech.OnInitListener {
    PlaceAutocompleteFragment autocompleteFragment;
    MapFragment mapFragment;
    GoogleMap map;
    GoogleApiClient googleApiClient = null;
    TextToSpeech tts;

    LocationRequest locationRequest;
    LocationManager locationManager;
    Location location;

    Place_Info place_info;
    ArrayList<Place_Info> place_name;

    double latitude;
    double longitude;

    Button search_button;
    Button add_memo;
    Button btn_myMemo;

    File filesv;
    File filest;

    int numvoice=0;
    int numtext=0;
    int numoffile = 0;

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

        search_button = (Button) findViewById(R.id.search_button);
        add_memo = (Button)findViewById(R.id.add_button);
        btn_myMemo = (Button)findViewById(R.id.btn_myMemo);

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
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(listner);

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SSTsted == false) {

                    String telling= "찾고자 하는 지명을 말씀해주세요";
                    tts.speak(telling, TextToSpeech.QUEUE_FLUSH, null);
                    while(tts.isSpeaking()) {
                        // 말 끝나고 삐 하자~
                    }

                    recognizer.startListening(i);
                    //Toast.makeText(view.getContext(),"시작",Toast.LENGTH_LONG).show();
                    SSTsted = true;
                    //Toast.makeText(getApplicationContext(), "여기까지 오긴 함1",Toast.LENGTH_SHORT).show();

                } else {

                    recognizer.stopListening();
                    SSTsted = false;
                    //Toast.makeText(getApplicationContext(), "여기까지 오긴 함3",Toast.LENGTH_SHORT).show();

                }
            }
        });

        add_memo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Choose_Place.class);
                startActivity(intent);
            }
        });



        btn_myMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MyMemoActivity.class);
                startActivity(intent);
            }
        });

        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            filesv = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/SF_SM/Voice");
            filesv.mkdirs(); // 있으면 안만들거고, 없으면 만들어주게~
            String numfilev[] = filesv.list();
            numvoice = numfilev.length;
            filest = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/SF_SM/Text");
            filest.mkdirs();
            String numfilet[] = filest.list();
            numtext = numfilet.length;
        }
        numoffile = numvoice + numtext;
        btn_myMemo = (Button)findViewById(R.id.btn_myMemo);
        String hey = "내가 남긴 "+String.valueOf(numoffile)+"개 메모";

        btn_myMemo.setText(hey);
        // 메모가 몇개인지 알아내서 버튼 이름 바꿔야지~
        tts = new TextToSpeech(this, this);
        //tts.speak("주변에 내가 남긴 N개의 메모가 있습니다",TextToSpeech.QUEUE_FLUSH,null);


        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000)
                .setSmallestDisplacement(100);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        place_name = new ArrayList<Place_Info>();

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    place_info = new Place_Info( placeLikelihood.getPlace().getName(),placeLikelihood.getPlace().getLatLng(),placeLikelihood.getPlace().getPlaceTypes());
                    place_name.add(place_info);
                }
                likelyPlaces.release();
            }
        });

    }


    public void initialMap(Location location) {
        map.clear();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        final LatLng Loc = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(Loc, 16));

        MarkerOptions options = new MarkerOptions();
        options.position(Loc);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        options.title("내 위치");
        options.snippet("현재 위치");
        map.addMarker(options);
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
            autocompleteFragment.setText("" + rs[0]);

            String telling= "검색하신 지명의 이름이 " + rs[0] + "과 관련된 지명입니다.";
            tts.speak(telling, TextToSpeech.QUEUE_FLUSH, null);
            while(tts.isSpeaking()) {
                // 말 끝나고 삐 하자~
            }

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

    public void updateMap(Place place) {
        map.clear();

        final LatLng Loc = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(Loc, 16));

        MarkerOptions options = new MarkerOptions();
        options.position(Loc);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        options.title(String.valueOf(place.getName()));
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(location != null)
            initialMap(location);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest, (LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onInit(int i) {

    }
}
