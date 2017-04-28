package com.example.himanshu.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Himanshu on 10/11/2016.
 */
public class ReceiverBroadcast extends BroadcastReceiver{


    public void onReceive(Context context, Intent intent) {

        SQLiteDatabase mydatabase=context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);
        String action = intent.getAction();
        DbFunctions dbFunctions=new DbFunctions();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
        //Log.d("ReceiverBroadcast","Something connected");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            mydatabase.execSQL("INSERT OR IGNORE INTO DEVICES_CURRENTLY_CONNECTED(BTDeviceMacAddr) VALUES( '"+device.getAddress()+"')");
            mydatabase.execSQL("INSERT OR IGNORE INTO BLUETOOTH_DEVICES_IN_RANGE VALUES('" + device.getAddress() + "','" + device.getName() + "','')");

            dbFunctions.setConnectedLogTBL(mydatabase,device.getAddress());
            Log.d("ReceiverBroadcast","The connected device is:"+device.getName());
        }
        if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d("ReceiverBroadcast","The disconnected device is:"+device.getName());
            mydatabase.execSQL("DELETE FROM DEVICES_CURRENTLY_CONNECTED where BTDeviceMacAddr='"+device.getAddress()+"'");
            mydatabase.execSQL("UPDATE UUID_TBL set isUsed=0 and MacAddress='NO' where MacAddress='"+device.getAddress()+"'" );
            mydatabase.execSQL("DELETE FROM TSR_SHARE_DONE_TBL WHERE MacAd='"+device.getAddress()+"'");
            mydatabase.execSQL("DELETE FROM INCENT_UUID_MAC_TBL where MacAd='"+device.getAddress()+"'");
            dbFunctions.devicesDisconnectedTime(mydatabase,device.getAddress(),device.getName());
            dbFunctions.unsetConnectedLogTBL(mydatabase,device.getAddress());
            Constants.deviceToSocket.remove(device.getAddress());
            mydatabase.execSQL("DELETE FROM INCENT_UUID_MAC_TBL where MacAd='"+device.getAddress()+"'");

        }
        if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
        {
            Log.d("ReceiverBroadcast"," Bluetooth State changed");
            final int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
            Activity newActivity=new Activity();

            if(state==BluetoothAdapter.STATE_ON) {
                Intent serviceIntent = new Intent(context, BTAcceptService.class);
                Log.d("avBar", "Context::" + this);
                context.startService(serviceIntent);
              // GeneralBTFuncs generalBTFuncs=new GeneralBTFuncs(context,android.R.layout.simple_list_item_1,newActivity,mydatabase);
               // generalBTFuncs.run();
            }
            if(state==BluetoothAdapter.STATE_OFF) {
                mydatabase.execSQL("DELETE FROM DEVICES_CURRENTLY_CONNECTED");
                mydatabase.execSQL("DELETE FROM CONNECTED_LOG_TBL");
                mydatabase.execSQL("UPDATE CONNECT_ALL_ATTEMPT_TBL SET doneOrNot=1");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mydatabase.execSQL("DELETE FROM INCENT_UUID_MAC_TBL");
                try{
                if(Constants.deviceToSocket!=null)
                Constants.deviceToSocket.remove(device.getAddress());}
                catch (Exception e)
                {
                    Log.d("ReceiverBroadcast","Exception in ReceiverBroadcast:"+e);
                }

            }
        }

    }
}
