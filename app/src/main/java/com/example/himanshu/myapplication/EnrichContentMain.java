package com.example.himanshu.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;

public class EnrichContentMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrich_content_main);
        final String title = getIntent().getStringExtra("title");
        final SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null);
        final Cursor cursorForImageTags=mydatabase.rawQuery("SELECT * FROM MESSAGE_TBL where ImagePath='"+title+"'",null);
        String presentTags=new String();
        String UUID=new String();
        while(cursorForImageTags.moveToNext())
        {
            int inTags=cursorForImageTags.getColumnIndex("tagsForCurrentImage");
            presentTags=cursorForImageTags.getString(inTags);
            int inUUID=cursorForImageTags.getColumnIndex("UUID");
            UUID=cursorForImageTags.getString(inUUID);
        }
        String alreadyAddedTags=new String();
        Cursor cursorSome=mydatabase.rawQuery("SELECT * from ADDED_TAGS_TBL where UUID='"+UUID+"'",null);
        while(cursorSome.moveToNext())
        {
            alreadyAddedTags=cursorSome.getString(1);
        }
        EditText newTags=(EditText)findViewById(R.id.newTagsText);
        newTags.setText(alreadyAddedTags);
        TextView tv=(TextView)findViewById(R.id.presentTags);
        tv.setText("Already present tags:"+presentTags);
        File imgFile = new File(title);

        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView myImage = (ImageView) findViewById(R.id.imageViewReceived);
            myImage.setImageBitmap(myBitmap);

        }

        Button addNewTagsButton=(Button)findViewById(R.id.addNewTagsButton);
        addNewTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {
                EditText newTags=(EditText)findViewById(R.id.newTagsText);
                String newTagsText=newTags.getText().toString();
                Cursor cursorForImageTags=mydatabase.rawQuery("SELECT * FROM MESSAGE_TBL where ImagePath='"+title+"'",null);
                String UUID=new String();
                while(cursorForImageTags.moveToNext())
                {
                    int inUUID=cursorForImageTags.getColumnIndex("UUID");
                    UUID=cursorForImageTags.getString(inUUID);
                }
                mydatabase.execSQL("INSERT OR IGNORE INTO ADDED_TAGS_TBL VALUES('"+UUID+"','"+newTagsText+"')");
                mydatabase.execSQL("UPDATE ADDED_TAGS_TBL set addedTags='"+newTagsText+"' WHERE UUID='"+UUID+"'");
                Toast.makeText(getBaseContext(), "New keywords are successfully added", Toast.LENGTH_SHORT).show();
            }
            });

       /* Button goToLocButton=(Button)findViewById(R.id.goToLoc);
        goToLocButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            ///Action for button
            public void onClick(View view) {
                Cursor cursorForLocation=mydatabase.rawQuery("SELECT * FROM MESSAGE_TBL where ImagePath='"+title+"'",null);
                LatLng closest; double latitude=0.0, longitude=0.0;
                while(cursorForLocation.moveToNext())
                {
                    int latIn=cursorForLocation.getColumnIndex("latitude");
                    int longIn=cursorForLocation.getColumnIndex("longitude");
                    latitude=cursorForLocation.getDouble(latIn);
                    longitude=cursorForLocation.getDouble(longIn);
                }
                closest=new LatLng(latitude,longitude);
                DestinationReachThread dt = new DestinationReachThread(getApplicationContext(), closest, closest,"The location you requested",0);
                dt.run();
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + closest.latitude + "," + closest.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });*/
            }


}

