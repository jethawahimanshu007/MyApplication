package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Himanshu on 9/7/2016.
 */
public class DbFunctions {

    public String getTags(SQLiteDatabase mydatabase) {

        Cursor cursorForTagsForLocalDevice=null;
        Log.d("DbFuncs","Went into getTags function");
        String tagsForLocalDevice=new String();
        try {
            Log.d("DbFuncs","Entered try 1 in DbFunctions getTags");
            if(mydatabase==null)
            {
                Log.d("DbFunctions","mydatabase is null");
            }
            cursorForTagsForLocalDevice = mydatabase.rawQuery("SELECT GROUP_CONCAT(Tags) from IMAGE_TAG_RELATION", null);
            if(cursorForTagsForLocalDevice==null)
                Log.d("DbFuncGetTags","cursorForTagsForLocalDevice isnull");
        } catch (Exception e) {
            Log.d("ConnectThread", "Exception occurred, hahahaha!::"+e);
        }
        try {
            tagsForLocalDevice = new String();
            Log.d("DbFunctions","tagsForLocalDevice 1  is::"+tagsForLocalDevice);
            try {
                Log.d("DbFunctions","tagsForLocalDevice 2 is::"+tagsForLocalDevice);
                if (cursorForTagsForLocalDevice != null)
                    while (cursorForTagsForLocalDevice.moveToNext()) {
                        tagsForLocalDevice = cursorForTagsForLocalDevice.getString(0);
                    }
                Log.d("DbFunctions","tagsForLocalDevice 3 is::"+tagsForLocalDevice);
            } catch (Exception e) {
                Log.d("ConnectThread", "Exception occured in sqlite query!!! ::" + e);
            }
        }
        catch(Exception e)
        {
            Log.d("DbFuncGetTags","Exception occured:"+e);
        }
        Log.d("DbFuncts","Returning hurt");

        return tagsForLocalDevice;
    }

    public static String compareTags(SQLiteDatabase mydatabase)
    {
        String matchingTags=new String();

        return matchingTags;
    }
    public void insertIntoDeviceTagTbl(SQLiteDatabase mydatabase,String deviceName,String deviceAddr,String tags) {
        try {
            String sqlStatement = "INSERT INTO DEVICE_TAG values('" + deviceName + "'," + "'" + deviceAddr + "'," + "'" + tags + "'" + ")";
            mydatabase.execSQL(sqlStatement);
        }
        catch(Exception e)
        {
            Log.d("DbFunctions","Exception occured in insertIntoDeviceTag:"+e);
        }
    }
    public String getTagsForDevice(SQLiteDatabase mydatabase,String deviceMacAddr)
    {
        String tagsForDevice="";
        Cursor cursorForTagsForLocalDevice=null;
        String sqlStatement="SELECT tags from DEVICE_TAG where deviceAddr='"+deviceMacAddr+"'";
        try {
            cursorForTagsForLocalDevice = mydatabase.rawQuery(sqlStatement, null);
            if (cursorForTagsForLocalDevice != null) {
                cursorForTagsForLocalDevice.moveToFirst();
                tagsForDevice = cursorForTagsForLocalDevice.getString(0);

            }
        }
        catch(Exception e)
        {
            Log.d("DbFuncs","Exception in dbFuncs getTagsFromDEvice:"+e);
        }
        return tagsForDevice;
    }

    public String getImagePaths(SQLiteDatabase mydatabase,String tags)
    {
        String imagePaths="";
        Cursor cursorForTagsForLocalDevice=null;
        String sqlStatement="SELECT GROUP_CONCAT(PicturePath) from IMAGE_TAG_RELATION where tags='"+tags+"'";
        try {
            cursorForTagsForLocalDevice = mydatabase.rawQuery(sqlStatement, null);
            if (cursorForTagsForLocalDevice != null) {
                cursorForTagsForLocalDevice.moveToFirst();
                imagePaths = cursorForTagsForLocalDevice.getString(0);

            }
        }
        catch(Exception e)
        {
            Log.d("DbFuncs","Exception in dbFuncs getTagsFromDEvice:"+e);
        }
        return imagePaths;
    }
    public void sendImageTbl(SQLiteDatabase mydatabase,String picturePath,String deviceMacAddr)
    {
        String sqlStatement="";
        sqlStatement="INSERT INTO SEND_IMAGE_TBL('"+deviceMacAddr+"','"+picturePath+"',0)";
        mydatabase.execSQL(sqlStatement);
        Log.d("DbFunctions","SEndImageTbl function--Hopefully SEND_IMAGE_TBL written");
    }

}
