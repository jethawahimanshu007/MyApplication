package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//This activity is used to add keywords to the database
public class AddKeywords extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_keywords);
        //Database query to get all the keywords belonging to the device and put it in the EditText
        Cursor cursorKeywords=ConstantsClass.mydatabaseLatest.rawQuery("SELECT GROUP_CONCAT(SI) from TSR_TBL where belongs_to='SELF'",null);
        cursorKeywords.moveToFirst();
        String keywords=cursorKeywords.getString(0);
        EditText editText=(EditText)findViewById(R.id.editTextAddKeywords);
        editText.setText(keywords, TextView.BufferType.EDITABLE);
        Button keyWordsAdd=(Button)findViewById(R.id.keywordsAdd);


        //Add into thr database on button click "Add keywords" and show the notification that the keywords are added
        keyWordsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {
                EditText editText=(EditText)findViewById(R.id.editTextAddKeywords);
                 String tags=editText.getText().toString();
                new DbFunctions().insertIntoTSRTbl(ConstantsClass.mydatabaseLatest,tags);
                Toast.makeText(getBaseContext(), "Keywords are added", Toast.LENGTH_SHORT).show();
            }
            });
    }
}
