package com.example.himanshu.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Himanshu on 8/22/2016.
 */
//This class is used to perform the bluetooth discovery of all the nearby devices--
//after the devices are discovered, they are saved in the database
public class GeneralBTFuncs implements Runnable{

    List<String> al = new ArrayList<String>();
    int flagBTenOrDis=0;
     int discoveryFinished=0;
    long startTimeDiscovery=0;
    int calledFirstTime=0;
    BroadcastReceiver mReceiver;
    Context context;
    int delay = 0;
    int resource; Activity activity;
    ArrayList<BluetoothDevice> BtDevices = new ArrayList<BluetoothDevice>();

    GeneralBTFuncs(Context context,int resource,Activity activity,SQLiteDatabase mydatabaseLatest)
    {
        this.context=context;
        this.resource=resource;
        this.activity=activity;
    }

    //run function of GeneralBtFuncs-- performs discovery every 60 seconds
    public void run()
    {

        int period = 60000; // repeat every 60 secs.
        Log.d("GeneralBTFuncs","General BTfuncs is called");

        Timer timer = new Timer();
                timer.schedule(new TimerTask() {

            public void run() {
                int connectAttemptDoneOrNot=1;
                //Check if all the devices from the previous discovery are tried for connection, if not do not do discovery
                Cursor cursorForConnectAttempt=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from CONNECT_ALL_ATTEMPT_TBL",null);
               try {
                   while (cursorForConnectAttempt.moveToNext()) {
                       connectAttemptDoneOrNot = cursorForConnectAttempt.getInt(0);
                   }
                   Cursor cursorLog = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * FROM CONNECTED_LOG_TBL where doneOrNot=0", null);
                   if (cursorLog.getCount() != 0) {
                       Log.d("GeneralBTFuncs", "Discovery not being done in this iteration");
                   } else {
                       if (connectAttemptDoneOrNot == 1)
                           runDiscovery(context, android.R.layout.simple_list_item_1, activity, ConstantsClass.mydatabaseLatest);
                        else if(calledFirstTime==0)
                       {
                           calledFirstTime++;
                           runDiscovery(context, android.R.layout.simple_list_item_1, activity, ConstantsClass.mydatabaseLatest);
                       }
                   }
                   System.out.println("repeating after 60 seconds");
               }
               catch(Exception e)
               {
                   Log.d("GeneralBTFuncs","Some exception occured!");
               }


            }

        }, delay,period);
    }

    ////Puts devices in table BLUETOOTH_DEVICES_IN_RANGE
    public ArrayAdapter<String> runDiscovery(Context context, int resource, Activity activity, final SQLiteDatabase mydatabaseLatest)
    {

        ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS BLUETOOTH_DEVICES_IN_RANGE");
        ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS BLUETOOTH_DEVICES_IN_RANGE(BTDeviceMacAddr VARCHAR,BTDeviceName VARCHAR,RSSI VARCHAR,deviceClass INTEGER,PRIMARY KEY(BTDeviceMacAddr))");
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(context,resource);
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("GeneralBTFuncs","Inside runDiscovery function");
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            //3 steps to show an alert dialog
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Error")
                    .setTitle("Bluetooth is not supported");

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
        }
        else
        {
                BtDevices= new ArrayList<BluetoothDevice>();;
                mBluetoothAdapter.startDiscovery();
                startTimeDiscovery=System.currentTimeMillis();
                long TimeKeeper;
                while(true)
                {
                    TimeKeeper=System.currentTimeMillis();
                    //Following condition checks if the time for which discovery has been going on is greater than 15 seconds,
                    //Discovery is stopped if it exceeds 15 seconds!
                    if(discoveryFinished!=1 && TimeKeeper-startTimeDiscovery>15000)
                    {
                        mBluetoothAdapter.cancelDiscovery();
                        Cursor cursor = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from BLUETOOTH_DEVICES_IN_RANGE order by RSSI", null);

                        try {
                            while (cursor.moveToNext()) {
                                String deviceMacAddr = cursor.getString(0);

                                if (!(al.contains(deviceMacAddr))) {
                                    al.add(deviceMacAddr);
                                }
                            }
                            String devices[] = new String[al.size()];
                            devices = al.toArray(devices);
                            ConnectThread newConnectThread = new ConnectThread(devices, context);
                            newConnectThread.start();

                            discoveryFinished=1;
                        } finally {
                            cursor.close();
                            discoveryFinished=1;
                        }
                        break;
                    }
                }



            // Create a BroadcastReceiver for ACTION_FOUND
            mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();
                    // When discovery finds a device
                    if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                    {

                        if(flagBTenOrDis==0) {
                            Long tsLong = System.currentTimeMillis()/1000;
                            flagBTenOrDis=1;
                        }

                    }
                    else
                    if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        //discovery starts, we can show progress dialog or perform other tasks
                        ///Showing a toast
                        discoveryFinished=0;
                        Log.d("GeneralBTFuncs","Started discovery!!");
                       // Toast toast=Toast.makeText(context,"Discovery started", Toast.LENGTH_SHORT);
                       // toast.setMargin(50,50);
                       // toast.show();
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                        if(discoveryFinished==0) {
                            Log.d("GeneralBTFuncs", "Finished discovery!!");

                            Cursor cursor = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from BLUETOOTH_DEVICES_IN_RANGE order by RSSI", null);
                            try {
                                while (cursor.moveToNext()) {
                                    String deviceMacAddr = cursor.getString(0);
                                    if (!(al.contains(deviceMacAddr))) {
                                            al.add(deviceMacAddr);
                                    }
                                }
                                String devices[] = new String[al.size()];
                                devices = al.toArray(devices);
                                ConnectThread newConnectThread = new ConnectThread(devices, context);
                                newConnectThread.start();

                            discoveryFinished=1;
                            } finally {
                                cursor.close();
                                discoveryFinished=1;
                            }

                        }



                    }
                    else
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Get the BluetoothDevice object from the Intent

                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        //Log.d("GeneralBTFuncs","Device type and bluetooth class and device class and major device class are:"+device.getType()+" and "+device.getBluetoothClass().getDeviceClass());
                        //Code to check if the device found is a laptop
                        if(device.getBluetoothClass().getDeviceClass()==268)
                        {
                            Log.d("GeneralBTFuncs","Found device "+device.getName()+" is a laptop");
                        }

                        int deviceClass=device.getBluetoothClass().getDeviceClass();
                        short RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                        double txP=Math.pow(10.0,RSSI/10.0);
                        //Log.d("GeneralBTFuncs","txP value is:"+txP);
                        //Log.d("GeneralBTFuncs","Max power value might be:"+Math.pow(10.0,10.0));
                        //String RSSI= intent.getParcelableExtra(BluetoothDevice.EXTRA_RSSI);
                        if(!BtDevices.contains(device)) {
                            BtDevices.add(device);

                            ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS BLUETOOTH_DEVICES_IN_RANGE(BTDeviceMacAddr VARCHAR,BTDeviceName VARCHAR,RSSI VARCHAR,deviceClass INTEGER,PRIMARY KEY(BTDeviceMacAddr))");
                            try {
                                if(!(device.getName().contains("'")))
                                ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO BLUETOOTH_DEVICES_IN_RANGE VALUES('" + device.getAddress() + "','" + device.getName() + "','"+RSSI+"',"+deviceClass+")");
                            } catch (Exception e) {
                                Log.d("GeneralBT", "Exception thrown while inserting into BLUETOOTH_DEVICES_IN_RANGE:" + e);
                            }
                            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                        }
                    }
                }
            };
            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.getApplicationContext().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


        }
        return mArrayAdapter;
    }
}
