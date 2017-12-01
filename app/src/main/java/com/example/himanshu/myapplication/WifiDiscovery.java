package com.example.himanshu.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;


//This file is used to discover devices on the same subnet!!
//This file is not used anymore because this was part of integration attempt
//but no Wifi is used to transfer to ROGER anymore, this file is still kept if the requirement needs it in the future
public class WifiDiscovery extends AppCompatActivity {

    private final String TAG = "DefaultDiscovery";
    private final static int[] DPORTS = { 139, 445, 22, 80 };
    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10; //FIXME: Test, plz set in options again ?
    private final int mRateMult = 5; // Number of alive hosts between Rate
    private int pt_move = 2; // 1=backward 2=forward
    ExecutorService mPool;
    private boolean doRateControl;
    Timer timer = new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_discovery);
        Context context=getApplicationContext();
        //final SQLiteDatabase ConstantsClass.mydatabaseLatest = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);

        final long delay=0;
        long period=5000;
        int noReceived=0;
        BroadcastReceiver mBroadcastReceiver;


        /*mBroadcastReceiver = new BroadcastReceiver() {
            int noReceived=0;
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("WifiDiscovery","Action gotten is:"+action);
                if("WIFI_DIS_DONE".equals(action)) {
                    if(noReceived==0) {
                        noReceived++;
                        Log.d("WiFiDiscovery", "Discovery done");
                        Cursor cursor = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from WIFI_NODES_TBL", null);
                        int noOfDevices = cursor.getCount();
                        Toast.makeText(context, "Discovery done", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("WIFI_DIS_DONE");  //
        mgr.registerReceiver(mBroadcastReceiver, filter);

*/

        String IpAddress=new String();
        Cursor cursorForRogerIP=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from ROGER_IP_ADD",null);
        Log.d("TSRsActivity","Number of entries in ROGER_IP_ADD table are:"+cursorForRogerIP.getCount());
        while(cursorForRogerIP.moveToNext())
        {
            IpAddress=cursorForRogerIP.getString(0);
        }
        final TextView SetIpAddress=(TextView)findViewById(R.id.SetIpAddress);
        SetIpAddress.setText("Existing Roger Ip Address:"+IpAddress);
        Button addIP=(Button)findViewById(R.id.addIP);

        addIP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String newIP=(((EditText)findViewById(R.id.ipAd)).getText().toString());
                ConstantsClass.mydatabaseLatest.execSQL("UPDATE ROGER_IP_ADD SET IpAdd='"+newIP+"'");
                //ConstantsClass.IP_ADDRESS=(((EditText)findViewById(R.id.ipAd)).getText().toString());
                SetIpAddress.setText("Set Ip Address:"+newIP);
            }
        });
        final Handler handler = new Handler() {

            public void handleMessage(Message msg) {

            }
        };
        final Button transferFiles=(Button)findViewById(R.id.TransferFiles);
        transferFiles.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ContextHandler contextHandler=new ContextHandler(getApplicationContext(),handler);
                new BackTask().execute(contextHandler);
             }
        });
        /*
        try{

            timer.schedule(new TimerTask() {

                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ConnectedDevices","Called WifiDiscovery thread");
                            try {

                                ArrayList<String> devicesInRange = new ArrayList();

                                ListView lv = (ListView) findViewById(R.id.wifiDevicesList);

                                Cursor cursorForNearbyDevices = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from WIFI_NODES_TBL", null);

                                while (cursorForNearbyDevices.moveToNext()) {
                                    //Log.d("ConnectedDevices", "There are devices in range");
                                    String IpAddress = cursorForNearbyDevices.getString(0);
                                    Log.d("ConnectedDevices", "IpAddress found:" + IpAddress);
                                    devicesInRange.add(IpAddress);
                                }

                                Log.d("ConnectedDevices","Array devicesinRange is:"+ Arrays.toString(devicesInRange.toArray()));

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                                        android.R.layout.simple_list_item_1, android.R.id.text1, devicesInRange);
                                lv.setAdapter(adapter);

                                lv.invalidateViews();

                            }
                            catch(Exception e){Log.d("ConnectedDevices","Exception in connectedDevices:"+e);}
                        }

                    });

                }}, delay,period);

        }
        catch(Exception e)
        {

        }
        */
    }
}
