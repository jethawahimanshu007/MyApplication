package com.example.himanshu.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * Created by Himanshu on 8/22/2016.
 */
public class GeneralBTFuncs {



    BroadcastReceiver mReceiver;
    ////Puts devices in table BLUETOOTH_DEVICES_IN_RANGE
    public ArrayAdapter<String> runDiscovery(Context context, int resource, Activity activity, final SQLiteDatabase mydatabase)
    {
        mydatabase.execSQL("DROP TABLE IF EXISTS BLUETOOTH_DEVICES_IN_RANGE");
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS BLUETOOTH_DEVICES_IN_RANGE(BTDeviceMacAddr VARCHAR,BTDeviceName VARCHAR,PRIMARY KEY(BTDeviceMacAddr))");
        //This code is for bluetooth testing
        //This line gets the bluetooth radio
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(context,resource);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
            }


            // Create a BroadcastReceiver for ACTION_FOUND
            mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();
                    // When discovery finds a device
                    if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        //discovery starts, we can show progress dialog or perform other tasks
                        ///Showing a toast
                        Log.d("GeneralBTFuncs","Started discovery!!");
                        Toast toast=Toast.makeText(context,"Discovery started", Toast.LENGTH_SHORT);
                        toast.setMargin(50,50);
                        toast.show();
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        //discovery finishes, dismis progress dialog
                        Log.d("MainActivity","Finished discovery!!");
                        Toast toast=Toast.makeText(context,"Discovery finished", Toast.LENGTH_SHORT);
                        toast.setMargin(50,50);
                        toast.show();
                        context.unregisterReceiver(mReceiver);

                    }
                    else
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        // Add the name and address to an array adapter to show in a ListView
                        Toast toast=Toast.makeText(context,"Device found:"+device.getName(), Toast.LENGTH_SHORT);
                        toast.setMargin(50,50);
                        toast.show();
                        Log.d("MainActivity","Device found:"+device.getName()+"\n");
                        String somethingToDebug="'"+device.getAddress()+"','"+device.getName()+"'";
                        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS BLUETOOTH_DEVICES_IN_RANGE(BTDeviceMacAddr VARCHAR,BTDeviceName VARCHAR,PRIMARY KEY(BTDeviceMacAddr,BTDeviceName))");
                        try
                        {
                        mydatabase.execSQL("INSERT INTO BLUETOOTH_DEVICES_IN_RANGE VALUES('"+device.getAddress()+"','"+device.getName()+"')");}
                        catch(Exception e)
                        {
                            Log.d("GeneralBT","Exception thrown while inserting into BLUETOOTH_DEVICES_IN_RANGE:"+e);
                        }
                        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        //Log.d("MainActivity","List of devices:"+mArrayAdapter);
                    }
                }
            };
            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

            mBluetoothAdapter.startDiscovery();
        }
        return mArrayAdapter;
    }
}
