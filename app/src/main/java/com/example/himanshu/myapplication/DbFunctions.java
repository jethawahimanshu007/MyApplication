package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothSocket;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
        String sqlStatement="SELECT GROUP_CONCAT(PicturePath|| '--' ||Latitude|| '--' ||Longitude|| '--' ||Timestamp|| '--' ||tags ) from IMAGE_TAG_RELATION where tags='"+tags+"'";
       // String sqlStatement="SELECT GROUP_CONCAT(PicturePath|| '--' ||Latitude|| '--' ||Longitude|| '--' ||Timestamp|| '--' ||tags ) from IMAGE_TAG_RELATION where tags='"+tags+"'";
        try {

            cursorForTagsForLocalDevice = mydatabase.rawQuery(sqlStatement, null);
            if (cursorForTagsForLocalDevice != null) {
                cursorForTagsForLocalDevice.moveToFirst();
                imagePaths = cursorForTagsForLocalDevice.getString(0);

            }
            Log.d("DbFunctions","The image paths are:"+imagePaths);
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
    public void insertIntoDeviceReceiptTbl(SQLiteDatabase mydatabase,String deviceName,String deviceMacAddr,String tags,String latitude, String longitude,String timestamp,String destDeviceName,String destDeviceAddr,String mime,String format,String fileName )
    {
        try {


            String sqlStatement = "";
            sqlStatement = "INSERT INTO DEVICE_IMAGE_RECEIPT values('" + deviceName + "','" + deviceMacAddr + "','"
                    + tags + "','" + latitude + "','" + longitude + "','" + timestamp +
                    "','" +destDeviceName+"','"+destDeviceAddr+"','"+mime+"','"+format+  "','"+fileName +"')";
            mydatabase.execSQL(sqlStatement);
            Log.d("dbfuncs","lat and long and tags are"+latitude+longitude+timestamp);
            Log.d("DbFunctions", "insertIntoDeviceReceiptTbl function--Hopefully DEVICE_IMAGE_RECEIPT written");
        }
        catch(Exception e)
        {
            Log.d("dbFunctions","Exception occured in inserting into DEVICE_IMAGE_RECEIPT"+e);
        }
    }
    public  String[] getReceivedImages(SQLiteDatabase mydatabase) {
        String[] someArray=null;
        Cursor cursorForImages=null;
        List<String> imagepaths = new ArrayList<String>();
        int numOfImages = 0;
        //////
        try {
            cursorForImages = mydatabase.rawQuery("SELECT fileName from DEVICE_IMAGE_RECEIPT ", null);
            if (cursorForImages == null)
                Log.d("DbFuncGetTags", "cursorForImages isnull");
        } catch (Exception e) {
            Log.d("DbFUncs", "Exception occurred, hahahaha!::" + e);
        }
        try {
            String image = new String();

            try {

                if (cursorForImages != null)
                    while (cursorForImages.moveToNext()) {
                        image = cursorForImages.getString(0);
                        imagepaths.add(image);
                        numOfImages++;
                    }
                if (numOfImages != 0) {
                    someArray = imagepaths.toArray(someArray);

                }


            } catch (Exception e) {
                Log.d("ConnectThread", "Exception occured in sqlite query!!! ::" + e);
            }


            //////
            return someArray;
        }
        catch(Exception e)
        {

        }
        return someArray;
    }


    public void insertIntoDevicesConnected(SQLiteDatabase mydatabase, String macAddr, String socket)
    {

        try {
            String sqlStatement = "INSERT INTO DEVICES_CURRENTLY_CONNECTED values('" + macAddr + "','" + socket + "')";
            mydatabase.execSQL(sqlStatement);
        }
        catch(Exception e)
        {
            Log.d("DbFunctions","Exception occured in insertIntoDevicesConnected:"+e);
        }
    }

    public int ifDeviceConnected(SQLiteDatabase mydatabase, String macAddr)
    {

        Cursor cursorForConnectedDevices = mydatabase.rawQuery("SELECT * from DEVICES_CURRENTLY_CONNECTED where BTDeviceMacAddr='"+macAddr+"'", null);
        if(cursorForConnectedDevices==null)
            return 0;
        else
        return 1;


    }
    public void deleteConnectedDevices(SQLiteDatabase mydatabase)
    {
        mydatabase.execSQL("DELETE from DEVICES_CURRENTLY_CONNECTED");
    }
    public void showConnectedDevices(SQLiteDatabase mydatabase)
    {
        Cursor cursorForConnectedDevices = mydatabase.rawQuery("SELECT * from DEVICES_CURRENTLY_CONNECTED", null);
        if (cursorForConnectedDevices != null)
            while (cursorForConnectedDevices.moveToNext()) {
                Log.d("DbFuncs","Connected device is:"+cursorForConnectedDevices.getString(0));
            }
        else
        {
            Log.d("DbFuncs","No device is currently connected");
        }
    }

    /*
    public BluetoothSocket getSocketForDevice(SQLiteDatabase mydatabase)
    {
        Cursor cursorForConnectedDevices = mydatabase.rawQuery("SELECT * from DEVICES_CURRENTLY_CONNECTED where BTDeviceMacAddr='"+macAddr+"'", null);
    }
*/


}
