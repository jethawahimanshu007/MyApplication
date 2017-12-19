package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.widget.Toast;

import static android.bluetooth.BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA;
import static android.bluetooth.BluetoothClass.Device.PHONE_CELLULAR;
import static android.bluetooth.BluetoothClass.Device.PHONE_CORDLESS;
import static android.bluetooth.BluetoothClass.Device.PHONE_ISDN;
import static android.bluetooth.BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY;
import static android.bluetooth.BluetoothClass.Device.PHONE_SMART;
import static android.bluetooth.BluetoothClass.Device.PHONE_UNCATEGORIZED;


/**
 * Created by Himanshu on 8/2/2016.
 */

//This is a client which will connect to server in acceptThread
class ConnectThread extends Thread {


    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public final BluetoothSocket mmSocket[];
    public final BluetoothDevice mmDevice[];
    public int deviceClasses[];
    BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    public Context context;


    //Connection or disconnection events are handled by Broadcast Receiver
     BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DbFunctions dbFunctions=new DbFunctions();
            //If a connection event is accepted
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Put info about connected device into the database
                ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO DEVICES_CURRENTLY_CONNECTED(BTDeviceMacAddr) VALUES( '"+device.getAddress()+"')");
                Log.d("ReceiverBroadcast","The connected device is:"+device.getName());
            }
        }
    };
    //Constructor for ConnectThread, accepts list of "MacAddress" strings and a Context object
    //Initilializes BluetoothDevice and BluetoothSocket objects for all the devices
    public ConnectThread(String deviceStrings[],Context context) {
        BluetoothSocket tmp = null;
        mmDevice=new BluetoothDevice[deviceStrings.length];
        mmSocket=new BluetoothSocket[deviceStrings.length];
        for(int i=0;i<deviceStrings.length;i++)
        {
            BluetoothDevice device= BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceStrings[i]);
            mmDevice[i]=device;
            try {

                mmSocket[i]=mmDevice[i].createInsecureRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (Exception e) { Log.d("ConnectThread","MaybeException"); }

        }
        this.context=context;
    }


    public void run() {
        //Cancel discovery needs to be ensured before starting connect function
        //because discovery consumes all of the bandwidth
        mBluetoothAdapter.cancelDiscovery();
        //Register for receiving connect event
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_ACL_CONNECTED");
        context.registerReceiver(receiver,filter);
        //For all the discovered devices, try to connect
            for(int i=0;i<mmSocket.length;i++)
            {
                //This condition checks whether a device is connected or not
                if (new DbFunctions().ifDeviceConnected(ConstantsClass.mydatabaseLatest, mmDevice[i].getAddress()) == 0) {
                            try {
                                    Log.d("ConnectThread", "Trying to connect to device:" + mmDevice[i].getName());
                                    mmSocket[i].connect();
                                    //If the device is connected, put it in connected devices table in DATABASE and
                                    //Update the TSR_SHARE_DONE table to reflect that the TSR sharing process is not done for this device
                                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO DEVICES_CURRENTLY_CONNECTED(BTDeviceMacAddr) VALUES( '" + mmDevice[i].getAddress() + "')");
                                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_SHARE_DONE_TBL VALUES('" + mmDevice[i].getAddress() + "',0)");
                                    //Put the connected device in deviceToSocket hashmap
                                    if(Constants.deviceToSocket.get(mmDevice[i].getAddress())==null)
                                        Constants.deviceToSocket.put(mmDevice[i].getAddress(), mmSocket[i]);

                                    try {
                                        //Once the connection is established, start the connected thread, so as to initiliaze the data receiveing process
                                        ConnectedThreadWithRequestCodes newConnectedThreadToRead = new ConnectedThreadWithRequestCodes(mmSocket[i], context);
                                        newConnectedThreadToRead.start();
                                    } catch (Exception e) {
                                        Log.d("ConnectThread", "Exception is thrown!! LOL::" + e);
                                    }

                                } catch (Exception e) {
                                    Log.d("ConnectThread", "Error in connecting mostly:" + e);
                                }

                                //When all the devices are tried for connection establishment, the postConnection function
                                //starts the initiation of TSR sharing
                                if(i==mmSocket.length-1)
                                {
                                    Log.d("ConnectThread","All the devices have been tried for connection establishment");
                                    postConnection();
                                }
                    }

            }



    }

    //Function for sharing the TSRs with all the connected devices
    public void postConnection()
    {
        //Set connect_attempt_tbl zero value for all the devices
        ConstantsClass.mydatabaseLatest.execSQL("UPDATE CONNECT_ALL_ATTEMPT_TBL SET doneOrNot=0");
        Iterator it = Constants.deviceToSocket.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());

            String Role=new String();
            //Share TSRs with newly connected device
            String TSRsToShare = new DbFunctions().calculateTSRsPre(ConstantsClass.mydatabaseLatest);

            //To share role of myself with the connected device-- whether sergeant or soldier
            Cursor cursorForRole=ConstantsClass.mydatabaseLatest.rawQuery("SELECT Role from ROLE_TBL where MACAd='SELF'",null);
            while(cursorForRole.moveToNext())
            {
                Role=cursorForRole.getString(0);
            }
            byte[] byteArrayTSRsToShare = TSRsToShare.getBytes();

            String ratingsDevices=new String();
            int countR=0;
            //This part is to share ratings of all the encountered users with the connected device
            Cursor cursorForDeviceRatings=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from USER_RATING_MAP_TBL",null);
            int noOfRows=cursorForDeviceRatings.getCount();
            if(noOfRows==0)
                ratingsDevices="NO";
            while(cursorForDeviceRatings.moveToNext())
            {
                if(countR!=0) {
                    ratingsDevices+=",";
                }
                    String MacAd = cursorForDeviceRatings.getString(0);
                    double rating = cursorForDeviceRatings.getDouble(1);
                    ratingsDevices += MacAd + "--" + rating;

                countR++;
            }
            //This part is to share the no of TSRs matching requirement for a messsage to be shared
            //For example, if this no is set as 2 in the database, the other device will only transfer
            //those messages which are tagged with 2 keywords that I am interested in

            //Preamble string construction
            ///Himanshu--Work on changing this preamble---
            //Send flag for pull and push--- if push, keep the preamble like this except add flag for push and remove priorityTags
            //Else add radius and coordinates info and tags!!
            Cursor cursorForMode=ConstantsClass.mydatabaseLatest.rawQuery("SELECT mode from OPERATION_MODE_TBL where MacAddr='SELF'",null);
            int mode=0;
            while(cursorForMode.moveToNext())
            {
                mode=cursorForMode.getInt(0);
            }
            Log.d("Settings","Mode of operation in DB inside ConnectThread is:"+mode);
            String preambleString=new String();
            if(mode==1)
            {
                Cursor cursorForTSRNoSelf=ConstantsClass.mydatabaseLatest.rawQuery("SELECT SIList FROM HIGH_PRIO_TAGS_TBL where MacAddr='SELF'",null);
                cursorForTSRNoSelf.moveToNext();
                String prioSIList=cursorForTSRNoSelf.getString(0);
                Cursor cursorForCoords=ConstantsClass.mydatabaseLatest.rawQuery("SELECT lat,long FROM COORDINATES_TBL where MacAddr='SELF'",null);
                cursorForCoords.moveToNext();
                double latitude=cursorForCoords.getDouble(0);double longitude=cursorForCoords.getDouble(1);
                String location=latitude+","+longitude;
                Cursor cursorForRadius=ConstantsClass.mydatabaseLatest.rawQuery("SELECT radius FROM RADIUS_TBL where MacAddr='SELF'",null);
                cursorForRadius.moveToNext();
                double radius=cursorForRadius.getDouble(0);
                preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TSRS + "::MessageSize:" + byteArrayTSRsToShare.length + "::Role:"+Role+"::"+ratingsDevices+"::"+mode+"::"+prioSIList+"::"+location+"::"+radius+"::";

            }
            else
            {
                preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TSRS + "::MessageSize:" + byteArrayTSRsToShare.length + "::Role:"+Role+"::"+ratingsDevices+"::"+mode+"::";
            }
            //String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TSRS + "::MessageSize:" + byteArrayTSRsToShare.length + "::Role:"+Role+"::"+ratingsDevices+"::"+prioSIList+"::";
            ConnectedThreadWithRequestCodes newConnectedThread = new ConnectedThreadWithRequestCodes((BluetoothSocket) pair.getValue(), context);
            Log.d("CTwRC","Sending TSRs to"+((BluetoothSocket) pair.getValue()).getRemoteDevice().getName()+"--haha  TSRs:"+TSRsToShare);
            //Preamble string sending
            newConnectedThread.writePreamble(preambleString);
            //TSR sharing
            newConnectedThread.write(byteArrayTSRsToShare);
        }
        ConstantsClass.mydatabaseLatest.execSQL("UPDATE CONNECT_ALL_ATTEMPT_TBL SET doneOrNot=1");

    }

}



