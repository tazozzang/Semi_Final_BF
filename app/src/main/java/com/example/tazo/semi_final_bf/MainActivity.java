package com.example.tazo.semi_final_bf;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener, GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener{
    //*[자동 롤링]에 쓰는 변수들
    private static final int TIMER_START = 10;
    private static final int TIMER_STOP = 11;
    private static final int TIMER_REPEAT = 12;
    private static final int TIME_DElAY = 700;//검사 시간 간격.
    private TimeCounter timeCounter = new TimeCounter();
    //*/////////////////////

    private GoogleApiClient googleApiClient;

    public double sx, sy; // 시작점
    public double tx, ty; // 현재점
    public double center_x, center_y; // 중심점
    double theta; // startPoint와 currentPoint 벡터의 사이각
    double ztheta; // theta 계산시 사용

    // 컨트롤러 모드(4), 바둑판 모드(5)
    int view_mode = 4;

    // 바둑판 배열
    GridSetting gridSetting;
    int gridIndex = 0;
    List<View> GridList;

    String focusedName = null; // 현재 포커싱 된 아이콘의 이름
    String focusedPName = null; // 현재 포커싱 된 아이콘의 패키지명
    int focusedNum = 0; //포커싱 된 아이콘 번호.( 현재 페이지 기준 0~8)

    // 컨트롤러 배열과 현재 화면에 있는 컨트롤러 번호 저장.
    Controller[] controllers = new Controller[3];
    int currentController = 0;
    int controllerNum = 1;

    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;

    // Two Finger Swipe
    int swipe = 1;
    int none = 0;
    int swipe_mode = none;
    float startX, startY, stopX, stopY;

    // Single Finger Swipe & Two Finger Swipe
    boolean isThereSSwipe = false;
    boolean isThereTSwipe = false;

    // Double Tap
    int clickCount = 0;

    //TTS
    TextToSpeech tts;

    //Vibration
    Vibrator v;

    Context context;
    int REQUEST_CHANGE = 1;
    int REQUEST_RETURN = 2;
    int REQUEST_CONTROLLER_MODE = 4;
    int REQUEST_GRID_MODE = 5;

    final long[] cpattern3 = new long[]{200,70,100,25,200,50};

    // DB
    DB_Handler db_handler;

    ImageView appImage;

    PackageManager pm;

    long startTime;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onStart() {
        super.onStart();

        if(!googleApiClient.isConnected())
            googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("activity_main", Activity.MODE_PRIVATE);

        editor = preferences.edit();

        boolean checkNavi = preferences.getBoolean("init",false);
        if(!checkNavi){
            Intent naviIntent = new Intent(getApplicationContext(), NaviActivity.class);
            startActivity(naviIntent);
            editor.clear();
            editor.putBoolean("init", true);
            editor.commit();
        }


        db_handler = DB_Handler.open(this);
        view_mode = db_handler.getMode();

        GridList = new ArrayList<>();
        gridSetting = new GridSetting();

        if(view_mode == 4) {
            setContentView(R.layout.activity_main);
        }else if(view_mode == 5) {
            setContentView(R.layout.activity_grid_main);

            GridList.add(findViewById(R.id.oneone));
            GridList.add(findViewById(R.id.onetwo));
            GridList.add(findViewById(R.id.onethree));
            GridList.add(findViewById(R.id.twoone));
            GridList.add(findViewById(R.id.twotwo));
            GridList.add(findViewById(R.id.twothree));
            GridList.add(findViewById(R.id.threeone));
            GridList.add(findViewById(R.id.threetwo));
            GridList.add(findViewById(R.id.threethree));

            gridSetting.setGrid(this, gridIndex, GridList);
            focusedNum = 0;
            focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum,true);
            focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
        }else {
            // DB에 저장된 view mode가 없음
            view_mode = 4; // 기본 모드는 컨트롤러 모드
            long result = db_handler.setMode(view_mode);
            if(result == -1) {
                Toast.makeText(this, "DB ERROR!!",Toast.LENGTH_LONG).show();
            }
            setContentView(R.layout.activity_main);
        }

        googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();



        int PerCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);


        if(PerCheck == PackageManager.PERMISSION_DENIED){
            perCheck();
        }
        ////////////////////////////////////////////////////////////// 권한 확인 끝

        context = getApplicationContext();

        tts = new TextToSpeech(this, this);

        tts.setLanguage(Locale.KOREA);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // DB
        currentController = 0;
        controllerNum = db_handler.howManyController();
        for(int i = 0; i < controllerNum; i ++) {
            controllers[i] = new Controller(context, i+1);
        }
        db_handler.addIcon(1,1,"spotmemo");

        center_x = controllers[currentController].centerX;
        center_y = controllers[currentController].centerY;

        pm = context.getPackageManager();

        appImage = (ImageView)findViewById(R.id.app_image);

        // 앱 실행할 때 포커스 된 앱 이름 읽어주려고 추가한 핸들러
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tts.speak(focusedName, TextToSpeech.QUEUE_FLUSH,null);  //speak after 1000ms
            }
        }, 500);
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Toast.makeText(this,"Connect",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Toast.makeText(this,"Suspend",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Toast.makeText(this,"Fail",Toast.LENGTH_SHORT).show();
    }

    private  ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(@NonNull Result result) {
            String resultC = "Sending " + result.getStatus().isSuccess();

            Toast.makeText(getApplicationContext(),resultC,Toast.LENGTH_SHORT).show();
        }
    };

    void perCheck(){
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.RECORD_AUDIO
                },
                0
        );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CHANGE) {
            if(resultCode == Activity.RESULT_OK) {
                // 설정 후, 바로 적용되도록 컨트롤러를 초기화시켜줍니다.
                controllerNum = db_handler.howManyController();
                for (int i = 0; i < controllerNum; i++) {
                    controllers[i] = new Controller(context, i + 1);
                }
                swipe_mode = none;
                clickCount = 0;

                tts.speak("메인 화면에 진입하였습니다.", TextToSpeech.QUEUE_FLUSH, null);
                v.vibrate(cpattern3, -1);
            }
            if(resultCode == REQUEST_RETURN) {
                tts.speak("메인 화면에 진입하였습니다.",TextToSpeech.QUEUE_FLUSH,null);
                v.vibrate(cpattern3,-1);
            }
            if(resultCode == REQUEST_CONTROLLER_MODE) {
                // resultCode == 4 : 컨트롤러 모드 변경했을 때 적용해야 될 부분
                view_mode = 4;
                int result = db_handler.updateMode(view_mode);
                if(result != -1) {
                    setContentView(R.layout.activity_main);
                    tts.speak("메인 화면에 진입하였습니다. 컨트롤러 모드로 변경되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    v.vibrate(cpattern3, -1);
                }else {
                    Toast.makeText(this,"Mode Setting Error!!",Toast.LENGTH_SHORT).show();
                }
            }
            if(resultCode == REQUEST_GRID_MODE) {
                // resultCode == 5 : 바둑판 모드 변경했을 때 적용해야 될 부분
                view_mode = 5;
                int result = db_handler.updateMode(view_mode);
                if(result != -1) {
                    setContentView(R.layout.activity_grid_main);
                    tts.speak("메인 화면에 진입하였습니다. 격자 모드로 변경되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    v.vibrate(cpattern3, -1);

                    GridList.add(findViewById(R.id.oneone));
                    GridList.add(findViewById(R.id.onetwo));
                    GridList.add(findViewById(R.id.onethree));
                    GridList.add(findViewById(R.id.twoone));
                    GridList.add(findViewById(R.id.twotwo));
                    GridList.add(findViewById(R.id.twothree));
                    GridList.add(findViewById(R.id.threeone));
                    GridList.add(findViewById(R.id.threetwo));
                    GridList.add(findViewById(R.id.threethree));

                    gridIndex = 0;
                    gridSetting.setGrid(this, gridIndex, GridList);
                }else {
                    Toast.makeText(this,"Mode Setting Error!!",Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    // [자동 롤링] 타이머 증가할 때마다 앱 번호 가져오는 함수.
    public void onTimeWork(int count){
        /**
         * gridIndex : 현재 페이지 첫번째 앱의 번호.
         * totalAppNum : 전체 앱 개수.
         */
        //앱개수로 나눳을 때 나머지
        //현재 페이지에 앱이 몇개인지 아는 방법: totalAppNum >= gridIndex + 9 == true 면 9개. 아니면 totalAppNum - gridIndex개.
        int appNum  = 0;
        int totalAppNum = gridSetting.gridLimit;
        if(totalAppNum >= gridIndex + 9){
            appNum = 9;
        }else{
            appNum = totalAppNum - gridIndex;
        }
        int number = count%appNum;
        if(focusedNum != number) {
            //포커스를 바꿔줌
            focusedNum = number;
            Log.d("test","Focused num - auto: "+focusedNum);
            //Toast.makeText(getApplicationContext(),"Focused num: "+focusedNum,Toast.LENGTH_SHORT).show();
            focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
            focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
        }
    }

    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getActionMasked() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.d("touch","ACTION_DOWN");
                timeCounter.sendEmptyMessage(TIMER_START);
                sx = e.getX();
                sy = e.getY();
                swipe_mode = none;
                if(view_mode == 4) {
                    if (e.getPointerCount() == 1) {
                        controllers[currentController].actionDown(e);
                        //[자동 롤링] 타이머 시작
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d("touch","ACTION_UP");
                //[자동 롤링] 롱프레스 타이머 끝.
                timeCounter.sendEmptyMessage(TIMER_STOP);
                int FingerNum = e.getPointerCount();
                // left to right
                if(isThereTSwipe) {
                    isThereTSwipe = false;
                }
                else if (tx - sx > SWIPE_MIN_DISTANCE && Math.abs(tx - sx) > SWIPE_THRESHOLD_VELOCITY) {
                    if(view_mode == 5 && FingerNum == 1) {
                        // Single Swipe Right => 오른쪽으로 포커스 이동
                        isThereSSwipe = true;
                        if (focusedNum + 1 < 9) {
                            focusedNum = focusedNum + 1;
                            focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
                            focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
                            tts.speak(focusedName, TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            // 다음 페이지로 이동
                            if (gridIndex + 9 < gridSetting.getLimit(context)) {
                                if (gridIndex == 0) {
                                    gridIndex = gridIndex + 8;
                                } else {
                                    gridIndex = gridIndex + 9;
                                }
                                gridSetting.setGrid(this, gridIndex, GridList);
                                focusedNum = 0;
                                focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
                                focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
                                tts.speak("다음 페이지로 이동했습니다.", TextToSpeech.QUEUE_FLUSH, null);
                                tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                v.vibrate(cpattern3, -1);
                            } else {
                                Toast.makeText(this, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show();
                                tts.speak("마지막 페이지입니다.", TextToSpeech.QUEUE_FLUSH, null);
                                tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                v.vibrate(cpattern3, -1);
                            }
                        }
                    }
                }
                // right to left
                else if (sx - tx > SWIPE_MIN_DISTANCE && Math.abs(sx - tx) > SWIPE_THRESHOLD_VELOCITY) {
                    if(view_mode == 5 && FingerNum == 1) {
                        // Single Swipe Left => 왼쪽으로 포커스 이동
                        isThereSSwipe = true;
                        if (focusedNum - 1 >= 0) {
                            focusedNum = focusedNum - 1;
                            focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
                            focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
                            tts.speak(focusedName, TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            // 이전 페이지로 이동
                            if(gridIndex - 8 >= 0) {
                                if(gridIndex == 8) {
                                    gridIndex = 0;
                                }else {
                                    gridIndex = gridIndex - 9;
                                }
                                gridSetting.setGrid(this, gridIndex, GridList);
                                focusedNum = 8;
                                focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
                                focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
                                tts.speak("이전 페이지로 이동했습니다.", TextToSpeech.QUEUE_ADD, null);
                                tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                v.vibrate(cpattern3, -1);
                            }else {
                                Toast.makeText(this, "첫 번째 페이지입니다.",Toast.LENGTH_SHORT).show();
                                tts.speak("첫 번째 페이지입니다.", TextToSpeech.QUEUE_ADD, null);
                                tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                v.vibrate(cpattern3, -1);
                            }
                        }
                    }
                }
                else {
                    if (FingerNum == 1) {
                        if (swipe_mode != swipe) {
                            clickCount++;
                        }
                        Handler handler = new Handler();
                        if (clickCount == 1) {
                            startTime = System.currentTimeMillis();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (view_mode == 4) {
                                        if (clickCount == 1 && swipe_mode != swipe) {
                                            controllers[currentController].actionUp();
                                            clickCount = 0;
                                        }
                                    } else {
                                        // 아이콘 포커스
                                        if(!isThereSSwipe) {
                                            focusedNum = calculate_grid(sx, sy) - 1;
                                            focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
                                            focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
                                            tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                            Log.d("test", "Focused num" + focusedNum);
                                            // [자동 롤링] 현재 포커스 숫자를 바꿈.
                                            timeCounter.changeFocusedNum(focusedNum - 1);
                                        }else {
                                            isThereSSwipe = false;
                                        }
                                    }
                                }
                            }, 200);

                        } else if (clickCount == 2) {
                            long duration = System.currentTimeMillis() - startTime;
                            if (duration <= 1000) { // ** 값 조정 필요
                                if (view_mode == 4) {
                                    // Double Tap
                                    if (controllerNum < 3) {
                                        controllers[controllerNum] = new Controller(getApplicationContext(), controllerNum + 1);
                                        controllerNum++;
                                        String alertmsg = "새 컨트롤러" + controllerNum + "이 생성되었습니다.";
                                        if (controllerNum == 2) {
                                            alertmsg = "새 컨트롤러 2가 생성되었습니다.";
                                        }
                                        Toast.makeText(getApplicationContext(), alertmsg, Toast.LENGTH_SHORT).show();
                                        tts.speak(alertmsg, TextToSpeech.QUEUE_FLUSH, null);
                                        v.vibrate(100);
                                    } else {
                                        String warningmsg = "컨트롤러는 최대 3개입니다.";
                                        Toast.makeText(getApplicationContext(), warningmsg, Toast.LENGTH_SHORT).show();
                                        tts.speak(warningmsg, TextToSpeech.QUEUE_FLUSH, null);
                                        v.vibrate(new long[]{200, 100, 200, 100}, -1);
                                    }
                                } else {
                                    // 포커스 된 아이콘 실행
                                    timeCounter.sendEmptyMessage(TIMER_STOP);
                                    Intent fi = pm.getLaunchIntentForPackage(focusedPName);
                                    if (focusedPName == "스팟메모") {
                                        focusedName = "스팟메모";
                                        fi = new Intent(this, SM_main.class);
                                    }
                                    tts.speak(focusedName + "을 실행합니다.", TextToSpeech.QUEUE_FLUSH, null);
                                    startActivity(fi);
                                }
                                clickCount = 0;
                            } else {
                                startTime = System.currentTimeMillis();
                                clickCount = 1;
                                if (view_mode == 5) {
                                    clickCount = 0; // ** 인자 초기화 의미 불명확함
                                    focusedNum = calculate_grid(sx, sy) - 1;
                                    focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
                                    focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
                                }
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d("touch","ACTION_POINTER_DOWN");
                clickCount = 0;
                swipe_mode = swipe;
                if (e.getPointerCount() == 2) {
                    focusedPName = null;
                    focusedName = null;
                    startY = e.getY(0);
                    startX = e.getX(0);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d("touch","ACTION_POINTER_UP");
                isThereTSwipe = true;
                clickCount = 0;
                if (e.getPointerCount() == 2) {
                    String alertmsg = "컨트롤러가 없습니다. 생성하려면 더블 탭";

                    // right to left swipe (오른쪽)
                    if (startX - stopX > SWIPE_MIN_DISTANCE && Math.abs(startX - stopX) > SWIPE_THRESHOLD_VELOCITY) {
                        if(view_mode == 4) {
                            if (controllerNum == 1) {
                                Toast.makeText(context, alertmsg, Toast.LENGTH_SHORT).show();
                                tts.speak(alertmsg, TextToSpeech.QUEUE_FLUSH, null);
                            } else {
                                currentController = (currentController + 1) % controllerNum;
                                if (currentController == 0) {
                                    v.vibrate(cpattern3, -1);
                                }
                            }
                            int forspeak = currentController + 1;
                            if (forspeak == 1) {
                                tts.speak("첫번째", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (forspeak == 2) {
                                tts.speak("두번째", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (forspeak == 3) {
                                tts.speak("세번째", TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                        if(view_mode == 5) {
                            if(gridIndex +9 < gridSetting.getLimit(context)) {
                                if(gridIndex == 0) {
                                    gridIndex = gridIndex + 8;
                                }else {
                                    gridIndex = gridIndex + 9;
                                }
                                gridSetting.setGrid(this, gridIndex, GridList);
                                focusedNum = 0;
                                focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
                                focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
                                tts.speak("다음 페이지로 이동했습니다.", TextToSpeech.QUEUE_ADD, null);
                                tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                v.vibrate(cpattern3, -1);
                            }else {
                                Toast.makeText(this, "마지막 페이지입니다.",Toast.LENGTH_SHORT).show();
                                tts.speak("마지막 페이지입니다.", TextToSpeech.QUEUE_ADD, null);
                                tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                v.vibrate(cpattern3, -1);
                            }
                        }
                    }
                    // left to right swipe (왼쪽)
                    else if (stopX - startX > SWIPE_MIN_DISTANCE && Math.abs(stopX - startX) > SWIPE_THRESHOLD_VELOCITY) {
                        if(view_mode == 4) {
                            if (controllerNum == 1) {
                                Toast.makeText(context, alertmsg, Toast.LENGTH_SHORT).show();
                                tts.speak(alertmsg, TextToSpeech.QUEUE_FLUSH, null);
                            } else {
                                if (currentController == 0) {
                                    // 처음과 끝을 이어보아요
                                    currentController = controllerNum - 1;
                                } else {
                                    currentController = (currentController - 1) % controllerNum;
                                    if (currentController == 0) {
                                        v.vibrate(cpattern3, -1);
                                    }
                                }
                            }
                            int forspeak = currentController + 1;
                            if (forspeak == 1) {
                                tts.speak("첫번째", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (forspeak == 2) {
                                tts.speak("두번째", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (forspeak == 3) {
                                tts.speak("세번째", TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                        if(view_mode == 5) {
                            if(gridIndex - 8 >= 0) {
                                if(gridIndex == 8) {
                                    gridIndex = 0;
                                }else {
                                    gridIndex = gridIndex - 9;
                                }
                                gridSetting.setGrid(this, gridIndex, GridList);
                                focusedNum = 0;
                                focusedName = gridSetting.getGridIconName(gridIndex + focusedNum, focusedNum, true);
                                focusedPName = gridSetting.getGridIconPName(gridIndex + focusedNum, gridIndex);
                                tts.speak("다음 페이지로 이동했습니다.", TextToSpeech.QUEUE_ADD, null);
                                tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                v.vibrate(cpattern3, -1);
                            }else {
                                Toast.makeText(this, "첫 번째 페이지입니다.",Toast.LENGTH_SHORT).show();
                                tts.speak("첫 번째 페이지입니다.", TextToSpeech.QUEUE_ADD, null);
                                tts.speak(focusedName, TextToSpeech.QUEUE_ADD, null);
                                v.vibrate(cpattern3, -1);
                            }
                        }
                    }
                    // down to up swipe
                    else if (startY - stopY > SWIPE_MIN_DISTANCE && Math.abs(startY - stopY) > SWIPE_THRESHOLD_VELOCITY) {
                        // 설정 메뉴 진입!!!!!
                        String settinmsg = "설정 메뉴로 진입합니다.";
                        Toast.makeText(context, settinmsg, Toast.LENGTH_SHORT).show();
                        tts.speak(settinmsg, TextToSpeech.QUEUE_FLUSH, null);
                        Intent intent = new Intent(context, SettingActivity.class);
                        startActivityForResult(intent, REQUEST_CHANGE);
                        //finish();
                    }
                    // up to down swipe
                    else if (stopY - startY > SWIPE_MIN_DISTANCE && Math.abs(stopY - startY) > SWIPE_THRESHOLD_VELOCITY) {
                        // 간단 알람 기능
                        String makeAlarm = "알람 만들기 진입합니다.";
                        Toast.makeText(context, "Make Alarm", Toast.LENGTH_SHORT).show();
                        tts.speak(makeAlarm, TextToSpeech.QUEUE_ADD, null);
                        Intent intent = new Intent(context, MakeAlarm.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("touch","ACTION_MOVE");
                if (e.getPointerCount() == 2) {
                    stopY = e.getY(0);
                    stopX = e.getX(0);
                } else {
                    tx = e.getX();
                    ty = e.getY();
                    if(view_mode == 4) {
                        calculate_theta(e);
                    }
                }
                break;
        }

        return super.onTouchEvent(e);
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
            appImage.setImageDrawable(controllers[currentController].getIconImage(1));
            tts.speak(controllers[currentController].icon1Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
        if (theta <= 120 && theta >= 120 - 5) {
            //if(controllers[currentController].icon2 != null) {

            v.vibrate(150);
            controllers[currentController].flaggingIcon(1);
            appImage.setImageDrawable(controllers[currentController].getIconImage(2));
            tts.speak(controllers[currentController].icon2Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
        // 세번째 앱
        if (theta <= 180 && theta >= 180 - 5) {
            //if(controllers[currentController].icon3 != null) {
            v.vibrate(150);
            controllers[currentController].flaggingIcon(2);
            appImage.setImageDrawable(controllers[currentController].getIconImage(3));
            tts.speak(controllers[currentController].icon3Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
        // 네번째 앱
        if (theta <= -120 && theta >= -120 - 5) {
            //if(controllers[currentController].icon4 != null) {
            v.vibrate(150);
            controllers[currentController].flaggingIcon(3);
            appImage.setImageDrawable(controllers[currentController].getIconImage(4));
            tts.speak(controllers[currentController].icon4Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
        // 다섯번째 앱
        if (theta <= -60 && theta >= -60 - 5) {
            //if(controllers[currentController].icon5 != null) {
            v.vibrate(150);
            controllers[currentController].flaggingIcon(4);
            appImage.setImageDrawable(controllers[currentController].getIconImage(5));
            tts.speak(controllers[currentController].icon5Name,TextToSpeech.QUEUE_FLUSH, null);
            //}
        }
    }

    @Override
    public void onInit(int i) {

    }

    public int calculate_grid(double x, double y) {
        double width = context.getResources().getDisplayMetrics().widthPixels - 32;
        double height = context.getResources().getDisplayMetrics().heightPixels - 32;

        if(x >= 16 && x <= width/3) {
            //first column
            if(y >= 16 && y <= height/3) {
                return 1;
            }else if(y >= height/3 && y <= (height/3)*2) {
                return 4;
            }else if(y >= (height/3)*2 && y <= height) {
                return 7;
            }
        }else if(x >= width/3 && x <= (width/3)*2) {
            // second column
            if(y >= 16 && y <= height/3) {
                return 2;
            }else if(y >= height/3 && y <= (height/3)*2) {
                return 5;
            }else if(y >= (height/3)*2 && y <= height) {
                return 8;
            }
        }else if(x >= (width/3)*2 && x <= width) {
            // third column
            if(y >= 16 && y <= height/3) {
                return 3;
            }else if(y >= height/3 && y <= (height/3)*2) {
                return 6;
            }else if(y >= (height/3)*2 && y <= height) {
                return 9;
            }
        }

        return 0;
    }


    //[자동 롤링]에 쓰는 타이머 클래스.
    class TimeCounter extends Handler {
        int count_;

        TimeCounter(int count){
            count_ = count;
        }

        TimeCounter(){
            count_ = 0;
        }

        public void changeFocusedNum(int num){
            count_ = num;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TIMER_START:
                    this.removeMessages(TIMER_REPEAT);
                    Log.d("timerhandler", "timer start!");
                    this.sendEmptyMessageDelayed(TIMER_REPEAT,TIME_DElAY);
                    break;
                case TIMER_REPEAT:
                    count_++;
                    //temp
                    Log.d("timehandler","Time count: "+count_);
                    onTimeWork(count_);
                    this.sendEmptyMessageDelayed(TIMER_REPEAT,TIME_DElAY);
                    break;
                case TIMER_STOP:
                    Log.d("timehandler","timer stop!!!");
                    this.removeMessages(TIMER_REPEAT);
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
