package com.example.tazo.semi_final_bf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miran lee on 2018-07-26.
 */

public class GridSetting {

    PackageManager pm;
    List<ResolveInfo> list;
    ArrayList<String> applist;
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

        if(limit > startIndex+9) {
            gridLimit = 9;
        }else {
            gridLimit = limit - startIndex;
        }

        for(int i = startIndex ; i < startIndex + gridLimit ; i++) {
            ResolveInfo resolveInfo = list.get(i);
            String pName = resolveInfo.activityInfo.applicationInfo.packageName;
            applist.add(pName);
        }
    }

    public void setGrid(Context c, int startIndex, List<View> List) {
        getAppList(c, startIndex);

        ViewList = List;

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

    public String getGridIconName(int iconNum, int viewNum, boolean isFocused) {
        ResolveInfo resolveInfo = list.get(iconNum);
        String iconName = resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString();

        if(isFocused) {
            for(int i = 0; i < gridLimit; i++) {
                ImageView ivv = (ImageView)ViewList.get(i);
                ivv.setBackgroundResource(R.drawable.noborder);
            }
            ImageView iv = (ImageView)ViewList.get(viewNum);
            iv.setBackgroundResource(R.drawable.border);
        }

        return iconName;
    }

    public String getGridIconPName(int iconNum) {
        ResolveInfo resolveInfo = list.get(iconNum);
        String pName = resolveInfo.activityInfo.applicationInfo.packageName;

        return pName;
    }
}
