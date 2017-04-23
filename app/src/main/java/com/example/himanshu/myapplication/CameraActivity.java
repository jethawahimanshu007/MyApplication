package com.example.himanshu.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class CameraActivity extends Activity {

    int TAKE_PHOTO_CODE = 0;
    public static int count = 0;
    SQLiteDatabase mydatabase ;
    String picturePath="";
    String file="";

    Handler handler = new Handler() {

        public void handleMessage(Message msg) {


            String aResponse = msg.getData().getString("message");

            if(aResponse.equals("Internet Issue"))
            {
                Toast.makeText(getBaseContext(), "Internet not connected, no tags will be be populated", Toast.LENGTH_SHORT).show();
            }
            else
            if ((null != aResponse)) {

                EditText tags=(EditText)findViewById(R.id.tagsTextCamera);
                String textToSet="";
                if(tags.getText().equals(""))
                    tags.setText(aResponse);
                else

                    tags.setText(tags.getText()+","+aResponse);
                // ALERT MESSAGE
                Toast.makeText(
                        getBaseContext(),
                        "The result has been populated by the API",
                        Toast.LENGTH_SHORT).show();
            }

            else{
                Toast.makeText(
                        getBaseContext(),
                        "Server response is null",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolderDtn/";
        File newdir = new File(dir);
        newdir.mkdirs();
        mydatabase= openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);;

        Button capture = (Button) findViewById(R.id.selectImageCamera);
        final EditText tags=(EditText)findViewById(R.id.tagsTextCamera);
        Button addTagsButton=(Button)findViewById(R.id.addTagsButtonCamera);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Here, the counter will be incremented each time, and the
                // picture taken by camera will be stored as 1.jpg,2.jpg
                // and likewise.

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                String format = simpleDateFormat.format(new Date());
                Log.d("CameraAc","format is:"+format);
                file = dir+"Img_"+format+".jpg";
                Log.d("CameraAc","Value of file before starting picture taking activity is:"+file);
                File newfile = new File(file);
                try {
                    newfile.createNewFile();
                }
                catch (IOException e)
                {
                }

                Uri outputFileUri = Uri.fromFile(newfile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
            }
        });
        addTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {
                if (!picturePath.equals("") && tags.getText().toString() != null && tags.getText().toString().length() > 0) {
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(getApplicationContext(), "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String tagsFromDb = getTagsFromPicturePath(picturePath);
                    LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                    Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    double latitude = 0.0;
                    double longitude = 0.0;
                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                    if (tagsFromDb != "") {
                        mydatabase.execSQL("UPDATE IMAGE_TAG_RELATION set Tags='" + tags.getText().toString() + "' where picturePath='" + picturePath + "'");
                        new DbFunctions().insertIntoTSRTbl(mydatabase, tags.getText().toString());
                    } else {
                        Log.d("CameraActivity", "Latitude and longitude values:" + latitude + "::" + longitude);
                        mydatabase.execSQL("INSERT INTO IMAGE_TAG_RELATION VALUES('" + picturePath + "','" + tags.getText().toString() + "'," + latitude + "," + longitude + "," + "(datetime('now','localtime'))" + ")");
                        new DbFunctions().insertIntoTSRTbl(mydatabase, tags.getText().toString());
                    }
                    // Toast.makeText(getApplicationContext(), tags.getText().toString()+" Will be inserted in db", Toast.LENGTH_SHORT).show();
                    //  Toast.makeText(getApplicationContext(), picturePath+" will be inserted in db", Toast.LENGTH_SHORT).show();
               /* mydatabase.execSQL("CREATE TABLE IF NOT EXISTS IMAGE_TAG_RELATION(PicturePath VARCHAR,Tags VARCHAR,PRIMARY KEY(PicturePath));");
                mydatabase.execSQL("INSERT INTO IMAGE_TAG_RELATION VALUES('"+picturePath+"','"+tags.getText().toString()+"')");
                */
                    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupCamera);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = (RadioButton) findViewById(selectedId);
                    String priority=(String)radioButton.getText();
                    int priorityLevel=0;
                    switch(priority)
                    {
                        case "High": priorityLevel=1; break;
                        case "Medium": priorityLevel=2; break;
                        case "Low": priorityLevel=3; break;
                    }

                    Cursor cursorForImages = mydatabase.rawQuery("SELECT * from IMAGE_TAG_RELATION where picturePath='" + picturePath + "'", null);
                    while (cursorForImages.moveToNext()) {

                        String imagePath = cursorForImages.getString(0);
                        String latitude1 = cursorForImages.getString(2);
                        String longitude1 = cursorForImages.getString(3);
                        String timestamp = cursorForImages.getString(4);
                        String tagsForCurrentImage = cursorForImages.getString(1);
                        String fileNameProcessing[] = imagePath.split("/");
                        String fileName = fileNameProcessing[fileNameProcessing.length - 1];
                        Log.d("GalleryTags", "fileName value is:" + fileName);
                        Log.d("GalleryTags", "fileName is:" + fileName);
                        String mime = "images/" + fileName.split("\\.")[1];
                        String format = fileName.split("\\.")[1];
                        String localMacAddr = android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
                        String localName = android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
                        String UUID = localMacAddr + "-" + fileName;

                        //Find size of the file
                        File file = new File(imagePath);
                        long sizeImage = file.length();
                        //Fina quality of image
                        BitmapFactory.Options bitMapOption=new BitmapFactory.Options();
                        bitMapOption.inJustDecodeBounds=true;
                        BitmapFactory.decodeFile(imagePath, bitMapOption);
                        int imageWidth=bitMapOption.outWidth;
                        int imageHeight=bitMapOption.outHeight;
                        long imageRes=imageWidth*imageHeight;

                        new DbFunctions().insertIntoMSGTBL(mydatabase, imagePath, latitude1, longitude1, timestamp, tagsForCurrentImage, fileName, mime, format, localMacAddr, localName, UUID,sizeImage,imageRes,priorityLevel);
                        mydatabase.execSQL("INSERT OR IGNORE INTO INCENT_FOR_MSG_TBL VALUES('"+UUID+"',0,0,0)");
                    }

                    Toast.makeText(getBaseContext(), "Tags are added", Toast.LENGTH_SHORT).show();
                    Cursor resultSet = mydatabase.rawQuery("Select * from IMAGE_TAG_RELATION where picturePath='" + picturePath + "'", null);
                    resultSet.moveToFirst();
                    String tagsFromDbNow = resultSet.getString(1);
                    String lat = resultSet.getString(2);
                    String ts = resultSet.getString(4);
                    // Toast.makeText(getApplicationContext(), lat+"::"+ts+" is found from DB", Toast.LENGTH_SHORT).show();
                    Cursor cursorForTagsForLocalDevice = null;
                    try {
                        cursorForTagsForLocalDevice = mydatabase.rawQuery("SELECT GROUP_CONCAT(Tags) from IMAGE_TAG_RELATION", null);
                    } catch (Exception e) {
                        Log.d("ConnectThread", "Exception occurred, hahahaha!");
                    }
                    /////Testing
                    try {
                        String tagsForLocalDevice = new String();


                        try {

                            if (cursorForTagsForLocalDevice != null)
                                while (cursorForTagsForLocalDevice.moveToNext()) {
                                    tagsForLocalDevice = cursorForTagsForLocalDevice.getString(0);
                                }
                            Log.d("GalleryTags", "tagsForLocalDevice are:" + tagsForLocalDevice);
                            String splitByCommas[] = tagsForLocalDevice.split(",");
                            String[] unique = new HashSet<String>(Arrays.asList(splitByCommas)).toArray(new String[0]);
                            String uniqueTags = TextUtils.join(",", unique);
                            Log.d("GalleryTags", "Distinct tags are:" + uniqueTags);
                        } catch (Exception e) {
                            Log.d("GalleryTags", "Exception occured in sqlite query!!! ::" + e);
                        }
                    } catch (Exception e) {
                        Log.d("GalleryTags", "Exception occured in fetching tags for Local device:" + e);
                    }
                    /////Testing
                }
                else{
                    if(tags.getText().toString() != null && tags.getText().toString().length() > 0)
                        Toast.makeText(getBaseContext(), "Please put a tag", Toast.LENGTH_SHORT).show();
                    else
                        if(picturePath.equals(""))
                            Toast.makeText(getBaseContext(), "Please select a picture and put a tag", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {

            Log.d("CameraDemo", "Pic saved");
            File imgFile = new  File(file);
            picturePath=imgFile.getAbsolutePath();


            Log.d("CameraActivity","Value of file is:"+file);
            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                ImageView myImage = (ImageView) findViewById(R.id.imageViewCamera);

                myImage.setImageBitmap(myBitmap);

            }
            try {


                Toast.makeText(getBaseContext(), "Please wait for keywords population", Toast.LENGTH_SHORT).show();
                String tagsFromDb=getTagsFromPicturePath(picturePath);
                if(tagsFromDb!="")
                {
                    EditText tagsEditText=(EditText)findViewById(R.id.tagsTextCamera);
                    tagsEditText.setText(tagsFromDb, TextView.BufferType.EDITABLE);
                }
                else{
                    EditText tagsEditText=(EditText)findViewById(R.id.tagsTextCamera);
                    tagsEditText.setText("", TextView.BufferType.EDITABLE);
                }
                Log.d("CameraActivity","Value of picturePath is:"+picturePath);
                getTagsForImage getTagsForImageObject=new getTagsForImage(picturePath,handler,getBaseContext());
                getTagsForImageObject.start();

            } catch (Exception e) {
                Log.d("MainActivity","Exception is thrown"+e);

            }
        }
    }

    /*
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }
    */

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
