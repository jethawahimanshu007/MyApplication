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
import android.support.v4.app.ActivityCompat;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import android.content.Context;
import android.widget.Toast;



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


        try {

            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e) { Log.d("ConnectThread","MaybeException"); }
        mmSocket = tmp;
    }


    public void run() {

        Log.d("ConnectThread","Entered the run function of ConnectThread");

        mBluetoothAdapter.cancelDiscovery();

        try {

            // Register the BroadcastReceiver
            //ReceiverBroadcast1 rb1=new ReceiverBroadcast1();
           //IntentFilter filter = new IntentFilter();
            //filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);

           //context.registerReceiver(rb1, filter); // Don't forget to unregister during onDestroy

            Log.d("Client-MainAc","Client trying to connect");
           // new DbFunctions().showConnectedDevices(mydatabase);
            //if(new DbFunctions().ifDeviceConnected(mydatabase,mmDevice.getAddress())==0) {
                Log.d("CT",mmDevice.getName()+" is not already connected, connecting to it now");
            //if(mmDevice.getName().equals("HimanshuTablet"))
                mmSocket.connect();
            Log.d("CT","The device is successfully connected");
            try {
                ConnectedThreadWithRequestCodes newConnectedThreadToRead=new ConnectedThreadWithRequestCodes(mmSocket,context);
                newConnectedThreadToRead.start();
            }
            catch(Exception e)
            {
                Log.d("ConnectThread","Exception is thrown!! LOL::"+e);
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
                String preambleString;
                if(tagsForLocalDevice!=null) {
                    byteArrayForTagsForLocalDevice = tagsForLocalDevice.getBytes();
                    sizeForTagsForLocalDevice = byteArrayForTagsForLocalDevice.length;
                    byteArrayForTagsForLocalDevice=tagsForLocalDevice.getBytes();
                }

                preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TAGS + "::MessageSize:" + sizeForTagsForLocalDevice + "::";


                byte preamble[]=new byte[Constants.PREAMBLE_SIZE];
                Log.d("CT","Preamble string is:"+preambleString);
                byte preambleSomething[]=preambleString.getBytes();
                Arrays.fill(preamble,(byte)0);
                System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);


                ConnectedThreadWithRequestCodes newConnectedThread=new ConnectedThreadWithRequestCodes(mmSocket,context);
                if(mmSocket==null) Log.d("CT","mmSocket is null");
                if(newConnectedThread==null) Log.d("CT","newConnectedThread is null");
                newConnectedThread.write(preamble);
                if(byteArrayForTagsForLocalDevice!=null) {
                    Log.d("ConnectThread","Going to send byteArrayForLocalDevice");
                    newConnectedThread.write(byteArrayForTagsForLocalDevice);

                }
                Log.d("ConnectThread","Tags for this device found from db are:"+tagsForLocalDevice);


            }
            catch(Exception e)
            {
                Log.d("Neighbors","Exception occured:"+e);
            }


            String imagePath="/storage/emulated/0/Download/Sonny-Bryant-Junior.jpg";


            //}


        } catch (Exception connectException) {
                Log.d("ConnectThread","Exception occured for device  "+mmDevice.getName()+" :"+connectException);
            try {
                mmSocket.close();
            } catch (Exception closeException) { }
            return;
        }

    }


    public void cancel() {
        try {
            mmSocket.close();
        } catch (Exception e) { }
    }

    public  class ReceiverBroadcast1 extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d("Ct","Connected to device successfully");

                String imagePath="/storage/emulated/0/Download/Sonny-Bryant-Junior.jpg";
                try {
                    java.io.RandomAccessFile raf = new java.io.RandomAccessFile(imagePath, "r");
                    byte[] b = new byte[(int) raf.length()];
                    Log.d("CTwRC", "Length of byte array from image is:" + b.length);
                    raf.readFully(b);
                   // Log.d("CT","maxReceivePacketSize and maxTransmitPacketSize:"+mmSocket.getMaxReceivePacketSize()+" and "+mmSocket.getMaxTransmitPacketSize());
                    OutputStream outputStream=mmSocket.getOutputStream();
                    outputStream.write(b);
                }
                catch(Exception e)
                {
                    Log.d("CT","Error sending Sonny-bryant-junior:"+e);
                }

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("ReceiverBroadcast1","hahahahahaha,The connected device is:"+device.getName());
                Log.d("CT","Device connected is:"+mmSocket.getRemoteDevice().getAddress());
                new DbFunctions().insertIntoDevicesConnected(mydatabase,mmSocket.getRemoteDevice().getAddress(),mmSocket.toString());
                Log.d("ConnectThread","Mostly the connection is established to  "+mmDevice.getAddress()+":"+mmDevice.getName());
                Log.d("ConnectThread"," And the Connected Socket is:"+mmSocket);


            }

        }
    }

}



