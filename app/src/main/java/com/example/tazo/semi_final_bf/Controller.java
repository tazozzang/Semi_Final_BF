package com.example.tazo.semi_final_bf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;

/**
 * Created by Tazo on 2017-04-26.
 */

public class Controller {
    // ** 컨트롤러 클래스 **
    // : Wheel Controller를 생성하고 관리합니다.

    // 1. 사용자가 터치한 좌표를 통해 진동 발생 위치를 결정합니다.
    double startPointX, startPointY; // 사용자가 처음으로 터치한 좌표
    double currentPointX, currentPointY; // 사용자가 현재 터지하고 있는 좌표
    double centerX, centerY; // 컨트롤러 중심점

    boolean first = true; // startPoint를 저장하기 위해 사용


    // 2. 설정된 아이콘을 저장하고 출력(실행)합니다.
    boolean pressed = false; // 사용자가 터치하고 있는지 확인
    boolean bicon1, bicon2, bicon3, bicon4, bicon5; // 출력할 아이콘을 결정 (다음으로 선택될 아이콘)
    String icon1;
    String icon2;
    String icon3;
    String icon4;
    String icon5;

    String icon1Name;
    String icon2Name;
    String icon3Name;
    String icon4Name;
    String icon5Name;

    int controllernum;

    Context context;
    PackageManager pm;

    Handler longClickHandler = new Handler();
    TextToSpeech tts;
    static int LONG_PRESS_TIME = 500;
    Runnable longClickRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                String name = pm.getApplicationLabel(pm.getApplicationInfo(icon1, PackageManager.GET_META_DATA)).toString();
                tts.speak(name, TextToSpeech.QUEUE_FLUSH, null);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }; // 이 Handler는 아이콘의 이름을 빠르게 출력하기 위해서 추가했습니다.

    public Controller() {}

    public Controller(Context context, int num) {
        // 생성자에서는 사용자의 디바이스 정보에 따라 화면의 중심을 중심점에 저장하고 context를 저장합니다

        this.context = context;
        centerX = context.getResources().getDisplayMetrics().widthPixels / 2;
        centerY = context.getResources().getDisplayMetrics().heightPixels / 2;
        pm = context.getPackageManager();
        controllernum = num;

        icon1 = setIcon(1);
        icon2 = setIcon(2);
        icon3 = setIcon(3);
        icon4 = setIcon(4);
        icon5 = setIcon(5);

        icon1Name = getIconName(1);
        icon2Name = getIconName(2);
        icon3Name= getIconName(3);
        icon4Name = getIconName(4);
        icon5Name = getIconName(5);
    }

    public void flaggingIcon(int pnum) {
        // 현재 선택된 아이콘의 번호.
        // 다음 아이콘만 선택되도록 제어해준다

        if (pnum == 0) {
            bicon1 = true;
            bicon2 = false;
            bicon3 = false;
            bicon4 = false;
            bicon5 = false;
        }
        if (pnum == 1) {
            bicon1 = false;
            bicon2 = true;
            bicon3 = false;
            bicon4 = false;
            bicon5 = false;
        }
        if (pnum == 2) {
            bicon1 = false;
            bicon2 = false;
            bicon3 = true;
            bicon4 = false;
            bicon5 = false;
        }
        if (pnum == 3) {
            bicon1 = false;
            bicon2 = false;
            bicon3 = false;
            bicon4 = true;
            bicon5 = false;
        }
        if (pnum == 4) {
            bicon1 = false;
            bicon2 = false;
            bicon3 = false;
            bicon4 = false;
            bicon5 = true;
        }
        if(pnum == 5) {
            // Swipe 동작이랑 아이콘 실행이랑 겹치지 않게 하려고!
            bicon1 = false;
            bicon2 = false;
            bicon3 = false;
            bicon4 = false;
            bicon5 = false;
        }
    }

    public void actionDown(MotionEvent e) {
        pressed =true;
        if(first == true) {
            flaggingIcon(0);
            startPointX = e.getX();
            startPointY = e.getY();
            first = false;

            icon1Name = getIconName(1);
            icon2Name = getIconName(2);
            icon3Name = getIconName(3);
            icon4Name = getIconName(4);
            icon5Name = getIconName(5);
        }
        longClickHandler.postDelayed(longClickRunnable, LONG_PRESS_TIME);
    }

    public void actionUp() {
        pressed = false;
        first = true;
        iconExecute();
        longClickHandler.removeCallbacks(longClickRunnable);
    }

    public void iconExecute() {
        Intent intent;
        DB_Handler db_handler = new DB_Handler(context, null, null, 1);
        if(bicon1) {
            if(controllernum == 1) {
                Intent intent1 = new Intent(context, SM_main.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);

                // !--- 여기에 스팟메모 실행 코드 추가 ---
            }else {
                String n = db_handler.findIcon(controllernum, 1).getPname();
                intent = pm.getLaunchIntentForPackage(db_handler.findIcon(controllernum, 1).getPname());
                if (null != intent && n != null) {
                    context.startActivity(intent);
                }
            }
        }
        if(bicon2) {
            String n = db_handler.findIcon(controllernum,2).getPname();
            intent = pm.getLaunchIntentForPackage(db_handler.findIcon(controllernum,2).getPname());
            if (null != intent && n != null) {
                context.startActivity(intent);
            }
        }
        if(bicon3) {
            String n = db_handler.findIcon(controllernum,3).getPname();
            intent = pm.getLaunchIntentForPackage(db_handler.findIcon(controllernum,3).getPname());
            if (null != intent && n != null) {
                context.startActivity(intent);
            }
        }
        if(bicon4) {
            String n = db_handler.findIcon(controllernum,4).getPname();
            intent = pm.getLaunchIntentForPackage(db_handler.findIcon(controllernum,4).getPname());
            if (null != intent && n != null) {
                context.startActivity(intent);
            }
        }
        if(bicon5) {
            String n = db_handler.findIcon(controllernum,5).getPname();
            intent = pm.getLaunchIntentForPackage(db_handler.findIcon(controllernum,5).getPname());
            if (null != intent && n != null) {
                context.startActivity(intent);
            }
        }
    }

    String setIcon(int inum) {
        DB_Handler db_handler = new DB_Handler(context, null, null, 1);
        DB_Controller db_controller = db_handler.findIcon(controllernum, inum);
        if(db_controller != null) {
            return db_controller.getPname();
        }else {
            return null;
        }
    }

    String getIconName(int inum) {
        if(controllernum == 1 && inum == 1) {
            return "스팟메모";
        }else {
            DB_Handler db_handler = new DB_Handler(context, null, null, 1);
            DB_Controller db_Controller = db_handler.findIcon(controllernum, inum);
            if (db_Controller != null) {
                try {
                    String name
                            = (String) context.getPackageManager()
                            .getApplicationLabel(context.getPackageManager()
                                    .getApplicationInfo(db_Controller.getPname(), PackageManager.GET_UNINSTALLED_PACKAGES));
                    return name;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    Drawable getIconImage(int inum) {
        String name = getIconName(inum);

        if(controllernum == 1 && inum == 1) {
            // 스팟메모 이미지 리턴
            Drawable drawable = context.getResources().getDrawable(R.drawable.spotmemo);
            return drawable;
        }

        switch (inum) {
            case 1:
                name = icon1;
                break;
            case 2:
                name = icon2;
                break;
            case 3:
                name = icon3;
                break;
            case 4:
                name = icon4;
                break;
            case 5:
                name= icon5;
                break;
        }

        try {
            return pm.getApplicationIcon(name);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
