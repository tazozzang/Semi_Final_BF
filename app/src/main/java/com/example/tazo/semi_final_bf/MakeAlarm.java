package com.example.tazo.semi_final_bf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Locale;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class MakeAlarm extends AppCompatActivity  implements TextToSpeech.OnInitListener{

    private SimpleFingerGestures mySfg;

    Vibrator vibrator;
    TimePicker timePicker;
    int t_o_d = 0, m = 0;

    LinearLayout linearLayout;

    TextToSpeech tts;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(timePicker.getMinute() >= 55){
                    timePicker.setHour(timePicker.getHour() + 1);
                }
                m += 5;
                String plus = m + "분 추가";
                //Toast.makeText(getApplicationContext(), warningmsg,Toast.LENGTH_SHORT).show();
                tts.speak(plus, TextToSpeech.QUEUE_FLUSH, null);
                timePicker.setMinute(timePicker.getMinute() + 5);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(timePicker.getMinute() <= 4){
                    timePicker.setHour(timePicker.getHour() - 1);
                }
                m -= 5;
                String minus = m + "분 감소";
                tts.speak(minus, TextToSpeech.QUEUE_FLUSH, null);
                timePicker.setMinute(timePicker.getMinute() - 5);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_alarm);
        tts = new TextToSpeech(this,this);
        tts.setLanguage(Locale.KOREA);

        linearLayout = (LinearLayout)findViewById(R.id.activity_make_alarm);

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);;
        timePicker = (TimePicker) findViewById(R.id.timePicker);

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
                    String setT = m + "분 후에 알람이 울립니다.";
                    tts.speak(setT, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(getApplicationContext(), "Make Alarm", Toast.LENGTH_SHORT).show();
                    new AlarmHATT(getApplicationContext()).Alarm(timePicker.getHour(), timePicker.getMinute());
                    finish();

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
        String telling= "현재 시각" + timePicker.getHour() +"시 " + timePicker.getMinute()+"분입니다.";
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

            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE),time_of_day,minute,0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),sender);
        }
    }
}
