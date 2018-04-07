package com.example.tazo.semi_final_bf;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class AddTextActivity extends AppCompatActivity implements  TextToSpeech.OnInitListener {

    EditText Text;

    TextToSpeech tts;
    String nar = null;
    CharSequence place_name;
    String pre_token; // 토큰 끊기전
    String[] token; // 토큰 끊은 후

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);

        setTitle("텍스트 메모 추가하기");

        Intent intent = getIntent();
        place_name =  intent.getCharSequenceExtra("place_name");
        pre_token = String.valueOf(place_name);
        token = pre_token.split("-");
        //token[0] = 장소 이름
        //token[1] = latitude
        //token[2] = longitude
        //token[3] = 0 -> 공유 안 함 / 1 -> 공유 함

        Text = (EditText)findViewById(R.id.TextMemo);
        tts = new TextToSpeech(this,this);
    }
    public void onBack(View view) {
        Intent i = new Intent(this, AddMemoActivity.class);
        finish();
        startActivity(i);
    }

    public void onSaveTextMemo(View v) {
        if(Text.getText().toString() == null) {
            nar = "메모 내용을 입력하세요";
            onInit(0);
        }else {
            File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SpotMemo/Text/");

            boolean f = nfile.mkdirs();

            File file = new File(nfile.getAbsolutePath() + "/" + place_name + "-(0).txt");
            int i = 1;
            while(file.exists()) {
                file = new File(nfile.getAbsolutePath()+"/"+place_name+"-("+(i++)+").txt");
            }

            FileWriter fw = null;
            BufferedWriter buf = null;

            try {
                fw = new FileWriter(file);
                buf = new BufferedWriter(fw);
                buf.write(Text.getText().toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(buf != null) {
                    buf.close();
                    nar = "저장이 완료되었습니다. 메인으로 돌아갑니다.";
                    onInit(0);
                    startActivity(new Intent(this, MainActivity.class));
                    while(tts.isSpeaking()) {

                    }
                    finish();
                }
                if(fw != null) {
                    fw.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onInit(int status) {
        tts.speak(nar,TextToSpeech.QUEUE_FLUSH,null);
    }

}
