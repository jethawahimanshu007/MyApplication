package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Himanshu on 8/27/2016.
 */

/*
This file is used to start a thread which will open socket for incoming connections
The accept function() is a blocking call which waits for incoming connections
In this file, when a connection is accepted, another Accept Thread is started so as
accept connections from more devices
* */
    class AcceptThread extends Thread {
    Context context;
        private static  UUID MY_UUID ;
        private final BluetoothServerSocket mmServerSocket;
        BluetoothSocket mmSocket = null;
        private  InputStream mmInStream;
        public  OutputStream mmOutStream;

    //Constructor for AcceptThread, takes the context object
    public AcceptThread(Context context) {
        BluetoothServerSocket tmp = null;
        BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        this.context=context;
        try {

             MY_UUID =
                    UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BTcheck", MY_UUID);
        } catch (Exception e) { }
        mmServerSocket = tmp;
    }
    //This function is the run function for the thread.. this function calls accept function which keeps on waiting for
    //incoming connections
    public void run() {

            Log.d("AcceptClass-MainAc","Started listening");
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    mmSocket = mmServerSocket.accept();
                    Log.d("AcceptThread","Connection is accepted from the device:"+mmSocket.getRemoteDevice().getAddress());
                    //If the accept function accepts the connection, the following code will be executed
                    //deviceToSocket is a Hashmap which stores MacAddress to Socket mapping
                    Constants.deviceToSocket.put(mmSocket.getRemoteDevice().getAddress(),mmSocket);
                    mmInStream=mmSocket.getInputStream();
                    mmOutStream=mmSocket.getOutputStream();

                    new DbFunctions().setConnectedLogTBL(ConstantsClass.mydatabaseLatest,mmSocket.getRemoteDevice().getAddress());

                    //Once the connection is accepted, ConnectedThread handles the data transfer
                    try {
                        ConnectedThreadWithRequestCodes newConnectedThread=new ConnectedThreadWithRequestCodes(mmSocket,context);
                        newConnectedThread.start();
                    }

                    catch(Exception e)
                    {
                        //If some exception occured, AcceptThread will be started again
                        Log.d("AcceptThread","Exception is thrown!! LOL::"+e);
                        AcceptThread at=new AcceptThread(context);
                        at.start();
                    }
                    //After accepting a connection, AcceptThread will listen for more incoming connections
                    AcceptThread at=new AcceptThread(context);
                    at.start();
                } catch (Exception e) {
                    Log.d("AcceptThread","Exception is thrown, lol..:"+e);
                    break;
                }
            }
        }
    }

