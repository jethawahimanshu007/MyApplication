package com.example.himanshu.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


//import com.ibm.watson.developer_cloud.visual_recognition.v3;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

//The functioning of this class is exactly similar to that of CameraActivity
public class GalleryTags extends Activity {

    private int PICK_IMAGE_REQUEST = 1;
    String picturePath="";
    //SQLiteDatabase ConstantsClass.mydatabaseLatest;
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {


            String aResponse = msg.getData().getString("message");

            if(aResponse.equals("Internet Issue"))
            {
                Toast.makeText(getBaseContext(), "Internet not connected, no tags will be be populated", Toast.LENGTH_SHORT).show();
            }
            else
            if ((null != aResponse)) {

                EditText tags=(EditText)findViewById(R.id.tagsText);
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_tags);
        //ConstantsClass.mydatabaseLatest = openOrCreateDatabase(Constants.DATABASE_NAME,MODE_PRIVATE,null);

        Button selectImage = (Button) findViewById(R.id.selectImage);

        final EditText tags=(EditText)findViewById(R.id.tagsText);
        Button addTagsButton=(Button)findViewById(R.id.addTagsButton);





        /*BluetoothDevice device= BluetoothAdapter.getDefaultAdapter().getRemoteDevice("D0:87:E2:4E:7A:2B");
        ConnectThread newConnectThread=new ConnectThread(device,this);
        Log.d("Neighbors","value of socket is:"+newConnectThread.mmSocket);
        newConnectThread.start();
        String imagePath="/storage/emulated/0/Download/Sonny-Bryant-Junior.jpg";
        try {
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(imagePath, "r");
            byte[] b = new byte[(int) raf.length()];
            Log.d("CTwRC", "Length of byte array from image is:" + b.length);
            raf.readFully(b);
            Log.d("avBar","Number of bytes of Sonny Bryant is:"+b.length);
        }
        catch(Exception e)
        {
            Log.d("CT","Error sending Sonny-bryant-junior:"+e);
        }

        */

        selectImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                selectImageFromGallery();


            }
        });


        addTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {
                if (!picturePath.equals("") && tags.getText().toString()!=null && tags.getText().toString().length()>0) {
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(getApplicationContext(), "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
                        //return;
                    }
                    String tagsFromDb = getTagsFromPicturePath(picturePath);
                    LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                    Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    double longitude = 0.0, latitude = 0.0;
                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                    if (tagsFromDb != "") {
                        ConstantsClass.mydatabaseLatest.execSQL("UPDATE IMAGE_TAG_RELATION set Tags='" + tags.getText().toString() + "' where picturePath='" + picturePath + "'");
                        new DbFunctions().insertIntoTSRTbl(ConstantsClass.mydatabaseLatest, tags.getText().toString());
                    } else {
                        ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO IMAGE_TAG_RELATION VALUES('" + picturePath + "','" + tags.getText().toString() + "'," + latitude + "," + longitude + "," + "(datetime('now','localtime'))" + ")");

                        new DbFunctions().insertIntoTSRTbl(ConstantsClass.mydatabaseLatest, tags.getText().toString());
                    }

                    //Get the priorities

                    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupGal);
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

                    ////Create message
                    Cursor cursorForImages = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from IMAGE_TAG_RELATION where picturePath='" + picturePath + "'", null);
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
                        new DbFunctions().insertIntoMSGTBL(ConstantsClass.mydatabaseLatest, imagePath, latitude1, longitude1, timestamp, tagsForCurrentImage, fileName, mime, format, localMacAddr, localName, UUID,sizeImage,imageRes,priorityLevel);

                        ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO INCENT_FOR_MSG_TBL VALUES('"+UUID+"',0,0,0)");
                    }
                    Toast.makeText(getBaseContext(), "Tags are added", Toast.LENGTH_SHORT).show();
                    ContextHandler contextHandler=new ContextHandler(getApplicationContext(),handler);
                    new BackTask().execute(contextHandler);


                    // Toast.makeText(getApplicationContext(), tags.getText().toString()+" Will be inserted in db", Toast.LENGTH_SHORT).show();
                    //  Toast.makeText(getApplicationContext(), picturePath+" will be inserted in db", Toast.LENGTH_SHORT).show();
               /* ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS IMAGE_TAG_RELATION(PicturePath VARCHAR,Tags VARCHAR,PRIMARY KEY(PicturePath));");
                ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO IMAGE_TAG_RELATION VALUES('"+picturePath+"','"+tags.getText().toString()+"')");
                */


                    Cursor resultSet = ConstantsClass.mydatabaseLatest.rawQuery("Select * from IMAGE_TAG_RELATION where picturePath='" + picturePath + "'", null);
                    resultSet.moveToFirst();
                    String tagsFromDbNow = resultSet.getString(1);
                    String lat = resultSet.getString(2);
                    String ts = resultSet.getString(4);
                    // Toast.makeText(getApplicationContext(), lat+"::"+ts+" is found from DB", Toast.LENGTH_SHORT).show();
                    Cursor cursorForTagsForLocalDevice = null;
                    try {
                        cursorForTagsForLocalDevice = ConstantsClass.mydatabaseLatest.rawQuery("SELECT GROUP_CONCAT(Tags) from IMAGE_TAG_RELATION", null);
                    } catch (Exception e) {
                        Log.d("ConnectThread", "Exception occurred, hahahaha!");
                    }
                    Cursor cursorForAllMessages = ConstantsClass.mydatabaseLatest.rawQuery("SELECT GROUP_CONCAT(imagePath) from MESSAGE_TBL", null);
                    cursorForAllMessages.moveToFirst();
                    Log.d("GalleryTags", "Image paths in MESSAGE_TBL are:" + cursorForAllMessages.getString(0));
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
                Toast.makeText(getBaseContext(), "Please wait for keywords population", Toast.LENGTH_SHORT).show();
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
                getTagsForImage getTagsForImageObject=new getTagsForImage(picturePath,handler,getBaseContext());
                getTagsForImageObject.start();

            } catch (Exception e) {
                Log.d("MainActivity","Exception is thrown"+e);

            }
        }
       // VisualRecognition service = new VisualRecognition(VisualRecognition);
        //service.setApiKey("{api-key}");


    }

    public String getTagsFromPicturePath(String picturePathInput)
    {
        String tagsFromDb="";
        try {
            Cursor resultSet = ConstantsClass.mydatabaseLatest.rawQuery("Select Tags from IMAGE_TAG_RELATION where PicturePath='" + picturePathInput + "'", null);
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
    public void onDestroy() {
        super.onDestroy();
        //ConstantsClass.mydatabaseLatest.close();
    }
}
