package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import javax.crypto.Mac;

public class ShowRatings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ratings);
        final SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        Cursor cursorForRatings=mydatabase.rawQuery("SELECT * from USER_RATING_MAP_TBL",null);
        ArrayList<String> RatingStrings=new ArrayList<>();
        final ListView lv=(ListView)findViewById(R.id.RatingsList);
        while(cursorForRatings.moveToNext())
        {
            String MacAd=cursorForRatings.getString(0);
            double rating=cursorForRatings.getDouble(1);
            RatingStrings.add(MacAd+"   "+String.format("%.4f",rating));
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, RatingStrings);
        lv.setAdapter(adapter);
    }
}
