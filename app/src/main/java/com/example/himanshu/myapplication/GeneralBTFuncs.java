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
public class GeneralBTFuncs implements Runnable{

    List<String> al = new ArrayList<String>();
    int flagBTenOrDis=0;
     int discoveryFinished=0;
    long currentTsForAction=0;
   //  Activity activity1;
    BroadcastReceiver mReceiver;
    Context context;
    int delayValue=5000;
    //int delay = 60000; // delay for 60 sec.
    int delay = 0;
    //delay=delayValue;
    int resource; Activity activity;
    ArrayList<BluetoothDevice> BtDevices = new ArrayList<BluetoothDevice>();

    SQLiteDatabase mydatabase=null;
    GeneralBTFuncs(Context context,int resource,Activity activity,SQLiteDatabase mydatabase)
    {
        this.context=context;
        this.resource=resource;
        this.activity=activity;
        this.mydatabase=mydatabase;
    }


    //int delay = 60000; // delay for 5 sec.


    public void run()
    {

        int period = 60000; // repeat every 60 secs.


        Timer timer = new Timer();
                timer.schedule(new TimerTask() {

            public void run() {
               // delay=delayValue+5000;
                int connectAttemptDoneOrNot=1;
                Cursor cursorForConnectAttempt=mydatabase.rawQuery("SELECT * from CONNECT_ALL_ATTEMPT_TBL",null);
                while(cursorForConnectAttempt.moveToNext())
                {
                    connectAttemptDoneOrNot=cursorForConnectAttempt.getInt(0);
                }
                Cursor cursorLog=mydatabase.rawQuery("SELECT * FROM CONNECTED_LOG_TBL where doneOrNot=0",null);
                if(cursorLog.getCount()!=0 )
                {
                    Log.d("GeneralBTFuncs","Discovery not being done in this iteration");
                }
                else {
                    if( connectAttemptDoneOrNot==1)
                    runDiscovery(context, android.R.layout.simple_list_item_1, activity, mydatabase);

                }
                System.out.println("repeating after 60 seconds");



            }

        }, delay,period);
    }
    ////Puts devices in table BLUETOOTH_DEVICES_IN_RANGE
    public ArrayAdapter<String> runDiscovery(Context context, int resource, Activity activity, final SQLiteDatabase mydatabase)
    {

        mydatabase.execSQL("DROP TABLE IF EXISTS BLUETOOTH_DEVICES_IN_RANGE");
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS BLUETOOTH_DEVICES_IN_RANGE(BTDeviceMacAddr VARCHAR,BTDeviceName VARCHAR,RSSI VARCHAR,deviceClass INTEGER,PRIMARY KEY(BTDeviceMacAddr))");
        //This code is for bluetooth testing
        //This line gets the bluetooth radio
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
            if (!mBluetoothAdapter.isEnabled()) {
                //Will ask user to enable bluetooth
                //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
               // activity1=avBar.activityMain;
               // ((Activity)context).startActivityForResult(enableBtIntent, 1);
              // activity.startActivityForResult(enableBtIntent, 1);
            }
            else
            {
                BtDevices= new ArrayList<BluetoothDevice>();;
                mBluetoothAdapter.startDiscovery();
            }


            // Create a BroadcastReceiver for ACTION_FOUND
            mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();
                    // When discovery finds a device
                    if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                    {

                        if(flagBTenOrDis==0) {
                           // Log.d("GeneralBTFuncs", "Bluetooth enabled or disabled");
                            Long tsLong = System.currentTimeMillis()/1000;
                           // Log.d("GeneralBtFuncs","tsLong:"+tsLong);
                            mBluetoothAdapter.startDiscovery();
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

                           // Toast toast = Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT);
                           // Log.d("Neighbors", "Discovery finished");

                            Cursor cursor = mydatabase.rawQuery("SELECT * from BLUETOOTH_DEVICES_IN_RANGE order by RSSI", null);

                            try {
                                while (cursor.moveToNext()) {
                                    String deviceMacAddr = cursor.getString(0);
                                    int deviceClass=cursor.getInt(3);

                                    int RSSI=cursor.getInt(2);
                                    if (!al.contains(deviceMacAddr)) {
                                        if(deviceMacAddr.equals("18:3B:D2:EA:15:62")||deviceMacAddr.equals("18:3B:D2:E9:CC:9B")||deviceClass==268)
                                            al.add(deviceMacAddr+"--"+deviceClass);
                                         //String RSSIs=Integer.toString(RSSI);
                                        //if(RSSIs.length()>0)
                                         //   al.add(deviceMacAddr);

                                        //Log.d("GeneralBTfuncs","RSSI value is:"+RSSI+"and RSSI length of string is:"+RSSIs.length());
                                      //  Toast.makeText(context, deviceMacAddr + ":" + deviceName + " is in range", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                String devices[] = new String[al.size()];
                                devices = al.toArray(devices);
                                Log.d("GeneralBTFuncs", "Number of Bluetooth devices in range are:" + devices.length);

                                Log.d("", "devices are:" + Arrays.toString(devices));
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
                        Log.d("GeneralBTFuncs","Device type and bluetooth class and device class and major device class are:"+device.getType()+" and "+device.getBluetoothClass().getDeviceClass());
                        //Code to check if the device found is a laptop
                        if(device.getBluetoothClass().getDeviceClass()==268)
                        {
                            Log.d("GeneralBTFuncs","Found device "+device.getName()+" is a laptop");
                        }

                        int deviceClass=device.getBluetoothClass().getDeviceClass();
                        short RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                        double txP=Math.pow(10.0,RSSI/10.0);
                        Log.d("GeneralBTFuncs","txP value is:"+txP);
                        //Log.d("GeneralBTFuncs","Max power value might be:"+Math.pow(10.0,10.0));
                        //String RSSI= intent.getParcelableExtra(BluetoothDevice.EXTRA_RSSI);
                        if(!BtDevices.contains(device)) {
                            BtDevices.add(device);
                            // Add the name and address to an array adapter to show in a ListView
                           // Toast toast = Toast.makeText(context, "Device found:" + device.getName(), Toast.LENGTH_SHORT);
                           // toast.setMargin(50, 50);
                           // toast.show();
                            Log.d("MainActivity", "Device found:" + device.getName() + "\n");
                            String somethingToDebug = "'" + device.getAddress() + "','" + device.getName() + "'";
                            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS BLUETOOTH_DEVICES_IN_RANGE(BTDeviceMacAddr VARCHAR,BTDeviceName VARCHAR,RSSI VARCHAR,deviceClass INTEGER,PRIMARY KEY(BTDeviceMacAddr))");
                            try {
                                if(!(device.getName().contains("'")))
                                mydatabase.execSQL("INSERT OR IGNORE INTO BLUETOOTH_DEVICES_IN_RANGE VALUES('" + device.getAddress() + "','" + device.getName() + "','"+RSSI+"',"+deviceClass+")");
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
