package com.example.tazo.semi_final_bf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class AddMemoActivity extends AppCompatActivity {
    CharSequence place_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memo);
        setTitle("메모 추가");
        Intent intent = getIntent();
        place_name =  intent.getCharSequenceExtra("place_name");

    }

    public void onClickAddVoiceMemo(View view) {
        Intent i = new Intent(this, AddVoiceActivity.class);
        i.putExtra("place_name",place_name);
        startActivity(i);
    }

    public void onClickAddTextMemo(View view) {
        Intent i = new Intent(this, AddTextActivity.class);
        i.putExtra("place_name",place_name);
        startActivity(i);
    }

    public void onBack(View view) {
        Intent i = new Intent(this, SM_main.class);
        finish();
        startActivity(i);
    }
}
