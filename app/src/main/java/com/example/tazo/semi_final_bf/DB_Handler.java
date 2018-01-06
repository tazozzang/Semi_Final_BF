package com.example.tazo.semi_final_bf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Tazo on 2017-04-26.
 */

public class DB_Handler extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "BF_DB.db";
    public static final String DATABASE_TABLE = "controllers";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONTROLLER_NUM = "cnum";
    public static final String COLUMN_ICON_NUM = "inum";
    public static final String COLUMN_PACKAGE_NAME = "packageName";

    public DB_Handler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. DB 생성
        String CREATE_TABLE = "create table if not exists "
                +DATABASE_TABLE
                +"("+ COLUMN_ID+" integer primary key autoincrement,"
                +COLUMN_CONTROLLER_NUM + " integer,"
                +COLUMN_ICON_NUM + " integer,"
                +COLUMN_PACKAGE_NAME+" text)";
        db.execSQL(CREATE_TABLE);

        addIcon(1,1,"spotmemo");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+DATABASE_TABLE);
        onCreate(db);
    }

    public static DB_Handler open(Context ctx) throws SQLException {
        DB_Handler handler = new DB_Handler(ctx,null,null,1);
        // 위의 객체 생성후 db 와 TABLE 이 생성

        return handler;
    }

    public long addIcon(int cnum, int num, String pName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTROLLER_NUM, cnum);
        values.put(COLUMN_ICON_NUM, num);
        values.put(COLUMN_PACKAGE_NAME, pName);
        SQLiteDatabase db= this.getWritableDatabase();
        long result = db.insert(DATABASE_TABLE, null, values);
        db.close();
        return result;
    }

    public boolean deleteIcon(int cnum, int inum) {
        boolean result = false;
        String query = "select * from "+DATABASE_NAME+" where "+COLUMN_CONTROLLER_NUM+"= "+cnum+" and "+COLUMN_ICON_NUM+"= "+inum;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            String id = cursor.getString(0);
            db.delete(DATABASE_TABLE, COLUMN_ID+"=?", new String[]{id});
            cursor.close();
            db.close();
            return true;
        }
        db.close();
        return result;
    }

    public DB_Controller findIcon(int cnum, int inum) {
        DB_Controller controller = new DB_Controller();
        if(cnum > 0 && inum > 0) {
            String query = "select * from " + DATABASE_TABLE + " where " + COLUMN_CONTROLLER_NUM + " = " + cnum + " and " + COLUMN_ICON_NUM + " = " + inum;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                controller.setId(cursor.getInt(0));
                controller.setCnum(cursor.getInt(1));
                controller.setInum(cursor.getInt(2));
                controller.setPname(cursor.getString(3));
                cursor.close();
                db.close();
            }
        }
        return controller;
    }

    public boolean updateIcon(int cnum, int inum, String pname) {
        DB_Controller controller = findIcon(cnum, inum);
        controller.setPname(pname);
        if(controller != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_CONTROLLER_NUM, controller.getCnum());
            values.put(COLUMN_ICON_NUM, controller.getInum());
            values.put(COLUMN_PACKAGE_NAME, controller.getPname());

            int result = db.update(DATABASE_TABLE, values, COLUMN_ID+"=\'"+controller.getId()+"\'", null);
            db.close();
            if(result != -1) {
                Log.d("update",controller.getCnum()+", "+controller.getInum()+", "+controller.getPname());
                return true;
            }else {
                return false;
            }
        }
        return  false;
    }

    public int howManyController() {

        String query2 = "select * from " + DATABASE_TABLE + " where " + COLUMN_CONTROLLER_NUM + " = " + 2;
        String query3 = "select * from " + DATABASE_TABLE + " where "+COLUMN_CONTROLLER_NUM+" = "+3;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query3, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return 3;
        }
        else {
            cursor = db.rawQuery(query2, null);
            if(cursor.moveToFirst()) {
                cursor.close();
                db.close();
                return 2;
            }
        }
        return 1;
    }

}
