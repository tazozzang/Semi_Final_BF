package com.example.tazo.semi_final_bf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Tazo on 2017-04-26.
 */

public class CSettingActivity extends Activity implements TextToSpeech.OnInitListener {
    TextToSpeech tts;

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

    int RESULT_OK = 1;

    UtteranceProgressListener listener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            if(chosenC == 4 || chosenI == 6) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result","ok");
                setResult(2, returnIntent);
                finish();
            }
        }

        @Override
        public void onError(String utteranceId) {

        }
    };

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
        tts.setOnUtteranceProgressListener(listener);
        chosenNum.setText("");
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");
        tts.speak(start,TextToSpeech.QUEUE_FLUSH, params);

        //QUEUE_FLUSH는 큐에 모든 값을 없애고 초기화한 후 값을 넣는 옵션
        //QUEUE_ADD는 현재 있는 큐값에 추가하는 옵션
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RESULT_OK) {
            if(resultCode == Activity.RESULT_OK) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",1);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }else if(resultCode == 2) {
                Intent returnIntent = new Intent(getApplication(), MainActivity.class);
                returnIntent.putExtra("result",2);
                setResult(2, returnIntent);
                finish();
            }else{
                tts.speak("오류 발생!",TextToSpeech.QUEUE_FLUSH,null);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                sx = e.getX();
                sy = e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == cmode) {
                    calculate_theta4(e);
                }else {
                    calculate_theta6(e);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mode == cmode && chosenC != -1) {
                    if(chosenC == 4) {
                        start = "설정을 취소합니다. 메인 화면으로 돌아갑니다.";
                        onInit(1);
                    }else {
                        mode = imode;
                        title.setText("컨트롤러" + chosenC + " 아이콘 선택");
                        start = "컨트롤러" + String.valueOf(chosenC) + "에서 수정할 아이콘을 Wheel을 돌려 선택해주세요.";
                        onInit(1);
                    }
                }else if(chosenI != -1){
                    if(chosenI == 6) {
                        start = "설정을 취소합니다. 메인 화면으로 돌아갑니다.";
                        onInit(1);
                    }else {
                        Intent i = new Intent(this, CSettingApplication.class);
                        i.putExtra("cnum", chosenC);
                        i.putExtra("inum", chosenI);
                        startActivityForResult(i, 1);
                    }
                }
                break;
        }
        return super.onTouchEvent(e);
    }

    public void calculate_theta4(MotionEvent e) {
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
            tts.speak("1번",TextToSpeech.QUEUE_FLUSH,null);
            chosenC = 1;
        }
        if (theta <= 150 && theta >= 150 - 5) {
            v.vibrate(100);
            chosenNum.setText("2");
            tts.speak("2번",TextToSpeech.QUEUE_FLUSH,null);
            chosenC = 2;
        }
        if (theta <= -150 && theta >= -150 - 5) {
            v.vibrate(100);
            chosenNum.setText("3");
            tts.speak("3번",TextToSpeech.QUEUE_FLUSH,null);
            chosenC = 3;
        }
        if (theta <= -60 && theta >= -60 - 5) {
            v.vibrate(100);
            chosenNum.setText("취소");
            tts.speak("설정 취소",TextToSpeech.QUEUE_FLUSH,null);
            chosenC = 4;
        }

    }

    public void calculate_theta6(MotionEvent e) {
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

        if (theta <= 20 && theta >= 20 - 5) {
            v.vibrate(100);
            chosenNum.setText("1");
            tts.speak("1번",TextToSpeech.QUEUE_FLUSH,null);
            chosenI = 1;
        }
        if (theta <= 80 && theta >= 80 - 5) {
            v.vibrate(100);
            chosenNum.setText("2");
            tts.speak("2번",TextToSpeech.QUEUE_FLUSH,null);
            chosenI = 2;
        }
        if (theta <= 140 && theta >= 140 - 5) {
            v.vibrate(100);
            chosenNum.setText("3");
            tts.speak("3번",TextToSpeech.QUEUE_FLUSH,null);
            chosenI = 3;
        }
        if (theta <= -160 && theta >= -160 - 5) {
            v.vibrate(100);
            chosenNum.setText("4");
            tts.speak("4번",TextToSpeech.QUEUE_FLUSH,null);
            chosenI = 4;
        }
        if (theta <= -100 && theta >= -100 - 5) {
            v.vibrate(100);
            chosenNum.setText("5");
            tts.speak("5번",TextToSpeech.QUEUE_FLUSH,null);
            chosenI = 5;
        }
        if (theta <= -40 && theta >= -40 - 5) {
            v.vibrate(100);
            chosenNum.setText("취소");
            tts.speak("설정 취소",TextToSpeech.QUEUE_FLUSH,null);
            chosenI = 6;
        }
    }
}