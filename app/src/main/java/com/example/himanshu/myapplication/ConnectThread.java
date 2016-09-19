package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import android.content.Context;

import afrl.phoenix.core.MetadataConstants;
import afrl.phoenix.information.BasicInformation;
import afrl.phoenix.information.Information;
import afrl.phoenix.information.messages.BinaryMessage;
import afrl.phoenix.information.messages.ByteArrayMessage;

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
    public SQLiteDatabase mydatabase;
  //HImanshu--  private final Handler mHandlerInput;
    BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    public Context context;
    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;


        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code

            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e) { Log.d("ConnectThread","MaybeException"); }
        mmSocket = tmp;
    }

    public ConnectThread(BluetoothDevice device,Context context) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.context=context;

        mydatabase = context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code

            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
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

            try {
                ConnectedThreadWithRequestCodes newConnectedThreadToRead=new ConnectedThreadWithRequestCodes(mmSocket,context);
                newConnectedThreadToRead.start();
            }
            catch(Exception e)
            {
                Log.d("AcceptThread","Exception is thrown!! LOL::"+e);
            }

            Cursor cursorForTagsForLocalDevice=null;
            try {
                 cursorForTagsForLocalDevice = mydatabase.rawQuery("SELECT GROUP_CONCAT(Tags) from IMAGE_TAG_RELATION", null);
            }
            catch(Exception e)
            {
                Log.d("ConnectThread","Exception occurred, hahahaha!");
            }
                try {
                String tagsForLocalDevice=new String();
                try {

                   if(cursorForTagsForLocalDevice!=null)
                    while (cursorForTagsForLocalDevice.moveToNext()) {
                        tagsForLocalDevice = cursorForTagsForLocalDevice.getString(0);
                    }
                }
                catch(Exception e)
                {
                    Log.d("ConnectThread","Exception occured in sqlite query!!! ::"+e);
                }
                byte[] byteArrayForTagsForLocalDevice=null;
                int sizeForTagsForLocalDevice=0;
                String preambleString = new String();
                if(tagsForLocalDevice!=null) {
                    byteArrayForTagsForLocalDevice = tagsForLocalDevice.getBytes();
                    sizeForTagsForLocalDevice = byteArrayForTagsForLocalDevice.length;
                    byteArrayForTagsForLocalDevice=tagsForLocalDevice.getBytes();
                }

                preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TAGS + "::MessageSize:" + sizeForTagsForLocalDevice + "::";


                byte preamble[]=new byte[Constants.PREAMBLE_SIZE];
                Log.d("Neighbors","Preamble string is:"+preambleString);
                byte preambleSomething[]=preambleString.getBytes();
                Arrays.fill(preamble,(byte)0);
                System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);


                ConnectedThreadWithRequestCodes newConnectedThread=new ConnectedThreadWithRequestCodes(mmSocket,context);newConnectedThread.write(preamble);
                    if(byteArrayForTagsForLocalDevice.length!=0) {
                        Log.d("ConnectThread","Going to send byteArrayForLocalDevice");
                        newConnectedThread.write(byteArrayForTagsForLocalDevice);

                    }
                Log.d("ConnectThread","Tags for this device found from db are:"+tagsForLocalDevice);
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



