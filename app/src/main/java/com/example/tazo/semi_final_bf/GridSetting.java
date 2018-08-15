package com.example.tazo.semi_final_bf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    ArrayList<String> applist;

    public void getAppList(Context c, int startIndex) {
        pm = c.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        list = pm.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        applist = new ArrayList<>();

        for(int i = startIndex ; i < startIndex+9 ; i++) {
            ResolveInfo resolveInfo = list.get(i);
            String pName = resolveInfo.activityInfo.applicationInfo.packageName;
            applist.add(pName);
        }
    }

    public void setGrid(Context c, int startIndex, List<View> viewList) {
        getAppList(c, startIndex);

        for(int i = 0; i < 9; i++) {
            Drawable d = new Drawable() {
                @Override
                public void draw(@NonNull Canvas canvas) {

                }

                @Override
                public void setAlpha(int alpha) {

                }

                @Override
                public void setColorFilter(@Nullable ColorFilter colorFilter) {

                }

                @Override
                public int getOpacity() {
                    return PixelFormat.UNKNOWN;
                }
            };

            try {
                d = pm.getApplicationIcon(applist.get(i));

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            ImageView iv = (ImageView)viewList.get(i);
            iv.setImageDrawable(d);
        }

    }
}
