package com.example.tazo.semi_final_bf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tazo on 2017-04-26.
 */

public class CSettingActivity extends Activity implements TextToSpeech.OnInitListener{
    TextToSpeech tts;
    PackageManager pm;
    List<ResolveInfo> list;
    ArrayList<ActivityInfo> applist;
    ArrayAdapter adapter;

    TextView title;
    TextView chosenNum;

    int chosenC = -1;
    int chosenI = -1;

    public double sx, sy; // 시작점
    public double tx, ty; // 현재점
    public double center_x, center_y; // 중심점
    double theta; // startPoint와 currentPoint 벡터의 사이각
    double ztheta; // theta 계산시 사용
    Vibrator v;

    Controller controller;
    String start;

    int mode = -1;
    int cmode = 0;
    int imode = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csetting);

        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.KOREA);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        controller = new Controller(this,0);
        center_x = controller.centerX;
        center_y = controller.centerY;

        title = (TextView)findViewById(R.id.csetting_title);
        chosenNum = (TextView)findViewById(R.id.csetting_chosenNum);
        start = "수정할 컨트롤러를 Wheel을 돌려 선택해주세요.";

        mode = cmode;
    }

    @Override
    public void onInit(int status) {
        chosenNum.setText("");
        tts.speak(start,TextToSpeech.QUEUE_FLUSH, null);

        //QUEUE_FLUSH는 큐에 모든 값을 없애고 초기화한 후 값을 넣는 옵션
        //QUEUE_ADD는 현재 있는 큐값에 추가하는 옵션
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                sx = e.getX();
                sy = e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == cmode) {
                    calculate_theta3(e);
                }else {
                    calculate_theta5(e);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mode == cmode && chosenC != -1) {
                    mode = imode;
                    title.setText("컨트롤러"+chosenC+" 아이콘 선택");
                    start = "컨트롤러"+String.valueOf(chosenC)+"에서 수정할 아이콘을 Wheel을 돌려 선택해주세요.";
                    onInit(1);
                }else if(chosenI != -1){
                    Intent i = new Intent(this, CSettingApplication.class);
                    i.putExtra("cnum",chosenC);
                    i.putExtra("inum",chosenI);
                    startActivity(i);
                }
                break;
        }
        return super.onTouchEvent(e);
    }

    public void calculate_theta3(MotionEvent e) {
        tx = e.getX();
        ty = e.getY();

        double v1x = sx - center_x;
        double v1y = sy - center_y;

        double v2x = tx - center_x;
        double v2y = ty - center_y;

        double temp1 = v1x * v2x + v1y * v2y;
        double temp2 = Math.sqrt(v1x * v1x + v1y * v1y);
        temp2 = temp2 * Math.sqrt(v2x * v2x + v2y * v2y);
        // 두 벡터의 사이각을 구해준다

        ztheta = Math.acos(temp1 / temp2);
        theta = (v1x * v2y - v1y * v2x > 0.0d) ? ztheta : -ztheta;
        theta = Math.toDegrees(theta);

        if (theta <= 60 && theta >= 60 - 5) {
            v.vibrate(100);
            chosenNum.setText("1");
            chosenC = 1;
        }
        if (theta <= 180 && theta >= 180 - 5) {
            v.vibrate(100);
            chosenNum.setText("2");
            chosenC = 2;
        }
        if (theta <= -60 && theta >= -60 - 5) {
            v.vibrate(100);
            chosenNum.setText("3");
            chosenC = 3;
        }
    }

    public void calculate_theta5(MotionEvent e) {
        tx = e.getX();
        ty = e.getY();

        double v1x = sx - center_x;
        double v1y = sy - center_y;

        double v2x = tx - center_x;
        double v2y = ty - center_y;

        double temp1 = v1x * v2x + v1y * v2y;
        double temp2 = Math.sqrt(v1x * v1x + v1y * v1y);
        temp2 = temp2 * Math.sqrt(v2x * v2x + v2y * v2y);
        // 두 벡터의 사이각을 구해준다

        ztheta = Math.acos(temp1 / temp2);
        theta = (v1x * v2y - v1y * v2x > 0.0d) ? ztheta : -ztheta;
        theta = Math.toDegrees(theta);

        if (theta <= 60 && theta >= 60 - 5) {
            v.vibrate(100);
            chosenNum.setText("1");
            chosenI = 1;
        }
        if (theta <= 120 && theta >= 120 - 5) {
            v.vibrate(100);
            chosenNum.setText("2");
            chosenI = 2;
        }
        if (theta <= 180 && theta >= 180 - 5) {
            v.vibrate(100);
            chosenNum.setText("3");
            chosenI = 3;
        }
        if (theta <= -120 && theta >= -120 - 5) {
            v.vibrate(100);
            chosenNum.setText("4");
            chosenI = 4;
        }
        if (theta <= -60 && theta >= -60 - 5) {
            v.vibrate(100);
            chosenNum.setText("5");
            chosenI = 5;
        }
    }

}
