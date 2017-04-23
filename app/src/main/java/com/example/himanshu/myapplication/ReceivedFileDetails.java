package com.example.himanshu.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ReceivedFileDetails extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_file_details);
        Intent intent = getIntent();
        String id = intent.getStringExtra("fileName");
        SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        String allAttributes=new String();
        Cursor cursorForImages = null;




        try {
            cursorForImages = mydatabase.rawQuery("SELECT * from MESSAGE_TBL where destMacAddr<>'NO' and destName<>'NO' and destMacAddr fileName='"+id+"'", null);
            if (cursorForImages == null)
                Log.d("DbFuncGetTags", "cursorForImages isnull");
        } catch (Exception e) {
            Log.d("DbFUncs", "Exception occurred, hahahaha!::" + e);
        }
        try {
            String image;
            ;
            try {


                    while (cursorForImages.moveToNext()) {

                        String sourceDeviceName = cursorForImages.getString(0);allAttributes+="Source name:"+sourceDeviceName+"\n";
                        String sourceDeviceAddr = cursorForImages.getString(0);allAttributes+="Source MAC address:"+sourceDeviceAddr+"\n";
                        String tags = cursorForImages.getString(0);allAttributes+="tags:"+tags+"\n";
                        String latitude = cursorForImages.getString(0);allAttributes+="latitude:"+latitude+"\n";
                        String longitude = cursorForImages.getString(0);allAttributes+="longitude:"+longitude+"\n";
                        String timestamp = cursorForImages.getString(0);allAttributes+="timestamp:"+timestamp+"\n";
                        String destDeviceName = cursorForImages.getString(0);allAttributes+="Destination name:"+destDeviceName+"\n";
                        String destDeviceAddr = cursorForImages.getString(0);allAttributes+="Destination MAC address:"+destDeviceAddr+"\n";
                        String mime = cursorForImages.getString(0);allAttributes+="Mime:"+mime+"\n";
                        String format = cursorForImages.getString(0);allAttributes+="Format:"+format+"\n";
                        String fileName = cursorForImages.getString(0);allAttributes+="File name:"+fileName+"\n";


                    }


            } catch (Exception e) {
                Log.d("DbFunctions", "Exception occured in sqlite query!!! ::" + e);
            }


        }
        catch(Exception e)
        {
            Log.d("ReceivedDetails","Exception:"+e);
        }

    }
}
