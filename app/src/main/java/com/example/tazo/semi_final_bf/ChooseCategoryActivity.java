package com.example.tazo.semi_final_bf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ChooseCategoryActivity extends AppCompatActivity  implements TextToSpeech.OnInitListener{
    TextToSpeech tts;
    String nar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_category);
        setTitle("카테고리 선택");
        setContentView(R.layout.activity_choose_category);
        tts = new TextToSpeech(this, this);
        nar = "공유될 메모의 카테고리를 선택해주세요";
        onInit(0);
    }
    public void onClickCafe(View v) {
        // 카페
        Intent returnIntent = new Intent();
        returnIntent.putExtra("category","cafe");
        setResult(Activity.RESULT_OK, returnIntent);
        nar = "카페를 선택하셨습니다. 메인으로 돌아갑니다";
        onInit(0);
        finish();
    }

    public void onClickFood(View v) {
        // 식당
        Intent returnIntent = new Intent();
        returnIntent.putExtra("category","food");
        setResult(Activity.RESULT_OK, returnIntent);
        nar = "식당을 선택하셨습니다.";
        onInit(0);
        finish();
    }

    public void onClickTheater(View v) {
        // 영화관/공연장
        Intent returnIntent = new Intent();
        returnIntent.putExtra("category","theater");
        setResult(Activity.RESULT_OK, returnIntent);
        nar = "영화관, 공연장을 선택하셨습니다.";
        onInit(0);
        finish();
    }

    public void  onClickShop(View v){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("category","shop");
        setResult(Activity.RESULT_OK, returnIntent);
        nar = "옷가게를 선택하셨습니다.";
        onInit(0);
        finish();
    }

    public void onClickPublic(View v) {
        // 공공시설
        Intent returnIntent = new Intent();
        returnIntent.putExtra("category","public");
        setResult(Activity.RESULT_OK, returnIntent);
        nar = "공공시설을 선택하셨습니다.";
        onInit(0);
        finish();
    }

    public void onClickEtc(View v) {
        // 기타
        Intent returnIntent = new Intent();
        returnIntent.putExtra("category","etc");
        setResult(Activity.RESULT_OK, returnIntent);
        nar = "기타를 선택하셨습니다.";
        onInit(0);
        finish();
    }

    public void onBack(View v) {
        nar = "공유를 취소하고 뒤로 돌아갑니다.";
        onInit(0);
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onInit(int i) {

    }
}
