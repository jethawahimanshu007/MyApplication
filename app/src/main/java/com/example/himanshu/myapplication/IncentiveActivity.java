package com.example.himanshu.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

public class IncentiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incentive);
        TextView tokensTV=(TextView)findViewById(R.id.tokensTV);
        //final SQLiteDatabase ConstantsClass.mydatabaseLatest = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        Cursor incentiveCursor=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from INCENTIVES_TBL",null);
        double incentiveLeft=0.0;
        DecimalFormat df=new DecimalFormat("#.####");
        while(incentiveCursor.moveToNext())
        {
            incentiveLeft=incentiveCursor.getDouble(0);
        }
        tokensTV.setText("Total tokens left on this device are:"+/*ICDCS0*/df.format(incentiveLeft) /*ICDCS*/);
        //ICDCS
        TextView Warning=(TextView)findViewById(R.id.warning);
        Warning.setText("Close to zero tokens left, participate in relaying!");
        Warning.setVisibility(View.INVISIBLE);
        if(incentiveLeft<2.0)
        {
            Warning.setVisibility(View.VISIBLE);
        }

        TextView showLastFive=(TextView)findViewById(R.id.ShowLastFive);
        String showLastFiveS=new String();
        Cursor lastFive=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from LAST_FIVE_TRANS",null);
        if(lastFive.getCount()==0)
            showLastFiveS="No transactions to show";
        while(lastFive.moveToNext())
        {
            String remoteMAC=lastFive.getString(0);
            String UUID=lastFive.getString(1);
            double paid=Double.parseDouble(df.format(lastFive.getDouble(2)));
            double received=Double.parseDouble(df.format(lastFive.getDouble(3)));
            String time=lastFive.getString(4);
            Log.d("IncentiveAc","Incentve paid and received are:"+paid+"::"+received);
            if(paid!=0.0)
            showLastFiveS+=paid+" tokens paid to "+remoteMAC+" for message "+UUID+" at time "+time+"\n\n";
            else
                showLastFiveS+=received+" tokens received from "+remoteMAC+" for message "+UUID+" at time "+time+"\n\n";
        }
        showLastFive.setText(showLastFiveS);

        //ICDCS

        //Set onclick listener for dumpdb button
        /*Button dumpDBButton=(Button)findViewById(R.id.dumpDB);
        dumpDBButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dumpDB();

            }
        });*/
/*
        Button dumpDBButton=(Button)findViewById(R.id.dumpDB);
        dumpDBButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dumpDB();
                /*Intent intent = new Intent(IncentiveActivity.this, ListViewCheckBoxesActivity.class);

                startActivity(intent);
            }
        });


        Button importDBButton=(Button)findViewById(R.id.importDB);
        importDBButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                importDB();
            }
        });

        Button resetDBButton=(Button)findViewById(R.id.resetDB);
        resetDBButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                resetDB(ConstantsClass.mydatabaseLatest);
            }
        });
*/

    }
    void dumpDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + getPackageName() + "/databases/DTNShare.db";
                String backupDBPath = "backupDb.db";

                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (!backupDB.exists()) {
                    try {
                        backupDB.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            Log.d("avBar","Exception occured in dumpDB"+e);
        }
    }
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

                Toast.makeText(getBaseContext(), backupDB.toString(),


                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }
    public void resetDB(SQLiteDatabase mydatabaseLatest)
    {
        Cursor cursorForMsgs=ConstantsClass.mydatabaseLatest.rawQuery("SELECT imagePath from MESSAGE_TBL",null);
        while(cursorForMsgs.moveToNext())
        {
            File file=new File(cursorForMsgs.getString(0));
            file.getAbsoluteFile().delete();
        }
        /*deleteDatabase(Constants.DATABASE_NAME);
        ConstantsClass.mydatabaseLatest = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        DbTableCreation dbTableCreation=new DbTableCreation();
        dbTableCreation.createTables(this);
*/

        ConstantsClass.mydatabaseLatest.execSQL("UPDATE INCENTIVES_TBL set incentive=7");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM SENT_IMAGE_LOG");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM TSR_REMOTE_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM TSR_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM MESSAGE_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM INCENT_FOR_MSG_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM ADDED_TAGS_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM LAST_FIVE_TRANS");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE from INCENT_UUID_MAC_TBL");
        String localMac=android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
        if(localMac.equals("18:3B:D2:E9:CC:9B"))
        {
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_TBL VALUES('SELF','soldier',(datetime('now','localtime')),0.9,'SELF')");
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO USER_RATING_MAP_TBL VALUES('D0:87:E2:4E:7A:2B',3.0,'SELF')");
        }
        else if(localMac.equals("18:3B:D2:EA:15:62"))
        {
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_TBL VALUES('18:3B:D2:E9:CC:9B','soldier',(datetime('now','localtime')),0.9,'SELF')");
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_REMOTE_TBL VALUES('18:3B:D2:E9:CC:9B','soldier',0.9,(datetime('now','localtime')),'SELF')");
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_TBL VALUES('SELF','isis',(datetime('now','localtime')),0.5,'SELF')");
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_TBL VALUES('SELF','enemy',(datetime('now','localtime')),0.5,'SELF')");
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_TBL VALUES('SELF','fire',(datetime('now','localtime')),0.5,'SELF')");
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_TBL VALUES('SELF','building',(datetime('now','localtime')),0.5,'SELF')");
            ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM USER_RATING_MAP_TBL");

        }
        else
        {
            ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO TSR_TBL VALUES('SELF','soldier',(datetime('now','localtime')),0.9,'SELF')");
        }

        /*ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM TSR_TBL");
        ConstantsClass.mydatabaseLatest.execSQL("DELETE FROM TSR_REMOTE_TBL");*/
    }
}
