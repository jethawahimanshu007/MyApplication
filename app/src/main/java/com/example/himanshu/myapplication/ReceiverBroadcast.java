package com.example.himanshu.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Himanshu on 10/11/2016.
 */
///This file is used to handle the events of Bluetooth switch on, switch off, bluetooth device connection and disconnection events
    //and wifi connection and disconnection event
public class ReceiverBroadcast extends BroadcastReceiver{


    public void onReceive(final Context context, Intent intent) {


        try {

            String action = intent.getAction();
            DbFunctions dbFunctions = new DbFunctions();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                try {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO DEVICES_CURRENTLY_CONNECTED(BTDeviceMacAddr) VALUES( '" + device.getAddress() + "')");
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO BLUETOOTH_DEVICES_IN_RANGE VALUES('" + device.getAddress() + "','" + device.getName() + "',''," + device.getBluetoothClass().getDeviceClass() + ")");


                    dbFunctions.setConnectedLogTBL(ConstantsClass.mydatabaseLatest, device.getAddress());
                    Log.d("ReceiverBroadcast", "The connected device is:" + device.getName());
                } catch (Exception e) {
                    Log.d("ReceiverB", "Exception occured");
                }
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("ReceiverBroadcast", "The disconnected device is:" + device.getName());

                ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM DEVICES_CURRENTLY_CONNECTED where BTDeviceMacAddr='" + device.getAddress() + "'");
                ConstantsClass.mydatabaseLatest.execSQL("UPDATE UUID_TBL set isUsed=0 and MacAddress='NO' where MacAddress='" + device.getAddress() + "'");
                ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM TSR_SHARE_DONE_TBL WHERE MacAd='" + device.getAddress() + "'");
                ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM INCENT_UUID_MAC_TBL where MacAd='" + device.getAddress() + "'");
                dbFunctions.devicesDisconnectedTime(ConstantsClass.mydatabaseLatest, device.getAddress(), device.getName());
                dbFunctions.unsetConnectedLogTBL(ConstantsClass.mydatabaseLatest, device.getAddress());
                Constants.deviceToSocket.remove(device.getAddress());
                ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM INCENT_UUID_MAC_TBL where MacAd='" + device.getAddress() + "'");


            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.d("ReceiverBroadcast", " Bluetooth State changed");
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    Intent serviceIntent = new Intent(context, BTAcceptService.class);
                    Log.d("avBar", "Context::" + this);
                    context.startService(serviceIntent);
                    Activity a=new Activity();
                }
                if (state == BluetoothAdapter.STATE_OFF) {
                    ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM DEVICES_CURRENTLY_CONNECTED");
                    ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM CONNECTED_LOG_TBL");
                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE CONNECT_ALL_ATTEMPT_TBL SET doneOrNot=1");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM INCENT_UUID_MAC_TBL");
                    try {
                        if (Constants.deviceToSocket != null)
                            Constants.deviceToSocket.remove(device.getAddress());
                    } catch (Exception e) {
                        Log.d("ReceiverBroadcast", "Exception in ReceiverBroadcast:" + e);
                    }

                }
            }
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                } else {

                }
            }

            if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
            {
                Log.d("ReciverBroadcast","Wi fi is switched ON or OFF!!");
            }

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action))
            {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                //Log.d("ReceiverBroadcast","WIFI ON or OFF!!");
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if ( (netInfo.getDetailedState()==(NetworkInfo.DetailedState.CONNECTED)) )
                {
                    Log.d("ReceiverBroadcast","WIFI ON or OFF!!");
                    // Wifi is connected, do what you want to do
                    ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS WIFI_NODES_TBL");
                    ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS WIFI_NODES_TBL(IpAddress VARCHAR, PRIMARY KEY(IpAddress))");

                    final Context contextThis=context;
                    Handler handler = new Handler() {

                        public void handleMessage(Message msg) {
                            Log.d("ReceiverBroadcast","Message received from BackTask");
                            String aResponse = msg.getData().getString("message");
                            Log.d("ReceiverBroadcast","Recieved message is:"+aResponse);
                            Intent intent = new Intent("WIFI_DIS_DONE");
                            // You can also include some extra data.
                            intent.putExtra("message", "Wi Fi Disovery done");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            /*
                            Intent intent = new Intent(contextThis,WifiDiscovery.class);
                            LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(contextThis);
                            mgr.sendBroadcast(intent);*/
                        }
                    };
                    ContextHandler contextHandler=new ContextHandler(context,handler);
                    new BackTask().execute(contextHandler);

                }
            }



        }
        catch(Exception e)
        {
            Log.d("ReceiverB","Exception occured:"+e);
        }
    }

}
