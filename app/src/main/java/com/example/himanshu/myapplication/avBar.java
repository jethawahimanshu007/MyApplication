package com.example.himanshu.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
        implements NavigationView.OnNavigationItemSelectedListener {

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
            Manifest.permission.SYSTEM_ALERT_WINDOW

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

        final SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        ///Db table creation
        DbTableCreation dbTableCreation=new DbTableCreation();
        dbTableCreation.createTables(this);

        Intent serviceIntent = new Intent(this,BTAcceptService.class);
        String macAddress = android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
        Log.d("avBar","Mac address is:"+macAddress);


        Log.d("avBar","Context::"+this);
        this.startService(serviceIntent);





        ////Make bluetooth always discoverable

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);
        final Activity current=this;
        GeneralBTFuncs generalBTFuncs = new GeneralBTFuncs(getApplicationContext(), android.R.layout.simple_list_item_1,current , mydatabase);
        generalBTFuncs.run();

        


        setContentView(R.layout.activity_av_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        verifyStoragePermissions(this);

        Cursor cursorForRole = mydatabase.rawQuery("SELECT role from ROLE_TBL where MACAd='SELF'",null);
        int flag=0;
        ///If SI is not there in local device, insert it into TSR_TBL
        int role=0;
        while (cursorForRole.moveToNext()) {
            role = cursorForRole.getInt(0);
        }
        Log.d("avBar","The role is:"+role);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //new DbFunctions().showConnectedDevices(openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null));
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }

        gridView = (GridView) findViewById(R.id.gridViewMain);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_man, getButtons(),R.id.imageGrid1,R.id.textGrid1);
        gridView.setAdapter(gridAdapter);

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
                }
                //Create intent
               /* Intent intent = new Intent(OwnMessagesActivity.this, DetailsActivity.class);
                intent.putExtra("title", item.getTitle());*/
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
    public void dbFunction() throws Exception
    {
        //ClientAddress ca=new ClientAddress("tp","127.0.0.1");


        //H2RepoLogic h2RepoLogic = new H2RepoLogic("H2RepoLogic", ca, H2RepoLogic.STORES.PACKETS);


    }


    public void zip(String[] _files, String zipFileName) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            int BUFFER=1000;
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testImageSavingLocal()
    {
        String imagePath="/storage/emulated/0/Download/Sonny-Bryant-Junior.jpg";
        try {
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(imagePath, "r");
            byte[] b = new byte[(int) raf.length()];
            Log.d("CTwRC", "Length of byte array from image is:" + b.length);
            raf.readFully(b);
            FileOutputStream fos1 = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/testFile.jpg");
            fos1.write(b);
            fos1.close();
        }
        catch(Exception e)
        {
            Log.d("avBar","testImageSavingLocal has an exception:"+e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new DbFunctions().deleteConnectedDevices(openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null));
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        final SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_sergeant:
                if (checked) {
                    TextView tv = (TextView) findViewById(R.id.roleTextView);
                    mydatabase.execSQL("UPDATE ROLE_TBL set role=1 WHERE MACAd='SELF'");
                    RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup);
                    rg.setVisibility(View.INVISIBLE);
                    tv.setText("Hello Sergeant, please select from options below!", TextView.BufferType.EDITABLE);
                    tv.setVisibility(View.VISIBLE);
                }
                    break;
            case R.id.radio_soldier:
                if (checked) {
                    mydatabase.execSQL("UPDATE ROLE_TBL set role=2 WHERE MACAd='SELF'");
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

        return imageItems;
    }

}

/// Replace 18:3B:D2:E9:CC:9B with   18:3B:D2:E9:CC:9B
