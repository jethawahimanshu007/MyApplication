package com.example.himanshu.myapplication;

/**
 * Created by Himanshu on 8/4/2016.
 */
public interface Constants {

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

    public static final int PREAMBLE_SIZE=100;
    public static final String DATABASE_NAME="DTNShare.db";

    public static final int MESSAGE_TAGS=1;
    public static final int MESSAGE_NO_TAGS=2;


}
