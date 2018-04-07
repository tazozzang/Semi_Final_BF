package com.example.tazo.semi_final_bf;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;

public class AddVoiceActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_voice);
    }

    @Override
    public void onInit(int i) {

    }
}
