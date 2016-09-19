package com.example.himanshu.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Himanshu on 8/25/2016.
 */
public class DbTableCreation {


    SQLiteDatabase mydatabase;
    public void createTables(Context context) {
        mydatabase = context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS DEVICE_TAG(deviceName VARCHAR, deviceAddr VARCHAR, tags VARCHAR,PRIMARY KEY(deviceName,deviceAddr))");
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS IMAGE_TAG_RELATION(PicturePath VARCHAR,Tags VARCHAR,PRIMARY KEY(PicturePath));");
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS BLUETOOTH_DEVICES_IN_RANGE(BTDeviceMacAddr VARCHAR,BTDeviceName VARCHAR,PRIMARY KEY(BTDeviceMacAddr))");
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS SEND_IMAGE_TBL(BTDeviceMacAddr VARCHAR,PicturePath VARCHAR,flagForSent INTEGER,PRIMARY KEY(BTDeviceMacAddr,PicturePath))");

    }
    }
