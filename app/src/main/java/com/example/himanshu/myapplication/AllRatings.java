package com.example.himanshu.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
//This file shows a list of devices on the path of a message

public class AllRatings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_ratings);
        try{
            ArrayList<String> sourceList = new ArrayList();
            final String title = getIntent().getStringExtra("title");
            String UUID=new String();
            String sourceMac=new String();

            //For source
            Cursor getUUIDFromTitle=ConstantsClass.mydatabaseLatest.rawQuery("SELECT UUID,sourceMacAddr from MESSAGE_TBL where imagePath='"+title+"'",null);
            while(getUUIDFromTitle.moveToNext())
            {
                UUID=getUUIDFromTitle.getString(0);
                sourceMac=getUUIDFromTitle.getString(1);
            }
            final  String UUID1=UUID;
            ListView lv = (ListView) findViewById(R.id.source);
            ArrayList sourceArrayList=new ArrayList<String>();
            sourceArrayList.add(sourceMac);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, sourceArrayList);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    Intent intent = new Intent(AllRatings.this, RatingsMain.class);
                    intent.putExtra("title", title);
                    startActivity(intent);
                }
            });


            //For relays
            Cursor cursorForRelays = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from ADDED_TAGS_TBL where UUID='"+UUID+"'", null);

            ArrayList<String> relayArrayList = new ArrayList();

            if(cursorForRelays.getCount()!=0) {
                while (cursorForRelays.moveToNext()) {
                    relayArrayList.add(cursorForRelays.getString(2));
                }
            }
            else{
                relayArrayList.add("No devices enriched this message");
            }

            final ListView lv1 = (ListView) findViewById(R.id.relayList);

            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getBaseContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, relayArrayList);
            lv1.setAdapter(adapter1);

            lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    String relayMac =(String) parent.getItemAtPosition(position);
                    Intent intent = new Intent(AllRatings.this, RelayRate.class);
                    intent.putExtra("MAC", relayMac);
                    intent.putExtra("UUID",UUID1);
                    startActivity(intent);
                }
            });


        }
        catch(Exception e){
            Log.d("ConnectedDevices","Exception in connectedDevices:"+e);}
    }

}

