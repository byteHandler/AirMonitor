package nik.sociomind.airmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class database extends SQLiteOpenHelper {
    public static final String COL_1 = "PM25";
    public static final String COL_2 = "PM10";
    public static final String COL_3 = "MQ135";
    public static final String COL_4 = "MQ02";
    public static final String COL_5 = "TEMPERATURE";
    public static final String COL_6 = "HUMID";
    public static final String DATABASE_NAME = "Main.db";
    public static final String TABLE_NAME = "threshold_table";
    public database(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = getWritableDatabase();
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table threshold_table(ID INTEGER PRIMARY KEY AUTOINCREMENT, PM25 TEXT,PM10 TEXT,MQ135 TEXT,MQ02 TEXT,TEMPERATURE TEXT,HUMID TEXT)");
    }

    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS threshold_table");
        onCreate(sqLiteDatabase);
    }

    public boolean insertdata(String pm25,String pm10,String mq135,String mq02,String temp,String humid) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, pm25);
        contentValues.put(COL_2, pm10);
        contentValues.put(COL_3, mq135);
        contentValues.put(COL_4, mq02);
        contentValues.put(COL_5, temp);
        contentValues.put(COL_6, humid);
        if (db.insert(TABLE_NAME, null, contentValues) == -1) {
            return false;
        }
        return true;
    }

    public Cursor getAllData() {
        return getReadableDatabase().rawQuery("select * from threshold_table", null);
    }


    public boolean updatedata(String specifier , String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if(specifier.equals("pm25")){
                contentValues.put(COL_1, value);
                db.execSQL("UPDATE threshold_table set PM25=" + value);
            }

         if(specifier.equals("pm10")){
                contentValues.put(COL_2, value);
                db.execSQL("UPDATE threshold_table set PM10=" + value);
            }
             if(specifier.equals("mq135")){
                contentValues.put(COL_3, value);
                db.execSQL("UPDATE threshold_table set MQ135=" + value);
            }
             if(specifier.equals("mq02")){
                contentValues.put(COL_4, value);
                db.execSQL("UPDATE threshold_table set MQ02=" + value);
            }
            if(specifier.equals("temp")){
                contentValues.put(COL_5, value);
                db.execSQL("UPDATE threshold_table set TEMPERATURE=" + value);
            }
           if(specifier.equals("humid")){
                contentValues.put(COL_6, value);
                db.execSQL("UPDATE threshold_table set HUMID=" + value);
                Log.d(TAG, "updatedata: humidity" + return_names()[0] + " " + return_names()[1] + " " + return_names()[2] + " " + return_names()[3] + " " + return_names()[4] + " " + return_names()[5]);
            }

        return true;
    }
    public Integer deletedata(int id) {
        return Integer.valueOf(getWritableDatabase().delete(TABLE_NAME, "ID=?", new String[]{Integer.toString(id)}));
    }
    public String[] return_names() {

        Cursor mycursor = getAllData();
        mycursor.moveToFirst();
        String[] allnames = new String[6];
        if(!isempty()) {
            allnames[0] = mycursor.getString(1);
            allnames[1] = mycursor.getString(2);
            allnames[2] = mycursor.getString(3);
            allnames[3] = mycursor.getString(4);
            allnames[4] = mycursor.getString(5);
            allnames[5] = mycursor.getString(6);
            Log.d(TAG, "return_names:0 "+allnames[0]);
            Log.d(TAG, "return_names:1 "+allnames[1]);
            Log.d(TAG, "return_names:2 "+allnames[2]);
            Log.d(TAG, "return_names:3 "+allnames[3]);
            Log.d(TAG, "return_names: 4"+allnames[4]);
            Log.d(TAG, "return_names: 5"+allnames[5]);
        }
        else
        {
            allnames[0] = "1000";
            allnames[1] = "1000";
            allnames[2] = "1000";
            allnames[3] = "1000";
            allnames[4] = "1000";
            allnames[5] = "1000";
        }
        mycursor.close();
        return allnames;
    }




    public int getID(String name) {
        Cursor locator = getAllData();
        if (locator.getCount() == 0) {
            return -1;
        }
        locator.moveToFirst();
        if (locator.getString(1).equals(name)) {
            return Integer.parseInt(locator.getString(0));
        }
        while (locator.moveToNext()) {
            if (locator.getString(1).equals(name)) {
                return Integer.parseInt(locator.getString(0));
            }
        }
        return -1;
    }


    public boolean isempty() {
        if (getAllData().getCount() == 0) {
            return true;
        }
        return false;
    }

    public int return_count() {
        Cursor locator = getAllData();
        int x = 0;
        while(locator.moveToNext())
            x += 1;
        return x;
    }






    public boolean list_contain(ArrayList<String> list, String string) {
        for (int i = 0; i < list.size(); i++) {
            if (((String) list.get(i)).equals(string)) {
                return true;
            }
        }
        return false;
    }

    public boolean is_name_present(String name) {
        Cursor db = getAllData();
        boolean x = false;
        while (db.moveToNext()) {
            if (db.getString(1).equals(name)) {
                x = true;
            }
        }
        return x;
    }
}
