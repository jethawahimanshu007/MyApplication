package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;


import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Himanshu on 8/2/2016.
 */

//This is a client which will connect to server in acceptThread
class ConnectThread extends Thread {
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    Uri uri;
    public final BluetoothSocket mmSocket;
    public final BluetoothDevice mmDevice;
  //HImanshu--  private final Handler mHandlerInput;
    BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;


        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code

            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e) { Log.d("ConnectThread","MaybeException"); }
        mmSocket = tmp;
    }

    public void run() {
        Log.d("ConnectThread","Entered the run function of ConnectThread");
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            Log.d("Client-MainAc","Client trying to connect");
            mmSocket.connect();
            Log.d("ConnectThread","Mostly the connection is established to  "+mmDevice.getAddress()+":"+mmDevice.getName());
            Log.d("ConnectThread"," And the Connected Socket is:"+mmSocket);



       ///Himanshu--     ConnectedThread newConnectedThread=new ConnectedThread(mmSocket,mHandlerInput);

        } catch (Exception connectException) {
                Log.d("ConnectThread","Exception occured:"+connectException);
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (Exception closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)

        //manageConnectedSocket(mmSocket);
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (Exception e) { }
    }
}



