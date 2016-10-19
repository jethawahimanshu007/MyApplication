package com.example.himanshu.myapplication;

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
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
        //Log.d("ReceiverBroadcast","Something connected");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d("ReceiverBroadcast","The connected device is:"+device.getName());
        }


    }
}
