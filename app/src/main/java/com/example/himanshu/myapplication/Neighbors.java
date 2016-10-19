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


        //test test1=new test();
        //byte bytesFortest1[]=Serializer.ObjectToBytes(test1);
       // Log.d("Neighbors","The length of byte array to be sent is:"+bytesFortest1.length);
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mReceiver, filter3);


        ////Directly trying to connect to Himanshu Phone
        BluetoothDevice device= BluetoothAdapter.getDefaultAdapter().getRemoteDevice("D0:87:E2:4E:7A:2B");
        if(device==null)
        {
            Log.d("Neighbors","Device HimanshuTablet is null");
            System.exit(1);
        }
       // ConnectThread newConnectThread=new ConnectThread(device);
        //Log.d("Neighbors","value of socket is:"+newConnectThread.mmSocket);
        //newConnectThread.start();


        mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null);
        generalBTFuncs=new GeneralBTFuncs(getApplicationContext(),android.R.layout.simple_list_item_1,this,mydatabase);
        generalBTFuncs.run();


       /* ArrayAdapter<String> mArrayAdapter= generalBTFuncs.runDiscovery(this,android.R.layout.simple_list_item_1,this,mydatabase);
        if(mArrayAdapter!=null) {
            Log.d("Neighbors", "ArrayAdapter is not null");
        }
        */

    }

    @Override
    protected void onDestroy() {

        //mydatabase.close();
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

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                /*
                Log.d("Neighbors","Device connected and socket for comm after connection are "+device.getName()+" and "+deviceToSocket.get(device));

                        Cursor cursorForTagsForLocalDevice=mydatabase.rawQuery("SELECT GROUP_CONCAT(Tags) from IMAGE_TAG_RELATION",null);
                       try {
                           String tagsForLocalDevice=new String();
                           while (cursorForTagsForLocalDevice.moveToNext()) {
                                tagsForLocalDevice = cursorForTagsForLocalDevice.getString(0);
                           }
                           byte[] byteArrayForTagsForLocalDevice;
                           int sizeForTagsForLocalDevice=0;
                           String preambleString = new String();
                           if(tagsForLocalDevice!=null) {
                               byteArrayForTagsForLocalDevice = tagsForLocalDevice.getBytes();
                               sizeForTagsForLocalDevice = byteArrayForTagsForLocalDevice.length;
                           }

                               preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TAGS + "::MessageSize:" + sizeForTagsForLocalDevice + "::";


                           byte preamble[]=new byte[Constants.PREAMBLE_SIZE];
                           Log.d("Neighbors","Preamble string is:"+preambleString);
                           byte preambleSomething[]=preambleString.getBytes();
                           Arrays.fill(preamble,(byte)0);
                           System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);


                           ConnectedThreadWithRequestCodes newConnectedThread=new ConnectedThreadWithRequestCodes(deviceToSocket.get(device));
                           newConnectedThread.write(preamble);
                           Log.d("Neighbors","Tags for this device found from db are:"+tagsForLocalDevice);
                           Cursor cursorDeviceTag=mydatabase.rawQuery("SELECT * from DEVICE_TAG",null);
                           while(cursorDeviceTag.moveToNext())
                           {
                               String deviceName=cursorDeviceTag.getString(0);
                           }
                       }
                       catch(Exception e)
                       {
                            Log.d("Neighbors","Exception occured:"+e);
                       }
                    */

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

            }
        }
    };


}
