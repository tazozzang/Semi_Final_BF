package com.example.tazo.semi_final_bf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

/**
 * Created by Tazo on 2017-04-26.
 */

public class SettingActivity extends Activity implements TextToSpeech.OnInitListener {
    TextToSpeech tts;

    ListView listView;
    ArrayAdapter adapter;

    String start;

    int keyPosition = -2;
    String keyChosenName = null;
    SimpleFingerGestures mySfg = new SimpleFingerGestures();

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
        adapter.add("화면 안내");
        listView.setAdapter(adapter);

        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.KOREA);

        start = "볼륨키나 터치를 이용하여 목록을 읽고, 더블 탭으로 메뉴를 선택하세요.";
        onInit(0);

        // ** 볼륨키로 선택하는 것도 추가하기
        // ** 터치도 완성 안됨

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                start = ((TextView) view).getText().toString();
                onInit(9);

                switch (position) {
                    case 0:
                        // 설정 취소
                        exit = true;
                        break;
                    case 1:
                        // 모드 변경
                        startActivityForResult(new Intent(SettingActivity.this, MSettingActivity.class), 4);
                        finish();
                        break;
                    case 2:
                        // 컨트롤러 설정
                        Intent c = new Intent(SettingActivity.this, CSettingActivity.class);
                        startActivityForResult(c,2);
                        finish();
                        break;
                    case 3:
                        // 모드 변경
                        startActivity(new Intent(SettingActivity.this, NaviActivity.class));
                        finish();
                        break;
                }
            }
        });

        mySfg.setOnFingerGestureListener(new SimpleFingerGestures.OnFingerGestureListener() {
            @Override
            public boolean onSwipeUp(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onSwipeDown(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onSwipeLeft(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onSwipeRight(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onPinch(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onUnpinch(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onDoubleTap(int i) {
                if (keyPosition == 0) {
                    // 0 : 설정 취소
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result","ok");
                    setResult(2, returnIntent);
                    finish();
                } else if (keyPosition == 1) {
                    // 1 : 모드 변경
                    startActivityForResult(new Intent(SettingActivity.this, MSettingActivity.class), 4);
                } else {
                    // 2 : 컨트롤러 설정
                    Intent c = new Intent(SettingActivity.this, CSettingActivity.class);
                    startActivityForResult(c,2);
                }
                return false;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        listView.setOnTouchListener(mySfg);
        listView.setOnItemClickListener(null);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (keyPosition <= 0) {
                    keyPosition = 0;
                }else {
                    keyPosition--;
                }

                keyChosenName = listView.getItemAtPosition(keyPosition).toString();
                tts.speak(String.valueOf(keyChosenName), TextToSpeech.QUEUE_FLUSH, null);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (keyPosition < 0) {
                    keyPosition = 0;
                }else if (keyPosition >= 2) {
                    keyPosition = 2;
                } else {
                    keyPosition++;
                }

                keyChosenName = listView.getItemAtPosition(keyPosition).toString();
                tts.speak(String.valueOf(keyChosenName), TextToSpeech.QUEUE_FLUSH, null);
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2) {
            // 컨트롤러 설정 후 돌아온 곳
            if(resultCode == Activity.RESULT_OK) {
                Intent returnIntent = new Intent();
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
        }else if(requestCode == 4) {
            // 모드 변경 후 돌아온 곳
            if(resultCode == 4) {
                Intent returnIntent = new Intent();
                setResult(4, returnIntent);
                finish();
            }
            if(resultCode == 5) {
                Intent returnIntent = new Intent();
                setResult(5, returnIntent);
                finish();
            }
        }
    }

}