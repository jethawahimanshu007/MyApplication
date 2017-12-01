package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

//This activity is used to show a list of the TSRs, either self or acquired
public class TSRsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tsrs);
        //final SQLiteDatabase ConstantsClass.mydatabaseLatest = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        //Database query to fetch all the interests of devices- i.e. interest, its weight and which device it belongs to
        Cursor cursorForTSRs=ConstantsClass.mydatabaseLatest.rawQuery("SELECT SI,weight,belongs_to from TSR_TBL",null);
        ArrayList<String> TSRStrings=new ArrayList<>();


        final ListView lv=(ListView)findViewById(R.id.TSRsList);
        //Following part fetches the data from the database and adds to the arraylist
        while(cursorForTSRs.moveToNext())
        {
            String SI=cursorForTSRs.getString(0);
            double weight=cursorForTSRs.getDouble(1);
            String belongs_to=cursorForTSRs.getString(2);
            TSRStrings.add(SI+" :: "+String.format("%.4f", weight)+" :: "+belongs_to);
        }
        //This part shows the data on the screen!
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, TSRStrings);
        lv.setAdapter(adapter);


    }
}
