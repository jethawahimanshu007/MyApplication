package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class GalleryTags extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    String picturePath="";
    SQLiteDatabase mydatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_tags);
        mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null);

        Button selectImage = (Button) findViewById(R.id.selectImage);

        final EditText tags=(EditText)findViewById(R.id.tagsText);
        Button addTagsButton=(Button)findViewById(R.id.addTagsButton);


        selectImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });


        addTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {
                String tagsFromDb=getTagsFromPicturePath(picturePath);
                if(tagsFromDb!="")
                {
                    mydatabase.execSQL("UPDATE IMAGE_TAG_RELATION set Tags='"+tags.getText().toString()+"' where picturePath='"+picturePath+"'");
                }
                else
                {
                    mydatabase.execSQL("INSERT INTO IMAGE_TAG_RELATION VALUES('"+picturePath+"','"+tags.getText().toString()+"')");
                }
               // Toast.makeText(getApplicationContext(), tags.getText().toString()+" Will be inserted in db", Toast.LENGTH_SHORT).show();
              //  Toast.makeText(getApplicationContext(), picturePath+" will be inserted in db", Toast.LENGTH_SHORT).show();
               /* mydatabase.execSQL("CREATE TABLE IF NOT EXISTS IMAGE_TAG_RELATION(PicturePath VARCHAR,Tags VARCHAR,PRIMARY KEY(PicturePath));");
                mydatabase.execSQL("INSERT INTO IMAGE_TAG_RELATION VALUES('"+picturePath+"','"+tags.getText().toString()+"')");
                */

                Cursor resultSet = mydatabase.rawQuery("Select * from IMAGE_TAG_RELATION where picturePath='"+picturePath+"'",null);
                resultSet.moveToFirst();
                String tagsFromDbNow = resultSet.getString(1);
                Toast.makeText(getApplicationContext(), tagsFromDbNow+" is found from DB", Toast.LENGTH_SHORT).show();

            }
        });

    }



    public void selectImageFromGallery()
    {

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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d("MainActivity","Went into this page, hurray!");
            //uri gets the path of the image
            Uri uri = data.getData();


            String[] projection = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            Log.d("MainActivity","Cursor::"+ DatabaseUtils.dumpCursorToString(cursor));

            int columnIndex = cursor.getColumnIndex(projection[0]);
             picturePath = cursor.getString(columnIndex); // returns null

            Log.d("MainActivity","picturePath is::"+picturePath);
            cursor.close();

            ///This try block creates a bitmap from uri and then shows it in the activity
            try {

                //This is used to display the image
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageView imageView = (ImageView) findViewById(R.id.imageViewGallery);
                imageView.setImageBitmap(bitmap);
                String tagsFromDb=getTagsFromPicturePath(picturePath);
                if(tagsFromDb!="")
                {
                    EditText tagsEditText=(EditText)findViewById(R.id.tagsText);
                    tagsEditText.setText(tagsFromDb, TextView.BufferType.EDITABLE);
                }
                else{
                    EditText tagsEditText=(EditText)findViewById(R.id.tagsText);
                    tagsEditText.setText("", TextView.BufferType.EDITABLE);
                }

            } catch (Exception e) {
                Log.d("MainActivity","Exception is thrown"+e);

            }
        }

    }

    public String getTagsFromPicturePath(String picturePathInput)
    {
        String tagsFromDb="";
        try {
            Cursor resultSet = mydatabase.rawQuery("Select Tags from IMAGE_TAG_RELATION where PicturePath='" + picturePathInput + "'", null);
            resultSet.moveToFirst();
             tagsFromDb = resultSet.getString(0);
            return tagsFromDb;
        }
        catch(Exception e)
        {
            Log.d("GalleryTags","Maybe no data from query");
            return tagsFromDb;
        }
    }
}
