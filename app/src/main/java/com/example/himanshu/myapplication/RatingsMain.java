package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import java.io.File;

import static android.R.attr.rating;

public class RatingsMain extends AppCompatActivity {

    private double rateTags=5.0;
    private double confi=5.0;
    private double mesQua=5.0;
    String UUID=new String();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings_main);

        final String title = getIntent().getStringExtra("title");

       //final SQLiteDatabase ConstantsClass.mydatabaseLatest = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        Cursor getUUIDFromTitle=ConstantsClass.mydatabaseLatest.rawQuery("SELECT UUID from MESSAGE_TBL where imagePath='"+title+"'",null);

        //Code for checking if a rating already exists for the message

        while(getUUIDFromTitle.moveToNext())
        {
            UUID=getUUIDFromTitle.getString(0);
        }
        File imgFile = new File(title);


        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView myImage = (ImageView) findViewById(R.id.imageViewReceived);
            myImage.setImageBitmap(myBitmap);

        }
        RatingBar tagsRatingBar = (RatingBar) findViewById(R.id.tagsRatingBar);
        RatingBar confiBar = (RatingBar) findViewById(R.id.confiBar);
        RatingBar mesquabar = (RatingBar) findViewById(R.id.mesquabar);

        Cursor ifUUIDExists=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from RATE_PARAMS_TBL where UUID='"+UUID+"'",null);
        if(ifUUIDExists.getCount()!=0)
        {
            ifUUIDExists.moveToNext();
            tagsRatingBar.setRating((int)ifUUIDExists.getDouble(1));
            confiBar.setRating((int)ifUUIDExists.getDouble(2));
            mesquabar.setRating((int)ifUUIDExists.getDouble(3));
        }

        tagsRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

                rateTags=rating;

            }
        });
        confiBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

                confi=rating;

            }
        });
        mesquabar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

                mesQua=rating;

            }
        });


        Button addNewTagsButton=(Button)findViewById(R.id.addRatingsButton);
        addNewTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {

                Cursor ifUUIDExists=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from RATE_PARAMS_TBL where UUID='"+UUID+"'",null);
                if(ifUUIDExists.getCount()==0)
                {
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO RATE_PARAMS_TBL VALUES('"+UUID+"',"+rateTags+","+confi+","+mesQua+")");
                }
                else
                {
                    //UUID varchar, rating REAL,confi REAL,qua
                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE RATE_PARAMS_TBL SET rating="+rateTags+",confi="+confi+",qua="+mesQua+" WHERE UUID='"+UUID+"'");
                }
                Log.d("RatingsMain","VAlues of variables rating,confi,mesQua are :"+rateTags+","+confi+","+mesQua);
                double finalRating=0.5*rateTags*(confi/5)+0.5*mesQua;
                Log.d("RatingsMain","final Rating for this message is:"+finalRating);
                Cursor numOfRowsInRatings=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from RATINGS_TBL where UUID='"+UUID+"'",null);
                if(numOfRowsInRatings.getCount()==0)
                {
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO RATINGS_TBL VALUES('"+UUID+"',"+finalRating+")");
                }
                else
                {
                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE RATINGS_TBL set rating="+finalRating+" WHERE UUID='"+UUID+"'");
                }

                //Compute blacklist for the sender of the message

                Cursor deviceAdCursor=ConstantsClass.mydatabaseLatest.rawQuery("SELECT sourceMacAddr from MESSAGE_TBL where UUID='"+UUID+"'",null);
                String deviceAd=new String();

                while(deviceAdCursor.moveToNext())
                {
                    deviceAd=deviceAdCursor.getString(0);
                }

                double average=0.0;int noOfMessages=0;

                Cursor allMessagesFromDevice=ConstantsClass.mydatabaseLatest.rawQuery("SELECT UUID from MESSAGE_TBL where sourceMacAddr='"+deviceAd+"'",null);
                int noOfRatings=0;
                while(allMessagesFromDevice.moveToNext())
                {
                    String UUIDtemp=allMessagesFromDevice.getString(0);
                    Cursor ratingsMes=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from RATINGS_TBL where UUID='"+UUIDtemp+"'",null);
                    if(!(ratingsMes.getCount()==0))
                    {
                        while(ratingsMes.moveToNext()) {
                            Log.d("RatingsMain","Value of rating for the message is:"+ratingsMes.getDouble(1));
                            average += ratingsMes.getDouble(1);
                            noOfRatings++;
                        }
                    }
                    noOfMessages++;
                }

                average=(average+5*(noOfMessages-noOfRatings))/noOfMessages;

        Log.d("RatingsMain","Value of average and noOfMessages is:"+average+"::"+noOfMessages);

                Cursor noOfEntries=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from USER_RATING_MAP_TBL where MacAd='"+deviceAd+"'",null);
                if(noOfEntries.getCount()>0)
                {
                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE USER_RATING_MAP_TBL set rating="+average+",updated_by='SELF' WHERE MacAd='"+deviceAd+"'");
                }
                else
                {
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO USER_RATING_MAP_TBL VALUES('"+deviceAd+"',"+average+",'SELF')");
                }
                /*Cursor cursorForBlackList=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from BLACKLIST_TBL where MacAd='"+deviceAd+"'",null);
                String updated_by="SELF";
                Double ratingPresent=0.0;
                int flagForExists=0;
                if(cursorForBlackList.getCount()>0)
                    flagForExists=1;
                while(cursorForBlackList.moveToNext())
                {
                    updated_by=cursorForBlackList.getString(1);
                    ratingPresent=cursorForBlackList.getDouble(2);
                }

                */

                ////Logic for blacklisting, check if it is already there in the blacklist,
                ///If it is not there, insert it with a value of self--Done code
                ///If it is there, check  if it is self or uopdated by another,
                ///If updated by self, update the value
                ///Else take the value and compute new value from it and insert
                /*
                if(average<3.5)
                {

                    if(updated_by.equals("SELF") && flagForExists==0)
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO BLACKLIST_TBL VALUES('"+deviceAd+"','SELF',"+average+")");
                    else
                        if(!updated_by.equals("SELF"));
                    {
                        average=0.2*ratingPresent+0.8*average;
                        ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO BLACKLIST_TBL VALUES('"+deviceAd+"','SELF',"+average+")");
                    }
                }
                else
                {
                    Cursor ifBlack=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from BLACKLIST_TBL where MacAd='"+deviceAd+"'",null);
                    if(ifBlack.getCount()>0)
                    {
                        ConstantsClass.mydatabaseLatest.execSQL("DELETE from BLACKLIST_TBL where MacAd='"+deviceAd+"'");
                    }
                }*/
            }
        });


    }
}
