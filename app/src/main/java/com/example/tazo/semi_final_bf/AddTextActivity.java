package com.example.tazo.semi_final_bf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    FirebaseStorage storage;
    StorageReference storageReference;
    UploadTask uploadTask;
    Uri uri;

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
        //token[3] = 0 -> 공유 안 함 / 카테고리
        //token[4] = (n) -> 같은 위치에 몇 개의 메모가 있는 지

        initFirebaseDatabase();

        Text = (EditText)findViewById(R.id.TextMemo);
        tts = new TextToSpeech(this,this);
    }
    public void onBack(View view) {
        Intent i = new Intent(this, AddMemoActivity.class);
        finish();
        startActivity(i);
    }

    private void initFirebaseDatabase() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("message");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        databaseReference.addChildEventListener(childEventListener);


       storage = FirebaseStorage.getInstance();
       storageReference = storage.getReference();
    }

    public void onSaveTextMemo(View v) {
        if(Text.getText().toString() == null) {
            nar = "메모 내용을 입력하세요";
            onInit(0);
        }else {
            File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Text/");

            boolean f = nfile.mkdirs();

            String message = nfile.getAbsolutePath() + "/" + place_name + "-(0).txt";
            String dbname = "gs://forfinal-q-ring.appspot.com/SF_SM/Text/"+place_name+"-(0).txt";
            File file = new File(message);
            int i = 1;
            if(file.exists()){
                while(file.exists()) {
                    message = nfile.getAbsolutePath()+"/"+place_name+"-("+(i++)+").txt";
                    file = new File(message);
                    dbname = "gs://forfinal-q-ring.appspot.com/SF_SM/Text/"+place_name+"-("+(i++)+").txt";
                    databaseReference.push().setValue(dbname); // 파일 저장명만 디비에 올리기
                }
            }
            else{
                databaseReference.push().setValue(dbname); // 파일 저장명만 디비에 올리기
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

                    uri = Uri.fromFile(file);
                    StorageReference uploadRef = storageReference.child("SF_SM/Text/" + file.getName());
                    uploadTask = uploadRef.putFile(uri);
                    uploadTask
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                                    //Toast.makeText(getApplicationContext(),downloadUri.toString(),Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(childEventListener);
    }
}
