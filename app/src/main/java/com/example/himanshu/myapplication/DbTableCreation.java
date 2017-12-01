package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.UUID;

/**
 * Created by Himanshu on 8/25/2016.
 */

//This file consists of all the database table creation table statements
public class DbTableCreation {

    public void createTables(Context context) {
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS DEVICE_TAG(deviceName VARCHAR, deviceAddr VARCHAR, tags VARCHAR,PRIMARY KEY(deviceName,deviceAddr,tags))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS DEVICE_IMAGE_RECEIPT(sourceDeviceName VARCHAR, sourceDeviceAddr VARCHAR, tags VARCHAR,latitude VARCHAR ,longitude VARCHAR, timestamp VARCHAR,destDeviceName VARCHAR,destDeviceAddr VARCHAR,mime VARCHAR, format VARCHAR,fileName VARCHAR,UUID varchar)");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS IMAGE_TAG_RELATION(PicturePath VARCHAR,Tags VARCHAR,Latitude REAL, Longitude REAL,Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(PicturePath));");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS BLUETOOTH_DEVICES_IN_RANGE(BTDeviceMacAddr VARCHAR,BTDeviceName VARCHAR,RSSI VARCHAR,deviceClass INTEGER,PRIMARY KEY(BTDeviceMacAddr))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS SEND_IMAGE_TBL(BTDeviceMacAddr VARCHAR,PicturePath VARCHAR,flagForSent INTEGER,PRIMARY KEY(BTDeviceMacAddr,PicturePath))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS DEVICES_CURRENTLY_CONNECTED(BTDeviceMacAddr VARCHAR,time DEFAULT (datetime('now','localtime')),PRIMARY KEY(BTDeviceMacAddr))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS TSR_TBL(belongs_to VARCHAR,SI varchar,time DATE DEFAULT (datetime('now','localtime')), weight real,updated_by VARCHAR, PRIMARY KEY(SI))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS DEVICE_DISCONNECTED_TIME(deviceMacAddr VARCHAR, deviceName VARCHAR,currentTime DATE DEFAULT (datetime('now','localtime')))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS TSR_REMOTE_TBL(deviceMacAddr VARCHAR, SI VARCHAR, weight real, time DEFAULT (datetime('now','localtime')),belongs_to VARCHAR,PRIMARY KEY(deviceMacAddr,SI))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS UUID_TBL(UUID VARCHAR, isUsed INTEGER,MacAddress VARCHAR,PRIMARY KEY(UUID))");
        ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO DEVICE_IMAGE_RECEIPT values('Device1','AA:AA:AA:AA:AA:AA','someTagsHere','28.8','93.0','2016-10-25 20:51:00','Device2','BB:BB:BB:BB:BB:BB','image/jpeg','jpeg','/storage/emulated/0/testFile.jpg','MAC-testFile.jpg')");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS BT_ALWAYS_DIS(isDiscoverable int)");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS CONNECT_ALL_ATTEMPT_TBL(doneOrNot INTEGER,PRIMARY KEY(doneOrNot))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS ROGER_IP_ADD(IpAdd VARCHAR, PRIMARY KEY(IpAdd))");
        Cursor cursorForROgerIP=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from ROGER_IP_ADD",null);
        if(cursorForROgerIP.getCount()==0)
        ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO ROGER_IP_ADD VALUES('0.0.0.0')");
        ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO CONNECT_ALL_ATTEMPT_TBL VALUES(1)");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS SENT_IMAGE_LOG(UUID VARCHAR, sentTo VARCHAR, receivedFrom VARCHAR,PRIMARY KEY(UUID,sentTo,receivedFrom))");
        String UUIDs[]={"8ce255c0-200a-11e0-ac64-0800200c9a66","8ce255c0-200a-11e0-ac64-0800200c9a67","8ce255c0-200a-11e0-ac64-0800200c9a68","8ce255c0-200a-11e0-ac64-0800200c9a69","8ce255c0-200a-11e0-ac64-0800200c9a70","8ce255c0-200a-11e0-ac64-0800200c9a71","8ce255c0-200a-11e0-ac64-0800200c9a72"};
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS MESSAGE_TBL(imagePath VARCHAR, latitude REAL,longitude REAL,timestamp DATE,tagsForCurrentImage VARCHAR,fileName VARCHAR,mime VARCHAR,format VARCHAR,sourceMacAddr VARCHAR,sourceName VARCHAR,UUID VARCHAR,destMacAddr VARCHAR,destName VARCHAR, size INTEGER, quality INTEGER,priority INTEGER,PRIMARY KEY(imagePath,UUID))");
        Log.d("DbTableCreation","Creating ADDED_TAGS_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS ADDED_TAGS_TBL(UUID VARCHAR, addedTags VARCHAR, MacAdd VARCHAR,rating REAL,PRIMARY KEY(UUID,MacAdd))");
        Log.d("DbTableCreation","Created ADDED_TAGS_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS INCENT_FOR_MSG_TBL(UUID VARCHAR,promised double,received double,paid double,PRIMARY KEY(UUID))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS CONNECTED_LOG_TBL(MacAddr VARCHAR, doneOrNot INTEGER, PRIMARY KEY(MacAddr))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS INCENTIVES_TBL(incentive real)");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS ROLE_TBL(role INTEGER, MACAd VARCHAR,PRIMARY KEY(MACAd))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS MAC_RSSI_TBL(MacAd VARCHAR,RSSI VARCHAR)");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS INCENT_UUID_MAC_TBL(MacAd VARCHAR,UUID VARCHAR,incentive real,flag integer,PRIMARY KEY(MacAd,UUID))");// flag represents if the node is  dest or relay greater than threshold
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS TSR_SHARE_DONE_TBL(MacAd VARCHAR, doneOrNor INTEGER)");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS LAST_FIVE_TRANS(RemoteMAC VARCHAR, UUID VARCHAR, paid double, received double, time TIMESTAMP )");/*, time DEFAULT datetime('now','localtime')*/
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS RATINGS_TBL(UUID VARCHAR, rating REAL)");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS USER_RATING_MAP_TBL(MacAd VARCHAR,rating REAL,updated_by VARCHAR)");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS RATE_PARAMS_TBL(UUID varchar, rating REAL,confi REAL,qua REAL, PRIMARY KEY(UUID))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS WIFI_NODES_TBL(IpAddress VARCHAR, PRIMARY KEY(IpAddress))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS UUID_DEVICE_RATE_TBL(UUID VARCHAR,MacAdd VARCHAR,Rating REAL,PRIMARY KEY(UUID,MacAdd))");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS MAC_TSR_NO_TBL(MacAdd VARCHAR,noOfTsrs INTEGER,PRIMARY KEY(MacAdd))");
        ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO MAC_TSR_NO_TBL VALUES('SELF',1)");
        ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO ROLE_TBL VALUES(0,'SELF')");
        insertIncentives();
        insertUUIDs(ConstantsClass.mydatabaseLatest,UUIDs);

    }
    public void insertUUIDs(SQLiteDatabase mydatabase,String UUIDs[])
    {
        for(int i=0;i<UUIDs.length;i++)
        {
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO UUID_TBL(UUID,isUsed) values('"+UUIDs[i]+"',0)");
        }
    }
    //This function is not used
    public void dropTables(SQLiteDatabase mydatabaseLatest)
    {
        Log.d("DbTableCreation","Tables will be dropped!");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS DEVICE_TAG");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS DEVICE_IMAGE_RECEIPT");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS IMAGE_TAG_RELATION");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS BLUETOOTH_DEVICES_IN_RANGE");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS SEND_IMAGE_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS DEVICES_CURRENTLY_CONNECTED");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS TSR_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS DEVICE_DISCONNECTED_TIME");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS TSR_REMOTE_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS UUID_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS BT_ALWAYS_DIS");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS SENT_IMAGE_LOG");
        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS MESSAGE_TBL");
    }
    public void insertIncentives()
    {
        Log.d("DbTableCreation","Incentives being inserted!");
        Cursor cursorForInc=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from INCENTIVES_TBL",null);
        int i=0;
        while(cursorForInc.moveToNext())
        {
            i++;
            Log.d("DbTableCreation","cursorForInc.moveToNext is true and value of i is:"+i);
        }
        if(i>0)
        {
            Log.d("DbTableCreation","Incentives already there");
        }
        else
        {
            Log.d("DbTableCreation","Incentives not there, inserting!");
            ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO INCENTIVES_TBL values(300.0)");
        }
    }

    }
