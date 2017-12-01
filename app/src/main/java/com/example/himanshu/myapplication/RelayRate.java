package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

//This activity is used to rate a relay for its added tags
public class RelayRate extends AppCompatActivity {
    double rateTags=5.0;
    private double confi=5.0;
    private double mesQua=5.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay_rate);
        final String MAC = getIntent().getStringExtra("MAC");
        final String UUID=getIntent().getStringExtra("UUID");
        String addedTag=new String();

        //Functionality for the textView
        Cursor cursorForAddedTag=ConstantsClass.mydatabaseLatest.rawQuery("SELECT addedTags from ADDED_TAGS_TBL where UUID='"+UUID+
                "' and MacAdd='"+MAC+"'",null);
        while(cursorForAddedTag.moveToNext())
        {
            addedTag=cursorForAddedTag.getString(0);
        }
        TextView tv=(TextView)findViewById(R.id.addedTagsRelay);
        tv.setText("Added tags are:"+addedTag);


        ///Code for adding rating by button click
        RatingBar tagsRatingBar = (RatingBar) findViewById(R.id.tagsRatingBarRelay);
        RatingBar confiBar = (RatingBar) findViewById(R.id.confiBarRelay);

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


        Button addNewTagsButton=(Button)findViewById(R.id.addRatingsButtonRelay);
        addNewTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {

                //Algo 1 for message relay rating

                double finalRating=0.5*rateTags*(confi/5)+0.5*mesQua;
                Log.d("RatingsMain","final Rating for this message is:"+finalRating);

                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE ADDED_TAGS_TBL SET rating="+finalRating+" WHERE UUID='"+UUID+"' and " +
                            "MacAdd='"+MAC+"'" );




                double average=0.0;int noOfMessages=0;
                ////Algo for device rating
                Cursor allMessagesFromDevice=ConstantsClass.mydatabaseLatest.rawQuery("SELECT UUID from MESSAGE_TBL where sourceMacAddr='"+MAC+"'",null);
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
                noOfMessages++;
                noOfRatings++;
                average+=finalRating;
                average=(average+5*(noOfMessages-noOfRatings))/noOfMessages;

                Log.d("RatingsMain","Value of average and noOfMessages is:"+average+"::"+noOfMessages);

                Cursor noOfEntries=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from USER_RATING_MAP_TBL where MacAd='"+MAC+"'",null);
                if(noOfEntries.getCount()>0)
                {
                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE USER_RATING_MAP_TBL set rating="+average+",updated_by='SELF' WHERE MacAd='"+MAC+"'");
                }
                else
                {
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT INTO USER_RATING_MAP_TBL VALUES('"+MAC+"',"+average+",'SELF')");
                }

            }
        });
    }
}
