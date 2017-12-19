package com.example.himanshu.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.pddstudio.talking.Talk;
import com.pddstudio.talking.model.SpeechObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;





public class avBar extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,Talk.Callback{

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    // Storage Permissions
    public  static Activity activityMain;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_FINE_LOCATION = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO

    };

    public static void verifyStoragePermissions(Activity activity) {

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_av_bar);

        //When the app is started, initialize the database
        ConstantsClass.mydatabaseLatest=openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        //Create all tables in the database
        DbTableCreation dbTableCreation=new DbTableCreation();
        dbTableCreation.createTables(this);

        //Speech part---initailizethe talk object
        Talk.init(this, this);
        Talk.getInstance().addSpeechObjects(helloObject);

        //Start the BTaccept service
        Intent serviceIntent = new Intent(this,BTAcceptService.class);
        String macAddress = android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
        this.startService(serviceIntent);

        ////Make bluetooth always discoverable
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);
        final Activity current=this;
        //This is to start the discovery of devices
        GeneralBTFuncs generalBTFuncs = new GeneralBTFuncs(getApplicationContext(), android.R.layout.simple_list_item_1,current , ConstantsClass.mydatabaseLatest);
        generalBTFuncs.run();
        //Set the view of this activity
        setContentView(R.layout.activity_av_bar);
        //Initialize the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Verify the permissions-required for android versions above Marshmellow
        verifyStoragePermissions(this);

        //This is to show the role on the home page
        Cursor cursorForRole = ConstantsClass.mydatabaseLatest.rawQuery("SELECT role from ROLE_TBL where MACAd='SELF'",null);
        int role=0;
        while (cursorForRole.moveToNext()) {
            role = cursorForRole.getInt(0);
        }
        Log.d("avBar","The role is:"+role);
        //Manage the radio buttons for Solider and Sergeant!
        //Show the radio buttons only if the role is not already selected, otherwise show the radio buttons
        if(role!=0)
        {
            RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup);
            rg.setVisibility(View.INVISIBLE);
            TextView tv = (TextView) findViewById(R.id.roleTextView);
            if(role==1)
            {
                tv.setText("Hello Sergeant, please select from options below!", TextView.BufferType.EDITABLE);
                tv.setVisibility(View.VISIBLE);
            }
            else if(role==2)
            {
                tv.setText("Hello Soldier, please select from options below!", TextView.BufferType.EDITABLE);
                tv.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup);
            rg.setVisibility(View.VISIBLE);
        }
        //Handle the navigation view
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Check if the permissions are set or not
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }

        gridView = (GridView) findViewById(R.id.gridViewMain);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_man, getButtons(),R.id.imageGrid1,R.id.textGrid1);
        gridView.setAdapter(gridAdapter);
        //Start different activity based on what button is selected
       gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                String title=item.getTitle();
                Intent intent=new Intent(avBar.this, avBar.class);
                switch(title)
                {
                    case "Gallery":intent = new Intent(avBar.this, GalleryTags.class);
                        break;
                    case "Camera":intent = new Intent(avBar.this, CameraActivity.class);
                        break;
                    case "Neighbors":intent = new Intent(avBar.this, ConnectedDevices.class);
                        break;
                    case "Interests":intent = new Intent(avBar.this, TSRsActivity.class);
                        break;
                    case "Add interests":intent = new Intent(avBar.this, AddKeywords.class);
                        break;
                    case "Incentives":intent = new Intent(avBar.this, IncentiveActivity.class);
                        break;
                    case "Navigate":intent = new Intent(avBar.this, NavigateActivity.class);
                        break;
                    case "Saved Messages":intent = new Intent(avBar.this, OwnMessagesActivity.class);
                        break;
                    case "Inbox":intent = new Intent(avBar.this, ShowReceivedImages.class);
                        break;
                    case "Enrich":intent = new Intent(avBar.this, EnrichContent.class);
                    break;
                    case "Add Ratings":intent = new Intent(avBar.this, Ratings.class);
                        break;
                    case "Ratings": intent = new Intent(avBar.this, ShowRatings.class);
                        break;
                    case "Marine": intent=new Intent(avBar.this,WifiDiscovery.class);
                        break;
                }
                startActivity(intent);

           }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.av_bar, menu);
        return true;
    }

    //This function is used to handle the options SET, RESET DB, Settings and Speech assist button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.set_db_home) {
            Toast.makeText(this, "DB has been set for demo", Toast.LENGTH_LONG).show();
            importDB();
            return true;
        }
        if(id== R.id.reset_db_home)
        {
            Toast.makeText(this, "DB has been reset for demo", Toast.LENGTH_LONG).show();
            resetDB(ConstantsClass.mydatabaseLatest);
            return true;
        }
        if(id==R.id.speech_assist)
        {
            Talk.getInstance().startListening();
        }
        if(id==R.id.settings)
        {
            //Intent intent = new Intent(this, Settings.class);
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    //This function is used to hande the click event from the navigation on the left of the home page
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, GalleryTags.class);
            startActivity(intent);
        } else if (id == R.id.nav_neighbors) {
            Intent intent = new Intent(this, ConnectedDevices.class);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, AddKeywords.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(this, ShowReceivedImages.class);
            startActivity(intent);
        }
        else if (id == R.id.TSRsAct) {
            Intent intent = new Intent(this, TSRsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.OwnMessagesAct) {
            Intent intent = new Intent(this, OwnMessagesActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.IncentiveAct)
        {
            Intent intent = new Intent(this, IncentiveActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.addMoreTags)
        {
            Intent intent = new Intent(this, EnrichContent.class);
            startActivity(intent);
        }
        else if(id==R.id.nav_navigate)
        {
            Intent intent = new Intent(this, NavigateActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.nav_ratings)
        {
            Intent intent = new Intent(this, NavigateActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.nav_wifi_dis)
        {
            Intent intent = new Intent(this, WifiDiscovery.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new DbFunctions().deleteConnectedDevices(openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null));
    }

    //This function handles the radio button click, if a button is clicked, hide the radio buttons and show the text
    //according to whether Sergeant or Soldier is selected
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_sergeant:
                if (checked) {
                    TextView tv = (TextView) findViewById(R.id.roleTextView);
                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE ROLE_TBL set role=1 WHERE MACAd='SELF'");
                    RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup);
                    rg.setVisibility(View.INVISIBLE);
                    tv.setText("Hello Sergeant, please select from options below!", TextView.BufferType.EDITABLE);
                    tv.setVisibility(View.VISIBLE);
                }
                    break;
            case R.id.radio_soldier:
                if (checked) {
                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE ROLE_TBL set role=2 WHERE MACAd='SELF'");
                    RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup);
                    rg.setVisibility(View.INVISIBLE);
                    TextView tv = (TextView) findViewById(R.id.roleTextView);
                    tv.setText("Hello Soldier, please select from options below!", TextView.BufferType.EDITABLE);
                    tv.setVisibility(View.VISIBLE);
                }
                    break;
        }
    }

    public static String DB_FILEPATH = "/data/data/{package_name}/databases/database.db";

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     * */
    void dumpDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + getPackageName() + "/databases/DTNShare.db";
                String backupDBPath = "/storage/emulated/0/backupDb.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            Log.d("avBar","Exception occured in dumpDB");
        }
    }

    //This is to set icons for the grid View menu on the homepage
    private ArrayList<ImageItem> getButtons() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.gallerybigg);
        imageItems.add(new ImageItem(bm,"Gallery"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.camerabig);
        imageItems.add(new ImageItem(bm,"Camera"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.neighborsbig);
        imageItems.add(new ImageItem(bm,"Neighbors"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.listbig);
        imageItems.add(new ImageItem(bm,"Interests"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.listaddbig);
        imageItems.add(new ImageItem(bm,"Add interests"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.briefcasebig);
        imageItems.add(new ImageItem(bm,"Incentives"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.navigatebig);
        imageItems.add(new ImageItem(bm,"Navigate"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.savedmessagebig);
        imageItems.add(new ImageItem(bm,"Saved Messages"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.inboxbig);
        imageItems.add(new ImageItem(bm,"Inbox"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.enrichbig);
        imageItems.add(new ImageItem(bm,"Enrich"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.addrating);
        imageItems.add(new ImageItem(bm,"Add Ratings"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.ratingsbig);
        imageItems.add(new ImageItem(bm,"Ratings"));
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.marinebig);
        imageItems.add(new ImageItem(bm,"Marine"));

        return imageItems;
    }

    //DEMO-only function
    // This is called when Set DB is pressed-- it replaces the database for the app with a pre defined file in the file system
    private void importDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            //SQLiteDatabase ConstantsClass.mydatabaseLatest1 = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
            Cursor cursorForRogerIP=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from ROGER_IP_ADD",null);
            String IpAddress=new String();
            while(cursorForRogerIP.moveToNext())
            {
                IpAddress=cursorForRogerIP.getString(0);
            }

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + getPackageName()
                        + "//databases//" + "DTNShare.db";
                String backupDBPath = "backupDb.db";
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                //final SQLiteDatabase ConstantsClass.mydatabaseLatest = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);

                ConstantsClass.mydatabaseLatest.execSQL("Delete from TSR_REMOTE_TBL");
                ConstantsClass.mydatabaseLatest.execSQL("Delete from SENT_IMAGE_LOG");
                ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS ADDED_TAGS_TBL");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS ADDED_TAGS_TBL(UUID VARCHAR, addedTags VARCHAR, MacAdd VARCHAR,rating REAL,PRIMARY KEY(UUID,MacAdd))");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS LAST_FIVE_TRANS(RemoteMAC VARCHAR, UUID VARCHAR, paid double, received double, time TIMESTAMP )");/*, time DEFAULT datetime('now','localtime')*/
                ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS INCENT_UUID_MAC_TBL");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS INCENT_UUID_MAC_TBL(MacAd VARCHAR,UUID VARCHAR,incentive real,flag integer,PRIMARY KEY(MacAd,UUID))");
                ConstantsClass.mydatabaseLatest.execSQL("DROP TABLE IF EXISTS RATINGS_TBL");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS RATINGS_TBL(UUID VARCHAR, rating REAL)");
                ConstantsClass.mydatabaseLatest.execSQL("UPDATE INCENTIVES_TBL SET incentive=7");

                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS ROLE_TBL(role INTEGER, MACAd VARCHAR,PRIMARY KEY(MACAd))");
                ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO ROLE_TBL VALUES(0,'SELF')");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS MAC_RSSI_TBL(MacAd VARCHAR,RSSI VARCHAR)");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS INCENT_UUID_MAC_TBL(MacAd VARCHAR,UUID VARCHAR,incentive real,flag integer,PRIMARY KEY(MacAd,UUID))");// flag represents if the node is  dest or relay greater than threshold
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS TSR_SHARE_DONE_TBL(MacAd VARCHAR, doneOrNor INTEGER)");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS LAST_FIVE_TRANS(RemoteMAC VARCHAR, UUID VARCHAR, paid double, received double, time TIMESTAMP )");/*, time DEFAULT datetime('now','localtime')*/
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS RATINGS_TBL(UUID VARCHAR, rating REAL)");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS USER_RATING_MAP_TBL(MacAd VARCHAR,rating REAL,updated_by VARCHAR)");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS ROGER_IP_ADD(MacAd VARCHAR, PRIMARY KEY(MacAd))");
                ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO ROGER_IP_ADD VALUES('"+IpAddress+"')");
                ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS RATE_PARAMS_TBL(UUID varchar, rating REAL,confi REAL,qua REAL, PRIMARY KEY(UUID))");
                //Toast.makeText(getBaseContext(), backupDB.toString(),


                   //     Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    //Demo only function-- no use other than Demo
    //This function is used to reset the DB
    public void resetDB(SQLiteDatabase mydatabaseLatest)
    {
        Cursor cursorForMsgs=ConstantsClass.mydatabaseLatest.rawQuery("SELECT imagePath from MESSAGE_TBL",null);
        while(cursorForMsgs.moveToNext())
        {
            File file=new File(cursorForMsgs.getString(0));
            file.getAbsoluteFile().delete();
        }

        ConstantsClass.mydatabaseLatest.execSQL("UPDATE INCENTIVES_TBL set incentive=300");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM SENT_IMAGE_LOG");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM TSR_REMOTE_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM TSR_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM MESSAGE_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM INCENT_FOR_MSG_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM ADDED_TAGS_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM LAST_FIVE_TRANS");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE from INCENT_UUID_MAC_TBL");
        String localMac=android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
    }
    public void onStartListening() {
        Toast.makeText(getApplicationContext(), "Started Listening", Toast.LENGTH_LONG).show();
    }
    public void onFinishedListening(SpeechObject speechObject) {
        Toast.makeText(getApplicationContext(), "Stopped Listening", Toast.LENGTH_LONG).show();
        if(speechObject != null) {
            speechObject.onSpeechObjectIdentified();
        }
    }
    private SpeechObject helloObject = new SpeechObject() {
        @Override
        public void onSpeechObjectIdentified() {
            Toast.makeText(getApplicationContext(), "Found the object", Toast.LENGTH_LONG).show();
            Intent enemiesIntent=new Intent(getApplicationContext(),EnemiesWithinAnArea.class);
            startActivity(enemiesIntent);


        }

        @Override
        public String getVoiceString() {
            return "Find all enemies near me";
        }
    };
    public void onFailedListening(int errorCode) {
        Toast.makeText(this, "Stopped Listening!!", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onRmsChanged(float rms) {

    }
}

