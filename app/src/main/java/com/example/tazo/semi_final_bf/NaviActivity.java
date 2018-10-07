package com.example.tazo.semi_final_bf;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class NaviActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private TextToSpeech tts;

    ImageView imageView;
    TextView textView;

    private int count = 0;
    private int[] res = {R.drawable.navi_1,  R.drawable.navi_6, R.drawable.navi_2, R.drawable.navi_3, R.drawable.navi_4,
            R.drawable.navi_5, R.drawable.navi_11, R.drawable.navi_7,R.drawable.navi_8,
            R.drawable.navi_9, R.drawable.navi_10, R.drawable.navi_12, R.drawable.navi_12,
           };
    private String[] talk = {
            "화면을 시계 방향으로 돌려 앱을 선택합니다.",
            "첫 번째 컨트롤러의 첫 번째 앱은 스팟 메모",
            "두 손가락으로 좌우를 드래그하면 다른 컨트롤러로 이동합니다.",
            "두 손가락으로 아래를 드래그하면 간단 알람 설정으로 이동합니다.",
            "두 손가락으로 위를 드래그하면 설정 화면으로 이동합니다.",
            "알람 설명",
            "알람 오는 것 확인",
            "설정 화면에서는 모드 변경, 컨트롤러 앱 설정, 화면 안내 다시 듣기",
            "모드 변경에서 컨트롤러와 격자 무늬 모드 변경 가능",
            "컨트롤러 수정 안내",
            "앱 리스트에서 선택하면 지정 됨",
            "격자 무늬 안내",
            "자동 롤링 설명",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        imageView = (ImageView)findViewById(R.id.image);
        imageView.setImageResource(R.drawable.controller);

        textView = (TextView)findViewById(R.id.navitext);
        textView.setText("화면을 터치하면 안내가 시작됩니다.");


        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.KOREA);


//        Intent i = new Intent(this, MainActivity.class);
//        finish();
//        startActivity(i);
    }

    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getActionMasked() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:

                if(count < 13){
                    imageView.setImageResource(res[count]);
                    textView.setText(talk[count]);
                    tts.speak(talk[count], TextToSpeech.QUEUE_FLUSH, null);
                    count++;
                }
                else{
                    tts.speak("화면 안내를 종료하고 메인 화면으로 돌아갑니다.", TextToSpeech.QUEUE_FLUSH, null);
                    onInit(0);
                    finish();
                }
                break;
        }

        return super.onTouchEvent(e);
    }

    @Override
    public void onInit(int i) {

    }
}
