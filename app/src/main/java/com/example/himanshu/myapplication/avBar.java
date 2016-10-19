package com.example.himanshu.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;





public class avBar extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_FINE_LOCATION = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET

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
        Intent serviceIntent = new Intent(this,BTAcceptService.class);

        Log.d("avBar","Context::"+this);
        this.startService(serviceIntent);

        Log.d("avBar","Saving locally a testImage");
        testImageSavingLocal();
        Log.d("avBar","function testImageSavingLocal returned");
        String paths[]=new String[1];
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
        try {
            archiver.extract(new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/some.tar.gz"), new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/some"));
        }
        catch(Exception e)
        {

        }

        try {
            dbFunction();
        }
        catch(Exception e)
        {
            Log.d("avBar","Exception has occured in avBar while calling dbFunction:"+e);
        }



        Log.d("avBar","The Mac address of my device is:"+BluetoothAdapter.getDefaultAdapter().getAddress());

        BluetoothDevice device= BluetoothAdapter.getDefaultAdapter().getRemoteDevice("D0:87:E2:4E:7A:2B");
        //BluetoothDevice device1= BluetoothAdapter.getDefaultAdapter().getRemoteDevice("28:BA:B5:B5:82:8F");
        if(device==null)
        {
            Log.d("Neighbors","Device HimanshuTablet is null");
            System.exit(1);
        }



        //if(device1==null)
        //{
          //  Log.d("Neighbors","Device Nejav is null");
           // System.exit(1);
        //}
        //ConnectThread newConnectThread1=new ConnectThread(device1,this);
        //Log.d("Neighbors","value of socket is:"+newConnectThread1.mmSocket);
        //newConnectThread1.start();

        ///Db table creation
        DbTableCreation dbTableCreation=new DbTableCreation();
        dbTableCreation.createTables(this);
        setContentView(R.layout.activity_av_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        verifyStoragePermissions(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        new DbFunctions().showConnectedDevices(openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null));
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }



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
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, GalleryTags.class);
            startActivity(intent);
        } else if (id == R.id.nav_neighbors) {
            Intent intent = new Intent(this, Neighbors.class);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(this, ShowReceivedImages.class);
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
}

