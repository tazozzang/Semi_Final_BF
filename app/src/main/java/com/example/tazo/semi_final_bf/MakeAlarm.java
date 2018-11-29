package com.example.tazo.semi_final_bf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Locale;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class MakeAlarm extends AppCompatActivity  implements TextToSpeech.OnInitListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private SimpleFingerGestures mySfg;

    private GoogleApiClient googleApiClient;

    Vibrator vibrator;
    TimePicker timePicker;
    int t_o_d = 0, m = 0;

    LinearLayout linearLayout;
    Button button;
    Button cnt;

    TextToSpeech tts;


    final long[] Know_You = {
            500, 800
    };
    final long[] Dont_Know_You = {
            500, 200, 200, 200, 200, 200, 200
    };
    int count = 0;


    final long[] pattern_ms = {
            50,70,100,150,500,70,100,150,500
    };
    final long[] pattern_cll = {
            0, 200
    };
    final long[] pattern_mscl = {
            0, 800
    };

    final long[] alarm_vib = {500, 1000};


    @Override
    protected void onStart() {
        super.onStart();

        if(!googleApiClient.isConnected())
            googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(timePicker.getCurrentMinute() >= 55){

                    timePicker.setCurrentHour(timePicker.getCurrentHour() + 1);
                }
                m += 5;
                String plus = m + "분 추가";
                //Toast.makeText(getApplicationContext(), warningmsg,Toast.LENGTH_SHORT).show();
                tts.speak(plus, TextToSpeech.QUEUE_FLUSH, null);
                timePicker.setCurrentMinute(timePicker.getCurrentMinute() + 5);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(timePicker.getCurrentMinute() <= 4){
                    timePicker.setCurrentHour(timePicker.getCurrentHour() - 1);
                }
                m -= 5;
                String minus = m + "분 감소";
                tts.speak(minus, TextToSpeech.QUEUE_FLUSH, null);
                timePicker.setCurrentMinute(timePicker.getCurrentMinute() - 5);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //////////////////////////////////////////////////////////////////
    //// 데이터 통신용


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Toast.makeText(this,"Connect",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Toast.makeText(this,"Suspend",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Toast.makeText(this,"Fail",Toast.LENGTH_SHORT).show();
    }

    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(@NonNull Result result) {
            String resultC = "Sending " + result.getStatus().isSuccess();

            Toast.makeText(getApplicationContext(),resultC,Toast.LENGTH_SHORT).show();
        }
    };

    public void onDontVibrate(){
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/DONT_VIBE_PATH");
        dataMapRequest.getDataMap().putLongArray("dontknow",Dont_Know_You);

        dataMapRequest.getDataMap().putInt("count",count++);
        PutDataRequest request = dataMapRequest.asPutDataRequest();

        Wearable.DataApi.putDataItem(googleApiClient,request).setResultCallback(resultCallback);
        //Toast.makeText(getApplicationContext(),"No보내숑",Toast.LENGTH_SHORT).show();
    }

    public void onKnowVibrate(){
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/KNOW_VIBE_PATH");
        dataMapRequest.getDataMap().putLongArray("know",Know_You);

        dataMapRequest.getDataMap().putInt("count",count++);
        PutDataRequest request = dataMapRequest.asPutDataRequest();

        Wearable.DataApi.putDataItem(googleApiClient,request).setResultCallback(resultCallback);
        //Toast.makeText(getApplicationContext(),"YES보내숑",Toast.LENGTH_SHORT).show();
    }

    public void onGiveAlarm(){
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/GIVE_ALARM");
        dataMapRequest.getDataMap().putLongArray("alarm",alarm_vib);

        dataMapRequest.getDataMap().putInt("count",count++);
        PutDataRequest request = dataMapRequest.asPutDataRequest();

        Wearable.DataApi.putDataItem(googleApiClient,request).setResultCallback(resultCallback);
        //Toast.makeText(getApplicationContext(),"알람보내숑",Toast.LENGTH_SHORT).show();
    }

    public void onGiveCancel(){
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/STOP_ALARM");

        dataMapRequest.getDataMap().putInt("count",count++);
        PutDataRequest request = dataMapRequest.asPutDataRequest();

        Wearable.DataApi.putDataItem(googleApiClient,request).setResultCallback(resultCallback);
        //Toast.makeText(getApplicationContext(),"알람죽여",Toast.LENGTH_SHORT).show();
    }

    /////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        setContentView(R.layout.activity_make_alarm);
        tts = new TextToSpeech(this,this);
        tts.setLanguage(Locale.KOREA);

        linearLayout = (LinearLayout)findViewById(R.id.activity_make_alarm);

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);;
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        button = (Button)findViewById(R.id.rmstop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGiveCancel();
            }
        });

        cnt = (Button)findViewById(R.id.test);
        cnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/GIVE_ALARM");
                dataMapRequest.getDataMap().putLongArray("alarm",alarm_vib);

                dataMapRequest.getDataMap().putInt("count",count++);
                PutDataRequest request = dataMapRequest.asPutDataRequest();

                Wearable.DataApi.putDataItem(googleApiClient,request).setResultCallback(resultCallback);
            }
        });

        mySfg = new SimpleFingerGestures();
        mySfg.setDebug(true);
        mySfg.setConsumeTouchEvents(true);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour_of_day, int minute) {
            }
        });

        mySfg.setOnFingerGestureListener(new SimpleFingerGestures.OnFingerGestureListener() {
            @Override
            public boolean onSwipeUp(int fingers, long gestureDuration, double gestureDistance) {
                return false;
            }

            @Override
            public boolean onSwipeDown(int fingers, long gestureDuration, double gestureDistance) {

                onGiveAlarm();

                String setT = m + "분 후에 알람이 울립니다.";
                    tts.speak(setT, TextToSpeech.QUEUE_FLUSH, null);
                    //Toast.makeText(getApplicationContext(), setT, Toast .LENGTH_SHORT).show();
                    new AlarmHATT(getApplicationContext()).Alarm(timePicker.getCurrentHour(), timePicker.getCurrentMinute());

                    finish();
                    overridePendingTransition(0,0); // finish animation 없애줌

                return false;
            }

            @Override
            public boolean onSwipeLeft(int fingers, long gestureDuration, double gestureDistance) {
                return false;
            }

            @Override
            public boolean onSwipeRight(int fingers, long gestureDuration, double gestureDistance) {
                return false;
            }

            @Override
            public boolean onPinch(int fingers, long gestureDuration, double gestureDistance) {
                return false;
            }

            @Override
            public boolean onUnpinch(int fingers, long gestureDuration, double gestureDistance) {
                return false;
            }

            @Override
            public boolean onDoubleTap(int fingers) {
                return false;
            }
        });

        linearLayout.setOnTouchListener(mySfg);

    }

    @Override
    public void onInit(int i) {
        String telling= "현재 시각" + timePicker.getCurrentHour() +"시 " + timePicker.getCurrentMinute()+"분입니다.";
        tts.speak(telling, TextToSpeech.QUEUE_FLUSH, null);
    }

    public class AlarmHATT{
        public Context context;
        public AlarmHATT(Context context){
            this.context = context;
        }
        public void Alarm(int time_of_day, int minute){
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(MakeAlarm.this, BroadcastD.class);

            PendingIntent sender = PendingIntent.getBroadcast(MakeAlarm.this, 0, intent, 0);

            Calendar calendar = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                calendar = Calendar.getInstance();
                calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE),time_of_day,minute,0);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),sender);
            }

        }
    }
}
