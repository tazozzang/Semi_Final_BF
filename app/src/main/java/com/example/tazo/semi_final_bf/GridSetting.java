package com.example.tazo.semi_final_bf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miran lee on 2018-07-26.
 */

public class GridSetting {

    PackageManager pm;
    List<ResolveInfo> list;
    ArrayList<String> applist; // package name 저장
    List<View> ViewList;

    int limit = 0;
    int gridLimit = 9;

    public int getAppListLimit(Context c) {
        pm = c.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        list = pm.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        applist = new ArrayList<>();

        return list.size();
    }

    public int getLimit(Context c) {
        if(limit == 0) {
            limit = getAppListLimit(c);
        }

        return limit;
    }

    public void getAppList(Context c, int startIndex) {
        limit = getAppListLimit(c);

        if (limit > startIndex + 9) {
            gridLimit = 9;
        } else {
            gridLimit = limit - startIndex;
        }

        if(startIndex == 0) {
            applist.add("스팟메모");
            gridLimit = 8;
        }

        for (int i = startIndex; i < startIndex + gridLimit; i++) {
            ResolveInfo resolveInfo = list.get(i);
            String pName = resolveInfo.activityInfo.applicationInfo.packageName;
            applist.add(pName);
        }

        if(startIndex == 0) {
            gridLimit = 9;
        }
    }

    public void setGrid(Context c, int startIndex, List<View> List) {
        getAppList(c, startIndex);

        ViewList = List;

        if(startIndex == 0) {
            ImageView iv = (ImageView)List.get(0);
            iv.setImageResource(R.drawable.spotmemo);
            for(int i = 1; i < gridLimit; i++) {
                ImageView ivv = (ImageView)List.get(i);
                try {
                    Drawable d = pm.getApplicationIcon(applist.get(i));
                    ivv.setImageDrawable(d);

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }else {
            for(int i = 0; i < gridLimit; i++) {
                ImageView iv = (ImageView)List.get(i);
                try {
                    Drawable d = pm.getApplicationIcon(applist.get(i));
                    iv.setImageDrawable(d);

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            for(int j = gridLimit; j < 9; j++) { // 아이콘이 없으면 null로 초기화
                ImageView iv = (ImageView)List.get(j);
                iv.setImageDrawable(null);
                applist.add(null);
            }
        }
    }

    public String getGridIconName(int iconNum, int viewNum, boolean isFocused) {
        String iconName;
        if(iconNum == 0) {
            iconName = "스팟메모";
        }else {
            if(iconNum - viewNum == 0) {
                ResolveInfo resolveInfo = list.get(iconNum-1);
                iconName = resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
            }else {
                ResolveInfo resolveInfo = list.get(iconNum);
                iconName = resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString();
            }
        }

        if (isFocused) {
            for (int i = 0; i < gridLimit; i++) {
                ImageView ivv = (ImageView) ViewList.get(i);
                ivv.setBackgroundResource(R.drawable.noborder);
            }
            ImageView iv = (ImageView) ViewList.get(viewNum);
            iv.setBackgroundResource(R.drawable.border);
        }

        return iconName;
    }

    public String getGridIconPName(int iconNum, int startIndex) {
        String pName;
        if(iconNum == 0) {
            pName = "스팟메모";
        }else {
            if(startIndex == 0) {
                ResolveInfo resolveInfo = list.get(iconNum-1);
                pName = resolveInfo.activityInfo.applicationInfo.packageName;
            }else {
                ResolveInfo resolveInfo = list.get(iconNum);
                pName = resolveInfo.activityInfo.applicationInfo.packageName;
            }
        }

        return pName;
    }
}
