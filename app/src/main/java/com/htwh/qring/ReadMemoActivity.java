package com.htwh.qring;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReadMemoActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    int type;
    String path;
    File f;
    TextView tv;
    TextToSpeech tts;
    boolean isFirst = true;
    String s = "";
    String[] token; // 토큰 끊은 후
    String[] second_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_memo);

        Intent i = getIntent();
        type = i.getIntExtra("type",-1);
        path = i.getStringExtra("file");

        f = new File(path);
        setTitle(f.getName()+"의 내용");
        String path = String.valueOf(f.getName());

        if(type == 1)
            token = path.split("\\.txt");
        if(type == 0)
            token = path.split("\\.mp4");

        second_token = token[0].split("-");

        tv = (TextView)findViewById(R.id.content);
        tts = new TextToSpeech(this, this);


        if(type == 1) {
            char ch;
            int data;
            FileReader fr = null;
            try {
                fr = new FileReader(f);
                while((data = fr.read()) != -1) {
                    ch = (char)data;
                    s = s+ch;
                }
                fr.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
            if(s != "") {
                tv.setText(s);
            }
        }

    }

    public void onClickMemoListen(View v) {
        if(type == 0) {
            // 음성 파일이면 재생
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        }else if(type == 1) {
            // 텍스트 파일이면 tts로 읽어주기
            if(s != "") {
                tts.speak(s, TextToSpeech.QUEUE_FLUSH,null);
            }else {
                tts.speak("파일 내용이 없습니다.",TextToSpeech.QUEUE_FLUSH,null);
            }
        }else {
            tts.speak("파일이 제대로 불려지지 않았습니다.",TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    public void onClickDelete(View v) {
        if(isFirst) {
            tts.speak(f.getName()+"을 정말로 삭제하시겠습니까? 삭제를 원하시면 더블 탭 하세요.",TextToSpeech.QUEUE_FLUSH,null);
            isFirst = false;
        }else {
            isFirst = true;
            tts.speak(f.getName()+"을 삭제하고 메인으로 돌아갑니다.",TextToSpeech.QUEUE_FLUSH,null);
            f.delete();
            while(tts.isSpeaking()) {

            }
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            StorageReference desertRef = storageRef.child("images/desert.jpg");

            desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                }
            });
            Intent i = new Intent(this, MainActivity.class);
            finish();
            startActivity(i);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            String category = data.getStringExtra("category");
            tts.speak("공유를 설정하고 메모창으로 돌아왔습니다.", TextToSpeech.QUEUE_FLUSH,null);

            switch (category) {
                case "cafe":
                    String change_name = second_token[0]+"-"+second_token[1]+"-"+second_token[2]+"-1-"+second_token[4];
                    if(type == 0){
                        try {
                            FileInputStream fin = new FileInputStream(f);
                            byte b[] = new byte[(int)f.length()];
                            fin.read(b);
                            File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Voice/");
                            File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".mp4");

                            FileOutputStream fo = new FileOutputStream(file);
                            fo.write(b);
                            fo.flush();
                            fo.close();

                            f.delete();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }else if(type ==1){
                        File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Text/");
                        File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".txt");

                        FileWriter fw = null;
                        BufferedWriter buf = null;
                        try {
                            fw = new FileWriter(file);
                            buf = new BufferedWriter(fw);
                            buf.write(s);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            buf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        f.delete();

                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }
                    break;
                case "food":
                    change_name = second_token[0]+"-"+second_token[1]+"-"+second_token[2]+"-2-"+second_token[4];
                    if(type == 0){
                        try {
                            FileInputStream fin = new FileInputStream(f);
                            byte b[] = new byte[(int)f.length()];
                            fin.read(b);
                            File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Voice/");
                            File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".mp4");

                            FileOutputStream fo = new FileOutputStream(file);
                            fo.write(b);
                            fo.flush();
                            fo.close();

                            f.delete();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }else if(type ==1){
                        File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Text/");
                        File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".txt");
                        FileWriter fw = null;
                        BufferedWriter buf = null;
                        try {
                            fw = new FileWriter(file);
                            buf = new BufferedWriter(fw);
                            buf.write(s);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            buf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        f.delete();

                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }
                    break;
                case "theater":
                    change_name = second_token[0]+"-"+second_token[1]+"-"+second_token[2]+"-3-"+second_token[4];
                    if(type == 0){
                        try {
                            FileInputStream fin = new FileInputStream(f);
                            byte b[] = new byte[(int)f.length()];
                            fin.read(b);
                            File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Voice/");
                            File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".mp4");

                            FileOutputStream fo = new FileOutputStream(file);
                            fo.write(b);
                            fo.flush();
                            fo.close();

                            f.delete();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }else if(type ==1){
                        File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Text/");
                        File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".txt");

                        FileWriter fw = null;
                        BufferedWriter buf = null;
                        try {
                            fw = new FileWriter(file);
                            buf = new BufferedWriter(fw);
                            buf.write(s);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            buf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        f.delete();

                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }
                    break;
                case "shop":
                    change_name = second_token[0]+"-"+second_token[1]+"-"+second_token[2]+"-4-"+second_token[4];
                    if(type == 0){
                        try {
                            FileInputStream fin = new FileInputStream(f);
                            byte b[] = new byte[(int)f.length()];
                            fin.read(b);
                            File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Voice/");
                            File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".mp4");

                            FileOutputStream fo = new FileOutputStream(file);
                            fo.write(b);
                            fo.flush();
                            fo.close();

                            f.delete();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }else if(type ==1){
                        File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Text/");
                        File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".txt");

                        FileWriter fw = null;
                        BufferedWriter buf = null;
                        try {
                            fw = new FileWriter(file);
                            buf = new BufferedWriter(fw);
                            buf.write(s);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            buf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        f.delete();

                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }
                    break;
                case "public":
                    change_name = second_token[0]+"-"+second_token[1]+"-"+second_token[2]+"-5-"+second_token[4];
                    if(type == 0){
                        try {
                            FileInputStream fin = new FileInputStream(f);
                            byte b[] = new byte[(int)f.length()];
                            fin.read(b);
                            File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Voice/");
                            File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".mp4");

                            FileOutputStream fo = new FileOutputStream(file);
                            fo.write(b);
                            fo.flush();
                            fo.close();

                            f.delete();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }else if(type ==1){
                        File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Text/");
                        File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".txt");

                        FileWriter fw = null;
                        BufferedWriter buf = null;
                        try {
                            fw = new FileWriter(file);
                            buf = new BufferedWriter(fw);
                            buf.write(s);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            buf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        f.delete();

                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }
                    break;
                case "etc":
                    change_name = second_token[0]+"-"+second_token[1]+"-"+second_token[2]+"-6-"+second_token[4];
                    if(type == 0){
                        try {
                            FileInputStream fin = new FileInputStream(f);
                            byte b[] = new byte[(int)f.length()];
                            fin.read(b);
                            File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Voice/");
                            File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".mp4");

                            FileOutputStream fo = new FileOutputStream(file);
                            fo.write(b);
                            fo.flush();
                            fo.close();

                            f.delete();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);
                    }else if(type ==1){
                        File nfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SF_SM/Text/");
                        File file = new File(nfile.getAbsolutePath() + "/" + change_name + ".txt");

                        FileWriter fw = null;
                        BufferedWriter buf = null;
                        try {
                            fw = new FileWriter(file);
                            buf = new BufferedWriter(fw);
                            buf.write(s);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            buf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        f.delete();

                        Intent i = new Intent(this, MyMemoActivity.class);
                        finish();
                        startActivity(i);

                    }
                    break;
            }

        }else {
            tts.speak("공유를 취소하고 메모창으로 돌아왔습니다.",TextToSpeech.QUEUE_FLUSH,null);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickShare(View v) {
        tts.speak("공유 설정을 위해 카테고리 선택 창으로 진입합니다",TextToSpeech.QUEUE_FLUSH,null);
        while(tts.isSpeaking()) {

        }
        startActivityForResult(new Intent(this, ChooseCategoryActivity.class),0);
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
