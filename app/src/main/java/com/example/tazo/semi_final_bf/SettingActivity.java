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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Tazo on 2017-04-26.
 */

public class SettingActivity extends Activity implements TextToSpeech.OnInitListener {
    TextToSpeech tts;

    ListView listView;
    ArrayAdapter adapter;

    String start;

    boolean exit = false;
    int RESULT_OK = 1;

    UtteranceProgressListener listener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            if(exit) {
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
        setContentView(R.layout.activity_setting);

        listView = (ListView)findViewById(R.id.setting_listview);
        adapter = new ArrayAdapter(getApplicationContext(), R.layout.row);
        adapter.add("설정 취소");
        adapter.add("모드 변경");
        adapter.add("컨트롤러 설정");
        listView.setAdapter(adapter);

        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.KOREA);

        start = "설정 메뉴입니다. 볼륨키나 터치를 이용하여 목록을 읽고, 더블 탭으로 설정을 완료하세요.";
        onInit(0);

        // 볼륨키로 선택하는 것도 추가하기
        // 터치도 완성 안됨

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        // 설정 취소
                        break;
                    case 2:
                        // 모드 변경
                        break;
                    case 3:
                        // 컨트롤러 설정
                        Intent i = new Intent(SettingActivity.this, CSettingActivity.class);
                        startActivity(i);
                        break;
                }
            }
        });

    }

    @Override
    public void onInit(int status) {
        tts.setOnUtteranceProgressListener(listener);
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

}