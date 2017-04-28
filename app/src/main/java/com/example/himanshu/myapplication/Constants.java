package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Himanshu on 8/4/2016.
 */
public interface Constants {

    // Trial for a hashmap for device to connectedthreads
    public static Map<String,BluetoothSocket> deviceToSocket = new HashMap<String,BluetoothSocket>();
    public static String imagePath=new String();
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int CONNECTED_DEVICE_ADDR_SOCKET=6;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final int PREAMBLE_SIZE=400;
    public static final String DATABASE_NAME="DTNShare.db";

    public static final int MESSAGE_TAGS=1;     //Implying the next message will be of type tags text
    public static final int MESSAGE_NO_TAGS=2;
    public static final int MESSAGE_TAGS_MATCHING=3;   //Implying that the next message will be of type tags text which will send matching tags
    public static final int MESSAGE_IMAGE=4;    //implying that the next message is an image
    public static final int MESSAGE_TSRS=5;
    public static final int MESSAGE_TSRS_BACK=6;
    public static final int MESSAGE_ACTUAL=7;
    public static final int MESSAGE_IMAGE_BACK=8;
    public static final int MESSAGE_IMAGE_ACK=9;
    public static final int MESSAGE_IMAGES_TOTAL=10;
    public static final int MESSAGE_IMAGES_BACK_TOTAL=11;
    public static final int MESSAGE_IMAGE_BACK_ACK=12;
    public static final int MESSAGE_INCENT_REQ=13;
    public static final int MESSAGE_INCENT_REP=14;
    public static final int MESSAGE_INCENT_REW=15;
    public static final int MESSAGE_TRANS_DONE=16;
    public static final int MESSAGE_TEST=101;




}
