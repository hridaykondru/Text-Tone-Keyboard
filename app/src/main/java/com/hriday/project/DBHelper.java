
package com.hriday.project;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserData.db";
    private static final String TABLE_NAME = "User_Data";
    // private static final String KEY_SLNO = "slno";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        String createTable = "create table " + TABLE_NAME + "(Slno INTEGER PRIMARY KEY,Datatype TEXT, Operation TEXT, Textdata TEXT, Audiodata BLOB,App TEXT,Time TEXT )";
        //  String createTable = "CREATE TABLE " + TABLE_NAME + "("
        //        + KEY_SLNO + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_APP + " TEXT,"
        //      + KEY_DATA + " TEXT" + ")";
        Log.i("Third", "3");
        DB.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists " + TABLE_NAME);
        onCreate(DB);
    }

    public Boolean insertuserdata(com.hriday.project.DataTyped typed) {

        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // contentValues.put(KEY_SLNO, typed.getID());
        // contentValues.put("appname", typed.getName());

        if(typed.getName()=="Shared")
            contentValues.put("Datatype", "Image");
        else
            contentValues.put("Datatype", "Text");
        contentValues.put("Operation", typed.getName());
        contentValues.put("Textdata", typed.getData());
        contentValues.put("App", typed.getAppname());
        contentValues.put("Time", mydate);
        //contentValues.put("picture", (byte[]) null);

        long result = DB.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public void insertaudiodata(File file1, String appname, String operation) throws IOException {

        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        FileInputStream instream = new FileInputStream(file1);
        BufferedInputStream bif = new BufferedInputStream(instream);
        byte[] byteImage1 = null;
        byteImage1 = new byte[bif.available()];
        bif.read(byteImage1);

        if(operation=="IShared")
            contentValues.put("Datatype", "Image");
        else
            contentValues.put("Datatype","Audio");
        //contentValues.put("data", (byte[]) null);
        contentValues.put("Operation", operation);
        contentValues.put("Audiodata", byteImage1);
        contentValues.put("App", appname);
        contentValues.put("Time", mydate);


        long result = DB.insert(TABLE_NAME, null, contentValues);

    }

    public List<com.hriday.project.DataTyped> getAllData() {
        List<com.hriday.project.DataTyped> DataList = new ArrayList<com.hriday.project.DataTyped>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);


        if (cursor.moveToFirst()) {
            do {
                com.hriday.project.DataTyped data = new com.hriday.project.DataTyped();
                data.setID(Integer.parseInt(cursor.getString(0)));
                //contact.setName(cursor.getString(1));
                data.setData(cursor.getString(1));
                // Adding contact to list
                DataList.add(data);
            } while (cursor.moveToNext());
        }


        return DataList;
    }


    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"message"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }
    }


    public byte[] getCell(int row) {

        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor c = sqlDB.rawQuery(query, null);
        byte[] blob2;
        int row2 = 0;
        if (c.moveToFirst()) {
            do {
                blob2 = c.getBlob(4);
                if (row2 == row) {
                    return blob2;
                }
                ++row2;
            } while (c.moveToNext());
        }

        return null;

    }

}