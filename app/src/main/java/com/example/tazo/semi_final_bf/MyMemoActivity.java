package com.example.tazo.semi_final_bf;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.ArrayList;

public class MyMemoActivity extends AppCompatActivity  implements TextToSpeech.OnInitListener{
    ListView listView;
    ArrayAdapter<String> adapter;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    ArrayList<String> fName;
    ArrayList<String> fPath;
    ArrayList<Integer> fType;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_memo);
        setTitle("내가 남긴 메모");

        tts = new TextToSpeech(this,this);

        fName = new ArrayList<>();
        fPath = new ArrayList<>();
        fType = new ArrayList<>();
        listView = (ListView)findViewById(R.id.mlistView);


        //initFire();


        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            File files = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/SF_SM/Voice");

            ArrayAdapter<String> filelist = new ArrayAdapter<String>(this, R.layout.mylist, fName);

            if (files.listFiles().length > 0) {
                for (File file : files.listFiles()) {
                    // 음성 메모이면 0
//                    String name_split[] = file.getName().split("-");
                    String name_split[] = file.getName().split("-");
                    String num_split[] = name_split[4].split("\\.");
                    fName.add(name_split[0] + "에 저장된 " + num_split[0] + "번째 메모");
                    //+ name_split[3] + "번 카테고리 "
                    fPath.add(file.getAbsolutePath());
                    fType.add(0);
                }
            }
            files = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/SF_SM/Text");
            if (files.listFiles().length > 0) {
                for (File file : files.listFiles()) {
                    // 텍스트 메모이면 1
                    String name_split[] = file.getName().split("-");
                    String num_split[] = name_split[4].split("\\.");
                    fName.add(name_split[0] + "에 저장된 " + num_split[0] + "번째 메모");
                    fPath.add(file.getAbsolutePath());
                    fType.add(1);
                }
            }
            listView.setAdapter(filelist);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getApplicationContext(), ReadMemoActivity.class);
                    if (fType.get(position) == 0) {
                        // 음성 파일이면 재생
                        i.putExtra("type", 0);
                        i.putExtra("file", fPath.get(position));
                        startActivity(i);
                    } else {
                        i.putExtra("type", 1);
                        i.putExtra("file", fPath.get(position));
                        startActivity(i);
                    }
                }
            });
        }
    }

    private void initFire(){

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("message");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String message = dataSnapshot.getValue(String.class);

                fPath.add(message);
                fType.add(1);

                String storage_split[] = message.split("com/");
                String name_split[] = storage_split[1].split("/");
                String num_split[] = name_split[2].split("-");
                String dot_split[] = num_split[4].split("\\.");
                fName.add(num_split[0]+"에 저장된 " + dot_split[0]+"번째 텍스트 메모");

                adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.mylist, fName);
                listView.setAdapter(adapter);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String message = dataSnapshot.getValue(String.class);
                adapter.remove(message);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        databaseReference.addChildEventListener(childEventListener);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //adapter.notifyDataSetChanged();
      //  databaseReference.removeEventListener(childEventListener);
    }


    public void onBack(View view) {
        Intent i = new Intent(this, SM_main.class);
        finish();
        startActivity(i);
    }

    @Override
    public void onInit(int i) {

    }
}
