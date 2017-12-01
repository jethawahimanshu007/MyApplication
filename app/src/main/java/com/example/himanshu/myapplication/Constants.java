package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Himanshu on 8/4/2016.
 */
public interface Constants {

    // Hashmap for device to connected devices
     static Map<String,BluetoothSocket> deviceToSocket = new HashMap<String,BluetoothSocket>();
    public static String imagePath=new String();

    public static final int PREAMBLE_SIZE=500;
    public static final String DATABASE_NAME="DTNShare.db";
    public static final int MESSAGE_IMAGE=4;    //Implying that the next message to come is an image
    public static final int MESSAGE_TSRS=5;     //Implying that the next message to come is TSRS
    public static final int MESSAGE_TSRS_BACK=6; //Implying that the next messaege to come is TSRs back
                                                // from the device for which MESSAGE_TSRs was sent
    public static final int MESSAGE_IMAGE_BACK=8;
    public static final int MESSAGE_IMAGE_ACK=9; ///Acknowledegement for Image
    public static final int MESSAGE_IMAGES_TOTAL=10; //Total images
    public static final int MESSAGE_IMAGES_BACK_TOTAL=11;
    public static final int MESSAGE_IMAGE_BACK_ACK=12;
    public static final int MESSAGE_INCENT_REQ=13;  //Incentive required for the device
    public static final int MESSAGE_INCENT_REP=14;  //Incetive reply for the required
    public static final int MESSAGE_INCENT_REW=15;  //Incentive reward for a message received
    public static final int MESSAGE_TRANS_DONE=16;  //Transaction done message

}
