package com.example.tazo.semi_final_bf;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.lassana.recorder.AudioRecorder;
import com.github.lassana.recorder.AudioRecorderBuilder;

import java.io.File;
import java.util.Locale;

public class AddVoiceActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    AudioRecorder audioRecorder;
    TextToSpeech tts;

    Button startbtn;
    Button stopbtn;
    Button backbtn;
    Button playbtn;
    Button cancelbtn;

    CharSequence place_name;
    String pre_token; // 토큰 끊기전
    String[] token; // 토큰 끊은 후

    boolean recordingState = false;
    boolean newRecording = true;

    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
    MediaPlayer mediaPlayer;
    boolean isListen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_voice);
        setTitle("음성메모 추가하기");

        Intent intent = getIntent();
        place_name =  intent.getCharSequenceExtra("place_name");
        pre_token = String.valueOf(place_name);
        token = pre_token.split("-");
        //token[0] = 장소 이름
        //token[1] = latitude
        //token[2] = longitude
        //token[3] = 0 -> 공유 안 함 / 1 -> 공유 함

        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.KOREA);

        startbtn = (Button) findViewById(R.id.btn_start);
        stopbtn = (Button) findViewById(R.id.btn_stop);
        backbtn = (Button) findViewById(R.id.btn_back);
        playbtn = (Button)findViewById(R.id.btn_listen);
        cancelbtn = (Button)findViewById(R.id.btn_cancel);
        stopbtn.setEnabled(false);
        playbtn.setEnabled(false);
        cancelbtn.setEnabled(false);
    }

    public void onClickStart(View view) {
        if(!recordingState) {
            // 녹음중이 아니라면 -> 녹음 시작
            recordingState = true;
            try {
                File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SpotMemo/Voice/");
                File []fileList = nfile.listFiles();
                int i = 0;
                for(File temp : fileList){
                    if(temp.isFile()){
                        if(temp.getName().equals(nfile.getAbsolutePath() + "/" + place_name + "-("+i+").mp4")){
                            i++;
                        }
                    }
                }

                String title = nfile.getAbsolutePath() + "/" + place_name +"-("+i+").mp4";


                if(newRecording) {
                    tts.speak("다시 더블탭 하면 녹음이 일시정지 되고, 녹음을 취소하거나 저장, 재생할 수 있습니다. 삐 소리후 녹음이 시작됩니다.", TextToSpeech.QUEUE_FLUSH, null);
                    audioRecorder = AudioRecorderBuilder
                            .with(getApplicationContext())
                            .fileName(title)
                            .config(AudioRecorder.MediaRecorderConfig.DEFAULT)
                            .loggable()
                            .build();
                    newRecording = false;
                }else {
                    tts.speak("삐 소리 후 녹음이 다시 시작됩니다.",TextToSpeech.QUEUE_FLUSH,null);
                }

                while(tts.isSpeaking()) {
                    // 말 끝나고 삐 하자~
                }
                tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);

                audioRecorder.start(new AudioRecorder.OnStartListener() {
                    @Override
                    public void onStarted() {
                        startbtn.setText("일시 정지");
                        playbtn.setEnabled(false);
                        backbtn.setEnabled(false); // 녹음 중에는 뒤로 가지 못하게
                        stopbtn.setEnabled(false); // 녹음 중에는 저장 할 수 없게
                        cancelbtn.setEnabled(true);
                    }

                    @Override
                    public void onException(Exception e) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            // 녹음중 이었다면 -> 일시정지 상태로
            audioRecorder.pause(new AudioRecorder.OnPauseListener() {
                @Override
                public void onPaused(String activeRecordFileName) {
                    stopbtn.setEnabled(true); // 그대로 저장할 수 있음
                    tts.speak("녹음이 일시정지 되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    startbtn.setText("이어서 녹음");
                    playbtn.setEnabled(true);
                    cancelbtn.setEnabled(true);
                }

                @Override
                public void onException(Exception e) {

                }
            });
        }
    }

    public void onClickStop(View view) { // 녹음 저장
        tts.speak(token[0]+"이름으로 녹음이 완료되었습니다",TextToSpeech.QUEUE_FLUSH,null);
        newRecording = true;
        recordingState = false;
        startbtn.setEnabled(true);
        startbtn.setText("녹음 시작");
        stopbtn.setEnabled(false);
        backbtn.setEnabled(true);
        playbtn.setEnabled(false);
        cancelbtn.setEnabled(false);
    }

    public void onClickListen(View view) {
        if(!newRecording) {
            if(!isListen) {
                tts.speak("녹음을 재생합니다. 더블탭 하면 재생이 멈춥니다.",TextToSpeech.QUEUE_FLUSH, null);
                while(tts.isSpeaking()) {
                    // 말 끝날때까지 기달료라~
                }
                isListen = true;
                recordingState = false;
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioRecorder.getRecordFileName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }else {
                isListen = false;
                playbtn.setText("들어보기");
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
    }

    public void onClickCancel(View v) {
        if(!newRecording) {
            newRecording = true;
            recordingState = false;
            if(audioRecorder.isPaused()) {
                audioRecorder.start(new AudioRecorder.OnStartListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onException(Exception e) {

                    }
                });
            }else {
                audioRecorder.cancel();
            }
            startbtn.setText("녹음 시작");
            startbtn.setEnabled(true);
            stopbtn.setEnabled(false);
            backbtn.setEnabled(true);
            tts.speak("녹음이 취소되어 기존 내용은 저장되지 않았습니다", TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    @Override
    public void onInit(int i) {

    }

    public void onBack(View view) {
        Intent i = new Intent(this, SM_main.class);
        finish();
        startActivity(i);
    }

}
