package com.example.tazo.semi_final_bf;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener{

    public double sx, sy; // 시작점
    public double tx, ty; // 현재점
    public double center_x, center_y; // 중심점
    double theta; // startPoint와 currentPoint 벡터의 사이각
    double ztheta; // theta 계산시 사용

    // 컨트롤러 배열과 현재 화면에 있는 컨트롤러 번호 저장.
    Controller[] controllers = new Controller[3];
    int currentController = 0;
    int controllerNum = 1;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    // Two Finger Swipe
    int swipe = 1;
    int none = 0;
    int mode = none;
    float startX, startY, stopX, stopY;

    // Double Tap
    final long double_click_time_delta = 350;
    long lastClickTime = 0;
    int doubleTap = 2;
    int clickCount = 0;

    //TTS
    TextToSpeech tts;

    //Vibration
    Vibrator v;

    Context context;

    final long[] cpattern3 = new long[]{200,70,100,25,200,50};

    // DB
    DB_Handler db_handler;


    PackageManager pm;

    long startTime;
    long duration;
    static final int MAX_DURATION = 500;
    final long[] pattern_ms = {
            9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16,
            9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16,
            9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16, 9, 16
    };
    final long[] pattern_cll = {
            0, 200
    };
    final long[] pattern_mscl = {
            300, 200,
            0, 200,
            0, 200,
            0, 200
    };

    int CALL_SMS = 0; // 0 둘 다 없음, 1 전화만 , 2 문자만 , 3 전화, 문자

    String ADDR_CALL = "모르는 번호";

    public final String Scron = "android.intent.action.SCREEN_ON";
    public final String Scroff = "android.intent.action.SCREEN_OFF";
    Cursor cursor;


    private Cursor getSmsLog(){

        String columns[] = new String[]{
                Telephony.Sms._ID, // 행의 고유 ID
                Telephony.Sms.THREAD_ID, // 스레드 ID
                Telephony.Sms.ADDRESS, // 번호
                Telephony.Sms.PERSON, // 보낸 사람 ID
                Telephony.Sms.BODY, // 본문
                Telephony.Sms.SUBJECT, // 제목
                //Telephony.Sms.SUBSCRIPTION_ID, // 속한 구독
                Telephony.Sms.READ // 읽었니

        };
        Cursor c = getContentResolver().query(Uri.parse("content://sms"), columns, null, null,"date DESC");
        return c;
    }

    private Cursor getCallLog(){

        String columns[]=new String[] {
                CallLog.Calls._ID, // 고유 ID 넘버
                CallLog.Calls.CACHED_NAME, // 저장명
                CallLog.Calls.NUMBER, // 폰번
                CallLog.Calls.DATE, // 날짜
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE, // 수신/발신/부재중
                CallLog.Calls.IS_READ
                // 안 읽은 것은 0 읽은 것은 1
        };
        Cursor c = getContentResolver().query(Uri.parse("content://call_log/calls"),
                columns, null, null, "Calls._ID DESC");

        return c;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int CalLogPerCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG);
        int SmsLogPerCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if(CalLogPerCheck == PackageManager.PERMISSION_DENIED){
            perCheck();
        }

        if(SmsLogPerCheck == PackageManager.PERMISSION_DENIED){
            perCheck();
        }
        ////////////////////////////////////////////////////////////// 권한 확인 끝

        context = getApplicationContext();

        currentController = 0;
        controllers[0] = new Controller(context,1);
        center_x = controllers[currentController].centerX;
        center_y = controllers[currentController].centerY;

        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.KOREA);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // DB
        db_handler = DB_Handler.open(this);

        pm = context.getPackageManager();

        ScreenOn screenOn = new ScreenOn();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Scron);
        filter.addAction(Scroff);
        registerReceiver(screenOn,filter);

        CALL_SMS = readLOG();



    }
    public int readLOG(){
        cursor = getCallLog();
        if(cursor != null){
            cursor.moveToNext();
            cursor.getColumnIndex(CallLog.Calls.DATE);
            //aaa.setText(cursor.getString(0) + ", " + cursor.getString(1));
            //cursor.getString(1)이 null 일 때 다른 진동 주기 / 또는 반대
           // bbb.setText(cursor.getString(2) + ", " + cursor.getString(5) + ", " + cursor.getString(6));
            // getString(5)가 3일때 부재중인가

            if(cursor.getString(5).equals("3") && cursor.getString(6).equals("0")){
                //Toast.makeText(this,"안읽은부재중전화있음", Toast.LENGTH_LONG).show();
                CALL_SMS += 1;
                //ADDR_CALL = cursor.getString(1);
            }

            // 퍼미션 조까
        }else {
           // aaa.setText("NANANA");
        }

        cursor = getSmsLog();
        if(cursor != null){
            cursor.moveToFirst();
//            cursor.getColumnIndex(Telephony.Sms.DATE);
//            ccc.setText(cursor.getString(2)+", "+ cursor.getString(3));
//            //cursor.getString(3)이 null 일 때 다른 진동 주기 / 또는 반대
//            ddd.setText(cursor.getString(4)+", "+ cursor.getString(7));
            if(cursor.getString(6).equals("1")){
                //Toast.makeText(this,"안읽은문자있음",Toast.LENGTH_LONG).show();

                CALL_SMS += 2;

            }
        }else {
            //ccc.setText("NANANA");
        }
        if(CALL_SMS == 1){
            new NoticeCall(getApplicationContext()).NC(ADDR_CALL);
        }
        else if(CALL_SMS == 2){
            new NoticeSms(getApplicationContext()).NS();
        }
        else if(CALL_SMS == 3){
            new NoticeBoth(getApplicationContext()).NB(ADDR_CALL);
        }
        return 0;
    }
    public class NoticeCall{
        public Context context;
        public NoticeCall(Context context){this.context = context;}
        public void NC(String name){

            Intent intent = new Intent(MainActivity.this, NoticeCall.class);
            PendingIntent sender = PendingIntent.getBroadcast(context,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.min);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.max));

            builder.setTicker("전화");
            builder.setContentTitle(name + " 전화 왔다");
            builder.setContentText("읽어라");
            builder.setVibrate(pattern_cll);

            //builder.setVibrate(new long[]{0,2000});
            builder.setAutoCancel(true);
            builder.setContentIntent(sender);

            Notification notification = builder.build();
            notificationManager.notify(1,notification);
        }
    }
    public class NoticeSms{
        public Context context;
        public NoticeSms(Context context){this.context = context;}
        public void NS(){
            NotificationManager notificationManager =
                    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(MainActivity.this, NoticeSMS.class);
            PendingIntent sender = PendingIntent.getBroadcast(context,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.min);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.max));

            builder.setTicker("문자");
            builder.setContentTitle("문자 왔다");
            builder.setContentText("읽어라");
            builder.setVibrate(pattern_ms);

            //builder.setVibrate(new long[]{0,2000});
            builder.setAutoCancel(true);
            builder.setContentIntent(sender);

            Notification notification = builder.build();
            notificationManager.notify(1,notification);
        }
    }

    public class NoticeBoth{
        public Context context;
        public NoticeBoth(Context context){this.context = context;}
        public void NB(String name){
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.min);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.max));

            builder.setTicker("문자 전화");
            builder.setContentTitle(name + " 전화, 문자 왔다");
            builder.setContentText("읽어라");
            builder.setVibrate(pattern_mscl);

            //builder.setVibrate(new long[]{0,2000});

            Notification notification = builder.build();
            notificationManager.notify(1,notification);
        }
    }
    void perCheck(){
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.READ_LOGS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_SMS
                },
                0
        );
    }

    public boolean onTouchEvent(MotionEvent e) {
        final MotionEvent ee = e;
        switch (e.getActionMasked() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(e.getPointerCount() == 1) {
                    controllers[currentController].actionDown(e);
                    mode = none;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(e.getPointerCount() == 1) {
                    clickCount++;

                    Handler handler = new Handler();
                    if(clickCount == 1) {
                        startTime = System.currentTimeMillis();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(clickCount == 1 && mode != swipe) {
                                    controllers[currentController].actionUp();
                                    clickCount = 0;
                                }
                            }
                        },200);

                    }else if(clickCount == 2) {
                        long duration = System.currentTimeMillis() - startTime;
                        if(duration <= 1000) {
                            // Double Tap
                            clickCount = 0;
                            duration = 0;

                            if(controllerNum < 3) {
                                controllers[controllerNum] = new Controller(getApplicationContext(), controllerNum+1);
                                controllerNum++;
                                String alertmsg = "새 컨트롤러"+controllerNum+"이 생성되었습니다.";
                                if(controllerNum == 2) {
                                    alertmsg = "새 컨트롤러 2가 생성되었습니다.";
                                }
                                Toast.makeText(getApplicationContext(), alertmsg, Toast.LENGTH_SHORT).show();
                                tts.speak(alertmsg, TextToSpeech.QUEUE_FLUSH, null);
                                v.vibrate(100);
                            }else {
                                String warningmsg = "컨트롤러는 최대 3개입니다.";
                                Toast.makeText(getApplicationContext(), warningmsg,Toast.LENGTH_SHORT).show();
                                tts.speak(warningmsg, TextToSpeech.QUEUE_FLUSH, null);
                                v.vibrate(new long[]{200,100,200,100},-1);
                            }
                        }else {
                            clickCount = 1;
                            startTime = System.currentTimeMillis();
                        }
                        break;
                    }
                }
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = swipe;
                if(e.getPointerCount() == 2) {
                    startY = e.getY(0);
                    startX = e.getX(0);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if(e.getPointerCount() == 2 && mode == swipe) {
                    String alertmsg = "컨트롤러가 없습니다. 생성하려면 더블 탭";
                    // right to left swipe (오른쪽)
                    if (startX - stopX > SWIPE_MIN_DISTANCE && Math.abs(startX - stopX) > SWIPE_THRESHOLD_VELOCITY) {
                        if (controllerNum == 1) {
                            Toast.makeText(context, alertmsg, Toast.LENGTH_SHORT).show();
                            tts.speak(alertmsg,TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            currentController = (currentController + 1) % controllerNum;
                            if(currentController == 0) {
                                v.vibrate(cpattern3,-1);
                            }
                        }
                        tts.speak(String.valueOf(currentController+1)+"번 컨트롤러", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    // left to right swipe (왼쪽)
                    else if (stopX - startX > SWIPE_MIN_DISTANCE && Math.abs(stopX - startX) > SWIPE_THRESHOLD_VELOCITY) {
                        if (controllerNum == 1) {
                            Toast.makeText(context, alertmsg, Toast.LENGTH_SHORT).show();
                            tts.speak(alertmsg,TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            if (currentController == 0) {
                                // 처음과 끝을 이어보아요
                                currentController = controllerNum -1;
                            }else {
                                currentController = (currentController - 1) % controllerNum;
                                if(currentController == 0) {
                                    v.vibrate(cpattern3,-1);
                                }
                            }
                        }
                        tts.speak(String.valueOf(currentController+1)+"번 컨트롤러", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    // down to up swipe
                    else if (startY - stopY > SWIPE_MIN_DISTANCE && Math.abs(startY - stopY) > SWIPE_THRESHOLD_VELOCITY) {
                        // 설정 메뉴 진입!!!!!
                        String settinmsg = "설정 메뉴로 진입합니다.";
                        Toast.makeText(context, settinmsg, Toast.LENGTH_SHORT).show();
                        tts.speak(settinmsg, TextToSpeech.QUEUE_FLUSH, null);
                        Intent intent = new Intent(context, CSettingActivity.class);
                        startActivity(intent);
                    }
                    // up to down swipe
                    else if (stopY - startY > SWIPE_MIN_DISTANCE && Math.abs(stopY - startY) > SWIPE_THRESHOLD_VELOCITY) {
                        // --지연아 여기야..!-------------간단 알람 기능
                        String makeAlarm = "알람 만들기 진입합니다.";
                        Toast.makeText(context, "Make Alarm", Toast.LENGTH_SHORT).show();
                        tts.speak(makeAlarm,TextToSpeech.QUEUE_ADD, null);
                        Intent intent = new Intent(context, MakeAlarm.class);
                        startActivity(intent);
                    }
                }
                mode = none;
                break;
            case MotionEvent.ACTION_MOVE:
                if(e.getPointerCount() == 2) {
                    stopY = e.getY(0);
                    stopX = e.getX(0);
                }else {
                    calculate_theta(e);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mode = none;
                break;
        }
        return true;
    }

    public void calculate_theta(MotionEvent e) {
        tx = e.getX();
        ty = e.getY();

        double v1x = sx - center_x;
        double v1y = sy - center_y;

        double v2x = tx - center_x;
        double v2y = ty - center_y;

        double temp1 = v1x * v2x + v1y * v2y;
        double temp2 = Math.sqrt(v1x * v1x + v1y * v1y);
        temp2 = temp2 * Math.sqrt(v2x * v2x + v2y * v2y);
        // 두 벡터의 사이각을 구해준다

        ztheta = Math.acos(temp1 / temp2);
        theta = (v1x * v2y - v1y * v2x > 0.0d) ? ztheta : -ztheta;
        theta = Math.toDegrees(theta);

        if (theta <= 60 && theta >= 60 - 5) {
            //if(controllers[currentController].icon1 != null) {
            // 아이콘이 설정된 것이 없으면 진동도 안울리게 했음.
            v.vibrate(150);
            controllers[currentController].flaggingIcon(0);
            tts.speak(controllers[currentController].icon1Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
        if (theta <= 120 && theta >= 120 - 5) {
            //if(controllers[currentController].icon2 != null) {

            v.vibrate(150);
            controllers[currentController].flaggingIcon(1);
            tts.speak(controllers[currentController].icon2Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
        // 세번째 앱 : 카카오톡을 해보자~
        if (theta <= 180 && theta >= 180 - 5) {
            //if(controllers[currentController].icon3 != null) {
            v.vibrate(150);
            controllers[currentController].flaggingIcon(2);
            tts.speak(controllers[currentController].icon3Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
        // 네번째 앱 : 인터넷을 해보자
        if (theta <= -120 && theta >= -120 - 5) {
            //if(controllers[currentController].icon4 != null) {
            v.vibrate(150);
            controllers[currentController].flaggingIcon(3);
            tts.speak(controllers[currentController].icon4Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
        // 다섯번째 앱 : 포켓몬 고를 해보자~
        if (theta <= -60 && theta >= -60 - 5) {
            //if(controllers[currentController].icon5 != null) {
            v.vibrate(150);
            controllers[currentController].flaggingIcon(4);
            tts.speak(controllers[currentController].icon5Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
    }

    @Override
    public void onInit(int i) {

    }

    public class ScreenOn extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Scron)) {
                CALL_SMS = readLOG();
            }
            else if(intent.getAction().equals(Scroff)){

            }
        }
    }
}
