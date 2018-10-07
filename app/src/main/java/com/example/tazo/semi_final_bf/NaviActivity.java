package com.example.tazo.semi_final_bf;

import android.content.Intent;
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
    private int[] res = {R.drawable.navi_1};
    private String[] talk = {
      "화면을 시계 방향으로 돌려 앱을 선택합니다."
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

                if(count < 1){
                    imageView.setImageResource(res[count]);
                    textView.setText(talk[count]);
                    tts.speak(talk[count], TextToSpeech.QUEUE_FLUSH, null);
                    count++;
                }
                else{
                    tts.speak("화면 안내를 종료합니다.", TextToSpeech.QUEUE_FLUSH, null);
                    onInit(0);
                    startActivity(new Intent(this, MainActivity.class));
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
