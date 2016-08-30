package com.example.himanshu.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.ActionBarDrawerToggle;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_FINE_LOCATION = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }


    }

    ///Handler to read and write data and interaction with the services
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what)
            {
                case Constants.MESSAGE_READ:
                    Log.d("MainActivity","Message received from BTAcceptClass"+msg.getData().getString("FoundDevice"));
                    Toast toast=Toast.makeText(getApplicationContext(),msg.getData().getString("FoundDevice"), Toast.LENGTH_SHORT);
                    toast.setMargin(50,50);
                    toast.show();
                    break;
            }
            super.handleMessage(msg);
        }
        };

    private int PICK_IMAGE_REQUEST = 1;
    String selectedBTdeviceName=new String();
    String selectedBTdeviceMacAddr=new String();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*Intent intent = new Intent(this, BTAcceptService.class);
        startService(intent);
        */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        verifyStoragePermissions(this);
        BTAcceptClass btac=new BTAcceptClass(mHandler);
        btac.start();


       /* Button button = (Button) findViewById(R.id.buttonForConnect);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClientService.class);
                startService(intent);
                // Do something in response to button click
            }
        });*/
        Button fab = (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {
                //First show paired devices
                showPairedDevices(BluetoothAdapter.getDefaultAdapter());

                ListView listView = (ListView) findViewById(R.id.sampleListView);
                //To see what happens on selection of device
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View v, int position,
                                            long arg3)
                    {
                        String value = (String)adapter.getItemAtPosition(position);
                        String deviceName=value.split("\n")[0]; selectedBTdeviceName=deviceName;
                        String macAddr=value.split("\n")[1]; selectedBTdeviceMacAddr=macAddr;
                        Toast toast=Toast.makeText(getApplicationContext(),"Selected device:"+deviceName+":"+macAddr, Toast.LENGTH_SHORT);
                        toast.setMargin(50,50);
                        toast.show();

                    ///Himanshu------After this the code for alert to ask the user whether he or she wants to connect to the device will be written
                        selectImageFromGallery();

                    }
                });
            }
        });
    }

    public void selectImageFromGallery()
    {
        ///The function onActivityResult will actually handle what happens after the image is selected
        //For now, assume that a user wants to connect to the selected device
        //After an option is clicked, it will go to gallery and come back from there after an image is selected

        //Intent intent = new Intent();

        // Show only images, no videos or anything else
        //intent.setType("image/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
// Start the Intent
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);


    }


    ///function to handle the coming back after selection of an image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //This function will show the image inside the activity's imageView
        if(resultCode!=RESULT_OK)
        {
            Log.d("MainActivity","ResultCode is -1");
            Intent intent = new Intent(this, ClientService.class);
            intent.putExtra("macAddr",selectedBTdeviceMacAddr);
            startService(intent);
        }
        else
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d("MainActivity","Went into this page, hurray!");
            //uri gets the path of the image
            Uri uri = data.getData();


            String[] projection = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            Log.d("MainActivity","Cursor::"+ DatabaseUtils.dumpCursorToString(cursor));

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null

            Log.d("MainActivity","picturePath is::"+picturePath);
            cursor.close();

            ///This try block creates a bitmap from uri and then shows it in the activity
            try {

                //This is used to display the image
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                //ImageView imageView = (ImageView) findViewById(R.id.imageView);
               // imageView.setImageBitmap(bitmap);

                ///Now connection from client to server will be attempted

                ClientConnection cc=new ClientConnection(mHandler,selectedBTdeviceMacAddr);
                if(cc.newConnectThread.mmSocket!=null) {
                    Log.d("MainActivity", "Initialized socket for device:" + selectedBTdeviceMacAddr);
                }
               cc.connectToClient();
                Log.d("MainActivity","Connection to client done");
                //cc.startListening();

                java.io.RandomAccessFile raf= new java.io.RandomAccessFile(picturePath,"r");
                byte[] b = new byte[(int)raf.length()];
                Log.d("MainActivity","Length of byte array from image is:"+b.length);
                raf.readFully(b);
                Log.d("MainActivity","Size of image to be sent in byte array:"+b.length);
                String fileNameProcessing[]=picturePath.split("/");
                String fileName=fileNameProcessing[fileNameProcessing.length-1];
                String preambleString="Preamble::MessageSize:"+b.length+"::MessageFileName:"+fileName+"::";
                byte preamble[]=new byte[Constants.PREAMBLE_SIZE];
                byte preambleSomething[]=preambleString.getBytes();
                Arrays.fill(preamble,(byte)0);
                System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);
                Log.d("MainActivity","Size of preamble is:"+preamble.length);
                cc.sendImageBytes(cc.newConnectThread.mmSocket,b,preamble);
               // cc.sendImage(cc.newConnectThread.mmSocket,bitmap);

               /* Intent intent = new Intent(this, ClientService.class);
                intent.putExtra("macAddr",selectedBTdeviceMacAddr);
                intent.putExtra("uri",uri);
                Log.d("MainActivity","URI is:"+uri);
                startService(intent);*/

            } catch (Exception e) {
                Log.d("MainActivity","Exception is thrown"+e);
                //e.printStackTrace();
                /*Intent intent = new Intent(this, ClientService.class);
                intent.putExtra("macAddr",selectedBTdeviceMacAddr);
                startService(intent);
                */
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showPairedDevices(BluetoothAdapter mBluetoothAdapter)
    {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayAdapter<String> mArrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.sampleListView);

// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                /*Toast toast=Toast.makeText(getApplicationContext(),"Paired device:"+device.getName(), Toast.LENGTH_SHORT);
                toast.setMargin(50,50);
                toast.show();
*/
            }

            listView.setAdapter(mArrayAdapter);

            //Item click listener on listView

        }
    }


}



