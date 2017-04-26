package com.example.tazo.semi_final_bf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MakeAlarm extends AppCompatActivity {

    Vibrator vibrator;
    TimePicker timePicker;
    int t_o_d = 0, m = 0;
    TextView aaa;
    TextView bbb;
    TextView ccc;
    TextView ddd;
    TextView eee;
    TextView fff;
    TextView min;
    Button button;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(m >= 55){
                    timePicker.setHour(timePicker.getHour() + 1);
                }
                m += 5;
                min.setText(String.valueOf(m));
                timePicker.setMinute(timePicker.getMinute() + 5);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(m <= 4){
                    timePicker.setHour(timePicker.getHour() - 1);
                }
                m -= 5;
                min.setText(String.valueOf(m));
                timePicker.setMinute(timePicker.getMinute() - 5);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_alarm);

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);;
        aaa = (TextView) findViewById(R.id.aaa);
        bbb = (TextView) findViewById(R.id.bbb);
        ccc = (TextView) findViewById(R.id.ccc);
        ddd = (TextView) findViewById(R.id.ddd);
        eee = (TextView) findViewById(R.id.eee);
        fff = (TextView) findViewById(R.id.fff);
        min = (TextView) findViewById(R.id.min);
        button = (Button) findViewById(R.id.alarmbtn);
        timePicker = (TimePicker) findViewById(R.id.timePicker);

        min.setText(String.valueOf(timePicker.getMinute()));

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour_of_day, int minute) {
                t_o_d = hour_of_day;
                m = minute;
                min.setText(String.valueOf(m));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Make Alarm", Toast.LENGTH_SHORT).show();
                new AlarmHATT(getApplicationContext()).Alarm(t_o_d, m);
            }
        });

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
