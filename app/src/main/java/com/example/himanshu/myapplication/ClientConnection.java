package com.example.himanshu.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by Himanshu on 8/10/2016.
 */
public class ClientConnection {
    Handler mHandler;
    BluetoothDevice device;
    ConnectThread newConnectThread;
    ConnectedThread newConnectedThreadSend;
    ConnectedThread newConnectedThreadReceive;
    ClientConnection(Handler mHandlerInput,String macAddr)
    {
        mHandler=mHandlerInput;
        device= BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddr);
        newConnectThread=new ConnectThread(device);
    }
    public int connectToClient()
    {

        try {
            newConnectThread.run();
            Log.d("ClientCOnnection","Hopefully the connection is established");
        }
        catch(Exception e)
        {
            Log.d("ClientConnection","Exception occured in connect method"+e);
        }

        return 0;
    }
    public void startListening()
    {
        newConnectedThreadReceive=new ConnectedThread(newConnectThread.mmSocket);
        newConnectedThreadReceive.run();
    }
    public void sendImageBytes(BluetoothSocket socket, byte[] bytes, byte[] preamble)
    {
        newConnectedThreadSend=new ConnectedThread(newConnectThread.mmSocket);
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //String preambleMessage="Preamble::"+"MessageSize:"+bytes.length+"::MessageFormat:JPEG::";
       // byte[] preamble=new byte[100];
        //preamble=preambleMessage.getBytes();
        Log.d("ClientCOnnection","Preamble sent is:"+new String(preamble));
        newConnectedThreadSend.write(preamble);

        Log.d("ClientConnection","Length of byte array for image is:"+bytes.length);
        newConnectedThreadSend.write(bytes);

    }
    public void sendImage(BluetoothSocket socket, Bitmap bitmap)
    {
        newConnectedThreadSend=new ConnectedThread(newConnectThread.mmSocket);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,baos); //bm is the bitmap object
        byte[] bytes = baos.toByteArray();
        String preambleMessage="Preamble::"+"MessageSize:"+bytes.length+"::MessageFormat:JPEG::";
        byte[] preamble=preambleMessage.getBytes();
        newConnectedThreadSend.write(preamble);
        Log.d("ClientConnection","Length of byte array for image is:"+bytes.length);

        newConnectedThreadSend.write(bytes);

    }
}
