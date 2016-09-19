package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Neighbors extends AppCompatActivity {
    SQLiteDatabase mydatabase;
    Map<BluetoothDevice,BluetoothSocket> deviceToSocket = new HashMap<BluetoothDevice,BluetoothSocket>();
    ArrayList al = new ArrayList();
    public  BluetoothDevice mmDevice;
    private  Handler mHandler;
    GeneralBTFuncs generalBTFuncs;
    String[] clientsInRange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighbors);


        //test test1=new test();
        //byte bytesFortest1[]=Serializer.ObjectToBytes(test1);
       // Log.d("Neighbors","The length of byte array to be sent is:"+bytesFortest1.length);
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mReceiver, filter3);


        ////Directly trying to connect to Himanshu Phone
        BluetoothDevice device= BluetoothAdapter.getDefaultAdapter().getRemoteDevice("D0:87:E2:4E:7A:2B");
        if(device==null)
        {
            Log.d("Neighbors","Device HimanshuTablet is null");
            System.exit(1);
        }
       // ConnectThread newConnectThread=new ConnectThread(device);
        //Log.d("Neighbors","value of socket is:"+newConnectThread.mmSocket);
        //newConnectThread.start();


        mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null);
        generalBTFuncs=new GeneralBTFuncs();
        ArrayAdapter<String> mArrayAdapter= generalBTFuncs.runDiscovery(this,android.R.layout.simple_list_item_1,this,mydatabase);
        if(mArrayAdapter!=null) {
            Log.d("Neighbors", "ArrayAdapter is not null");
            //Log.d("Neighbors","The first item in arrayadapter is:"+mArrayAdapter.getItem(0));
        }

        Button showDevicesInRange=(Button)findViewById(R.id.devices_in_range);
        showDevicesInRange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Cursor cursor=mydatabase.rawQuery("SELECT * from BLUETOOTH_DEVICES_IN_RANGE",null);

                try {
                    while (cursor.moveToNext()) {
                        String deviceMacAddr=cursor.getString(0);
                        String deviceName=cursor.getString(1);
                        al.add(deviceMacAddr);
                        Toast.makeText(getApplicationContext(), deviceMacAddr+":"+deviceName +" is in range", Toast.LENGTH_SHORT).show();
                    }
                    clientsInRange=new String[al.size()];
                    al.toArray(clientsInRange);


                    Log.d("Neighbors","ClientsInRange[0]:"+clientsInRange[0]);
                    ClientListToConnect listOfClients=new ClientListToConnect();
                    listOfClients.mData=clientsInRange.length;
                    listOfClients.mName=new String[listOfClients.mData];
                    for(int i=0;i<clientsInRange.length;i++)
                    {
                        listOfClients.mName[i]=clientsInRange[i];
                        Log.d("Neighbors","Service for client"+(i+1)+"started");
                        BluetoothDevice device= BluetoothAdapter.getDefaultAdapter().getRemoteDevice(clientsInRange[i]);
                        if(device.getName()!=null) {
                            if(device.getName().equals("HimanshuTablet")) {
                                ConnectThread newConnectThread = new ConnectThread(device, getApplicationContext());
                                newConnectThread.start();
                                deviceToSocket.put(device, newConnectThread.mmSocket);
                                Log.d("Neighbors", "Device and socket while connecting are:" + device.getName() + "  and  " + newConnectThread.mmSocket);
                            }
                        }

                    }
                    Log.d("Neighbors","listOfClients.mName[0]:"+listOfClients.mName[0]);


                } finally {
                    cursor.close();
                }

                Iterator iterator = al.iterator();
                while (iterator.hasNext()) {
                    String macAddrInContext=(String)iterator.next();
                   Log.d("Neighbors","Device is:"+macAddrInContext);

                   // ClientConnection cc=new ClientConnection(mHandler,(String)iterator.next());
                    //cc.connectToClient();

                    //ConnectThread ct=new ConnectThread(BluetoothAdapter.getDefaultAdapter().getRemoteDevice((String)iterator.next()));
                }

            }
        });

    }

    @Override
    protected void onDestroy() {

        Log.d("Neighbors","stopping discovery");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.cancelDiscovery();
        Log.d("Neighbors","Neighbors activity onDestroy(): Receiver is unregistered and discovery is stopped");
        try {
            unregisterReceiver(generalBTFuncs.mReceiver);
        }
        catch(Exception e)
        {

        }
        super.onDestroy();

    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                /*
                Log.d("Neighbors","Device connected and socket for comm after connection are "+device.getName()+" and "+deviceToSocket.get(device));

                        Cursor cursorForTagsForLocalDevice=mydatabase.rawQuery("SELECT GROUP_CONCAT(Tags) from IMAGE_TAG_RELATION",null);
                       try {
                           String tagsForLocalDevice=new String();
                           while (cursorForTagsForLocalDevice.moveToNext()) {
                                tagsForLocalDevice = cursorForTagsForLocalDevice.getString(0);
                           }
                           byte[] byteArrayForTagsForLocalDevice;
                           int sizeForTagsForLocalDevice=0;
                           String preambleString = new String();
                           if(tagsForLocalDevice!=null) {
                               byteArrayForTagsForLocalDevice = tagsForLocalDevice.getBytes();
                               sizeForTagsForLocalDevice = byteArrayForTagsForLocalDevice.length;
                           }

                               preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TAGS + "::MessageSize:" + sizeForTagsForLocalDevice + "::";


                           byte preamble[]=new byte[Constants.PREAMBLE_SIZE];
                           Log.d("Neighbors","Preamble string is:"+preambleString);
                           byte preambleSomething[]=preambleString.getBytes();
                           Arrays.fill(preamble,(byte)0);
                           System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);


                           ConnectedThreadWithRequestCodes newConnectedThread=new ConnectedThreadWithRequestCodes(deviceToSocket.get(device));
                           newConnectedThread.write(preamble);
                           Log.d("Neighbors","Tags for this device found from db are:"+tagsForLocalDevice);
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
                    */

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

            }
        }
    };
}
