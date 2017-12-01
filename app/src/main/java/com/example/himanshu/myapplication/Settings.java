package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


//This activity is used to set the number of TSR matching parameter for an image transfer
public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    int TSRno=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Spinner gets the instance of the drop down
        Spinner spinner = (Spinner) findViewById(R.id.tsr_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tsr_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //Following is to get the set value of no of tsrs in the database
        Cursor cursorForTSRNoSelf=ConstantsClass.mydatabaseLatest.rawQuery("SELECT noOfTsrs FROM MAC_TSR_NO_TBL where MacAdd='SELF'",null);
        cursorForTSRNoSelf.moveToNext();
        TSRno=cursorForTSRNoSelf.getInt(0);
        spinner.setSelection(adapter.getPosition(Integer.toString(TSRno)));
    }

    //This function checks if the new value selected is different from the one in the database
    //If the value is different, update that value in the database
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        String menuItem=(String)parent.getItemAtPosition(pos);
        int noOfTSRs=Integer.parseInt(menuItem);

        if(TSRno!=noOfTSRs) {
            ConstantsClass.mydatabaseLatest.execSQL("UPDATE MAC_TSR_NO_TBL SET noOfTsrs=" + noOfTSRs + " WHERE MacAdd='SELF'");
        }

    }
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
