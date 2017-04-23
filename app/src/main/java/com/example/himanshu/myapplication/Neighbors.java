package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Neighbors extends AppCompatActivity {
    SQLiteDatabase mydatabase;
    Map<BluetoothDevice,BluetoothSocket> deviceToSocket = new HashMap<BluetoothDevice,BluetoothSocket>();
    ArrayList al = new ArrayList();
    public  BluetoothDevice mmDevice;
    private  Handler mHandler;
    GeneralBTFuncs generalBTFuncs;
    String[] clientsInRange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighbors);
        mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null);
       // generalBTFuncs=new GeneralBTFuncs(getApplicationContext(),android.R.layout.simple_list_item_1,this,mydatabase);
       // generalBTFuncs.run();



    }

    @Override
    protected void onDestroy() {


        Log.d("Neighbors","stopping discovery");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.cancelDiscovery();
        Log.d("Neighbors","Neighbors activity onDestroy(): Receiver is unregistered and discovery is stopped");
        try {
            unregisterReceiver(generalBTFuncs.mReceiver);
        }
        catch(Exception e)
        {

        }
        super.onDestroy();

    }



}
