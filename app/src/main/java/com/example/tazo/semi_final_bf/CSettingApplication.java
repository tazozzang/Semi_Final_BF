package com.example.tazo.semi_final_bf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tazo on 2017-04-26.
 */

public class CSettingApplication extends Activity implements TextToSpeech.OnInitListener {
    TextToSpeech tts;
    PackageManager pm;
    List<ResolveInfo> list;
    ArrayList<ActivityInfo> applist;
    ArrayAdapter adapter;
    ListView listView;
    Intent i;
    int cnum, inum;
    TextView textView;

    String nar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capplication);

        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.KOREA);

        listView = (ListView)findViewById(R.id.listview);
        textView = (TextView)findViewById(R.id.ca_title);
        getAppList();

        i = getIntent();
        cnum = i.getIntExtra("cnum",-1);
        inum = i.getIntExtra("inum",-1);

        textView.setText("컨트롤러 "+cnum+", 아이콘 "+inum);

        nar = "어플리케이션을 선택하세요."+"볼륨키나 터치를 이용하여 목록을 읽고, 더블탭으로 설정을 완료하세요.";

    }

    @Override
    public void onInit(int status) {
        tts.speak(nar,TextToSpeech.QUEUE_FLUSH, null);
    }

    int keyPosition = 0;
    String keyChosenPName = null;
    String keyChosenName = null;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(keyPosition <= 0) {
                    keyPosition = 0;
                    tts.speak("목록의 처음입니다.", TextToSpeech.QUEUE_FLUSH, null);
                }else {
                    keyPosition--;
                }

                keyChosenPName = list.get(keyPosition).activityInfo.applicationInfo.packageName;
                keyChosenName = list.get(keyPosition).activityInfo.applicationInfo.loadLabel(pm).toString();
                tts.speak(String.valueOf(keyChosenName),TextToSpeech.QUEUE_FLUSH,null);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(keyPosition >= list.size()-1) {
                    keyPosition = list.size()-1;
                    tts.speak("목록의 끝입니다.", TextToSpeech.QUEUE_FLUSH, null);
                }else {
                    keyPosition++;
                }

                keyChosenPName = list.get(keyPosition).activityInfo.applicationInfo.packageName;
                keyChosenName = list.get(keyPosition).activityInfo.applicationInfo.loadLabel(pm).toString();
                tts.speak(String.valueOf(keyChosenName),TextToSpeech.QUEUE_FLUSH,null);
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void getAppList() {
        pm = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        list = pm.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        applist = new ArrayList<>();

        adapter = new ArrayAdapter(getApplicationContext(), R.layout.row);
        listView.setAdapter(adapter);
        for(int i = 0 ; i < list.size() ; i++) {
            ResolveInfo resolveInfo = list.get(i);
            String pName = resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
            applist.add(list.get(i).activityInfo);
            adapter.add(pName);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = ((TextView)view).getText().toString();
                tts.speak(name,TextToSpeech.QUEUE_FLUSH, null);
                final String chosenPname = list.get(position).activityInfo.applicationInfo.packageName;
                final String chosenName = ((TextView) view).getText().toString();

                tts.speak("컨트롤러 "+cnum+", 아이콘 "+inum+"을 "+chosenName+"으로 바꾸시겠습니까?",TextToSpeech.QUEUE_FLUSH,null);

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(CSettingApplication.this);
                alert_confirm.setMessage("컨트롤러 "+cnum+", 아이콘 "+inum+"을 "+chosenName+"으로 바꾸시겠습니까?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DB_Handler db_handler = new DB_Handler(getApplicationContext(), null, null, 1);
                        DB_Controller res = db_handler.findIcon(cnum, inum);
                        if(res.pname != null) {
                            // 아이콘에 설정된 앱이 있었다면 update
                            boolean result = db_handler.updateIcon(cnum, inum, chosenPname);
                            if (result) {
                                nar = "컨트롤러 수정 완료!";
                                Toast.makeText(getApplicationContext(), nar, Toast.LENGTH_SHORT).show();
                                onInit(0);
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("result","ok");
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            } else {
                                nar = "컨트롤러 수정 실패";
                                Toast.makeText(getApplicationContext(), nar, Toast.LENGTH_SHORT).show();
                                onInit(0);
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("result","no");
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                        }else {
                            // 아이콘에 설정된 앱이 없었다면 add(insert)
                            long ress = db_handler.addIcon(cnum, inum, chosenPname);
                            if(ress != -1) {
                                nar = "컨트롤러 수정 완료!";
                                Toast.makeText(getApplicationContext(), nar, Toast.LENGTH_SHORT).show();
                                onInit(0);
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("result","ok");
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }else {
                                nar = "컨트롤러 수정 실패";
                                Toast.makeText(getApplicationContext(), nar, Toast.LENGTH_SHORT).show();
                                onInit(0);
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("result","no");
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                        }
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //No
                    }
                });
                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        });
        onInit(0);
    }
}
