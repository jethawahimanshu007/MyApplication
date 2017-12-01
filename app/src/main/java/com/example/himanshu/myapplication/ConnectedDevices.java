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

//This is to show the list of nearby devices as well as the connected devices
public class ConnectedDevices extends AppCompatActivity {
    Activity ac=this;
    Timer timer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_devices);

        final long delay=0;
        long period=5000;

///Update the lists every 5 seconds
        try{

            timer.schedule(new TimerTask() {

                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //Nearby devices list populated from the database
                                ArrayList<String> devicesInRange = new ArrayList();
                                ListView lv = (ListView) findViewById(R.id.nearbyDevicesList);
                                Cursor cursorForNearbyDevices = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from BLUETOOTH_DEVICES_IN_RANGE", null);

                                while (cursorForNearbyDevices.moveToNext()) {
                                    String BTDeviceMacAddr = cursorForNearbyDevices.getString(0);
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
                                //Connected devices from the database
                                Cursor cursorForConnectedDevices = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from DEVICES_CURRENTLY_CONNECTED", null);
                                ArrayList<String> devicesConnected = new ArrayList();
                                if(cursorForConnectedDevices.getCount()!=0) {
                                    while (cursorForConnectedDevices.moveToNext()) {
                                        String BTDeviceMacAddr = cursorForConnectedDevices.getString(0);
                                        String time = cursorForConnectedDevices.getString(1);
                                        devicesConnected.add(BTDeviceMacAddr /*+ "--" + time*/);
                                    }
                                }
                                //Following lines are used to update the lists
                                final ListView lv1 = (ListView) findViewById(R.id.connectedDevicesList);

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
    }
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
