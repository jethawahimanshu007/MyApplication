package com.example.himanshu.myapplication;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.UUID;

//This file acts as a service to start the AcceptThread,
// so that it runs in the background
public class BTAcceptService extends IntentService {

    public BTAcceptService() {
        super("BTAcceptService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try{
            Log.d("BTAcceptService","");
            Log.d("BTAcceptService","BTService called");
            Log.d("BTAcceptService","If ConstantsClass.mydatabaseLatest is null:"+ConstantsClass.mydatabaseLatest);

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

            AcceptThread at=new AcceptThread(getBaseContext());
            at.run();
        }
        catch(Exception e)
        {
                Log.d("AcceptThread","Some exception occured!!");

        }

    }

}
