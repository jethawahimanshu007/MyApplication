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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class IncentiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incentive);
        TextView tokensTV=(TextView)findViewById(R.id.tokensTV);
        final SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        Cursor incentiveCursor=mydatabase.rawQuery("SELECT * from INCENTIVES_TBL",null);
        double incentiveLeft=0.0;
        while(incentiveCursor.moveToNext())
        {
            incentiveLeft=incentiveCursor.getDouble(0);
        }
        tokensTV.setText("Total tokens left on this device are:"+/*ICDCS0*/incentiveLeft /*ICDCS*/);
        //ICDCS
        TextView Warning=(TextView)findViewById(R.id.warning);
        Warning.setText("Close to zero tokens left, participate in relaying!");
        Warning.setVisibility(View.INVISIBLE);
        if(incentiveLeft<2.0)
        {
            Warning.setVisibility(View.VISIBLE);
        }
        //ICDCS

        //Set onclick listener for dumpdb button
        /*Button dumpDBButton=(Button)findViewById(R.id.dumpDB);
        dumpDBButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dumpDB();

            }
        });*/

        Button dumpDBButton=(Button)findViewById(R.id.dumpDB);
        dumpDBButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(IncentiveActivity.this, ListViewCheckBoxesActivity.class);
                //intent.putExtra("title", item.getTitle());
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
                resetDB(mydatabase);
            }
        });


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
                final SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
                mydatabase.execSQL("Delete from TSR_REMOTE_TBL");
                mydatabase.execSQL("Delete from SENT_IMAGE_LOG");
                mydatabase.execSQL("CREATE TABLE IF NOT EXISTS ADDED_TAGS_TBL(UUID VARCHAR, addedTags VARCHAR,PRIMARY KEY(UUID))");
                Toast.makeText(getBaseContext(), backupDB.toString(),


                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }
    public void resetDB(SQLiteDatabase mydatabase)
    {
        Cursor cursorForMsgs=mydatabase.rawQuery("SELECT imagePath from MESSAGE_TBL",null);
        while(cursorForMsgs.moveToNext())
        {
            File file=new File(cursorForMsgs.getString(0));
            file.getAbsoluteFile().delete();
        }
        mydatabase.execSQL("UPDATE INCENTIVES_TBL set incentive=10");
        mydatabase.execSQL("DELETE FROM SENT_IMAGE_LOG");
        mydatabase.execSQL("DELETE FROM TSR_REMOTE_TBL");
        mydatabase.execSQL("DELETE FROM MESSAGE_TBL");
        mydatabase.execSQL("DELETE FROM INCENT_FOR_MSG_TBL");
        mydatabase.execSQL("DELETE FROM ADDED_TAGS_TBL");
        /*mydatabase.execSQL("DELETE FROM TSR_TBL");
        mydatabase.execSQL("DELETE FROM TSR_REMOTE_TBL");*/
    }
}
