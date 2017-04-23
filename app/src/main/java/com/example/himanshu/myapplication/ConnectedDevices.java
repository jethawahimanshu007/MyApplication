package com.example.himanshu.myapplication;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectedDevices extends AppCompatActivity {
    Activity ac=this;
    Timer timer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_devices);
        final SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);

        final long delay=0;
        long period=5000;


        try{

            timer.schedule(new TimerTask() {

                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ConnectedDevices","Called ConnectedDevices thread");
                            try {

                                ArrayList<String> devicesInRange = new ArrayList();

                                 ListView lv = (ListView) findViewById(R.id.nearbyDevicesList);

                                Cursor cursorForNearbyDevices = mydatabase.rawQuery("SELECT * from BLUETOOTH_DEVICES_IN_RANGE", null);

                                while (cursorForNearbyDevices.moveToNext()) {
                                    Log.d("ConnectedDevices", "There are devices in range");
                                    String BTDeviceMacAddr = cursorForNearbyDevices.getString(0);
                                    Log.d("ConnectedDevices", "BTDeviceMacAddr" + BTDeviceMacAddr);
                                    if(cursorForNearbyDevices.getString(1)!=null) {
                                        String BTDeviceName = cursorForNearbyDevices.getString(1);
                                        String RSSI=cursorForNearbyDevices.getString(2);
                                        devicesInRange.add(BTDeviceMacAddr + "--" + BTDeviceName + "--" + RSSI);
                                    }
                                    else
                                    {
                                        devicesInRange.add(BTDeviceMacAddr);
                                    }
                                }
                                Cursor cursorForConnectedDevices = mydatabase.rawQuery("SELECT * from DEVICES_CURRENTLY_CONNECTED", null);
                                ArrayList<String> devicesConnected = new ArrayList();
                                Log.d("ConnectedDevices","Number of rows in cursorForConnectedDevices are:"+cursorForConnectedDevices.getCount());
                                if(cursorForConnectedDevices.getCount()!=0) {
                                    while (cursorForConnectedDevices.moveToNext()) {
                                        String BTDeviceMacAddr = cursorForConnectedDevices.getString(0);
                                        Log.d("ConnectedDevices", "Connected device is:" + BTDeviceMacAddr);
                                        String time = cursorForConnectedDevices.getString(1);
                                        devicesConnected.add(BTDeviceMacAddr /*+ "--" + time*/);
                                    }
                                }

                                final ListView lv1 = (ListView) findViewById(R.id.connectedDevicesList);
                                //devicesInRange.add("D0:87:E2:4E:7A:2C-- HimanshuTablet");
                                Log.d("ConnectedDevices","Array devicesinRange is:"+ Arrays.toString(devicesInRange.toArray()));

                                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getBaseContext(),
                                        android.R.layout.simple_list_item_1, android.R.id.text1, devicesConnected);
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                                        android.R.layout.simple_list_item_1, android.R.id.text1, devicesInRange);
                                lv.setAdapter(adapter);
                                lv1.setAdapter(adapter1);
                                lv.invalidateViews();
                                lv1.invalidateViews();
                            }
                            catch(Exception e){Log.d("ConnectedDevices","Exception in connectedDevices:"+e);}
                        }

                    });

            }}, delay,period);

        }
        catch(Exception e)
        {

        }
        //nearbyDevicesList
        //connectedDevicesList

    }
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
