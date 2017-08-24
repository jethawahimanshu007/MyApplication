package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class TSRsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tsrs);
        final SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        Cursor cursorForTSRs=mydatabase.rawQuery("SELECT SI,weight,belongs_to from TSR_TBL",null);
        ArrayList<String> TSRStrings=new ArrayList<>();
        final ListView lv=(ListView)findViewById(R.id.TSRsList);

        while(cursorForTSRs.moveToNext())
        {
            String SI=cursorForTSRs.getString(0);
            double weight=cursorForTSRs.getDouble(1);
            String belongs_to=cursorForTSRs.getString(2);
            TSRStrings.add(SI+" :: "+String.format("%.4f", weight)+" :: "+belongs_to);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, TSRStrings);
        lv.setAdapter(adapter);

        Button addIP=(Button)findViewById(R.id.addIP);

        addIP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ConstantsClass.IP_ADDRESS=(((EditText)findViewById(R.id.ipAd)).getText().toString());
            }
        });
    }
}
