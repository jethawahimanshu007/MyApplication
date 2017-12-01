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

//This is the activity where a selected received message can be enriched by adding more tags
public class EnrichContentMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrich_content_main);
        final String title = getIntent().getStringExtra("title");

        final Cursor cursorForImageTags=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * FROM MESSAGE_TBL where ImagePath='"+title+"'",null);
        String presentTags=new String();
        String UUID=new String();
        //Get UUID for the selected Image
        while(cursorForImageTags.moveToNext())
        {
            int inTags=cursorForImageTags.getColumnIndex("tagsForCurrentImage");
            presentTags=cursorForImageTags.getString(inTags);
            int inUUID=cursorForImageTags.getColumnIndex("UUID");
            UUID=cursorForImageTags.getString(inUUID);
        }
        String alreadyAddedTags=new String();
        String macAddress = android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
        //This is to check if there are already existing tags for this image by this device itself
        Cursor cursorSome=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from ADDED_TAGS_TBL where UUID='"+UUID+"' and MacAdd='"+macAddress+"'",null);
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

        //Following code adds more tags for the selected image in the database
        Button addNewTagsButton=(Button)findViewById(R.id.addNewTagsButton);
        addNewTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {
                EditText newTags=(EditText)findViewById(R.id.newTagsText);
                String newTagsText=newTags.getText().toString();
                Cursor cursorForImageTags=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * FROM MESSAGE_TBL where ImagePath='"+title+"'",null);
                String UUID=new String();
                while(cursorForImageTags.moveToNext())
                {
                    int inUUID=cursorForImageTags.getColumnIndex("UUID");
                    UUID=cursorForImageTags.getString(inUUID);
                }
                String macAddress = android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
                ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO ADDED_TAGS_TBL VALUES('"+UUID+"','"+newTagsText+"','"+macAddress+"',5.0)");
                ConstantsClass.mydatabaseLatest.execSQL("UPDATE ADDED_TAGS_TBL set addedTags='"+newTagsText+"' WHERE UUID='"+UUID+"' and macAdd='"+macAddress+"'");
                Toast.makeText(getBaseContext(), "New keywords are successfully added", Toast.LENGTH_SHORT).show();
            }
            });
            }


}

