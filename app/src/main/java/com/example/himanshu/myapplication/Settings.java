package com.example.himanshu.myapplication;

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


//This activity is used to set the number of TSR matching parameter for an image transfer
public class Settings extends FragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,FragmentContact.OnDataPass{

    double latitude=0.0;double longitude=0.0;
    int operationMode=0;
    EditText et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Cursor modeOper=ConstantsClass.mydatabaseLatest.rawQuery("SELECT mode from OPERATION_MODE_TBL where MacAddr='SELF'",null);
        while(modeOper.moveToNext())
        {
            operationMode=modeOper.getInt(0);
        }
        if(operationMode==0)
        {
            RadioGroup pushPullRadio=(RadioGroup)findViewById(R.id.radioGroupPushPull);
            pushPullRadio.check(R.id.radio_push);
        }
        else{
            RadioGroup pushPullRadio=(RadioGroup)findViewById(R.id.radioGroupPushPull);
            pushPullRadio.check(R.id.radio_pull);
            showPullOptions();
            updatePullOptionsView();
        }

        Button addTagsButton=(Button)findViewById(R.id.addPrioTags);

        addTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            ///Action for button
            public void onClick(View view) {
                updatePullOptionsDB();
            }
        });


    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_push:
                if (checked) {
                    operationMode=0;
                    ConstantsClass.mydatabaseLatest.execSQL("UPDATE OPERATION_MODE_TBL SET mode="+operationMode+" WHERE MacAddr='SELF'");
                    hidePullOptions();
                }
                break;
            case R.id.radio_pull:
                if (checked) {
                    operationMode=1;
                    showPullOptions();
                    updatePullOptionsView();


                }
                break;
        }
    }
    @Override
    public void onMapClick(LatLng point) {
        Toast.makeText(this, "Tapped location is:"+point, Toast.LENGTH_LONG).show();
        //mTapTextView.setText("tapped, point=" + point);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Toast.makeText(this, "Long pressed location is:"+point, Toast.LENGTH_LONG).show();
        //mTapTextView.setText("long pressed, point=" + point);
    }

    public void onDataPass(LatLng data) {
        Log.d("LOG","Received Latlng value of selected point to this activity is: " + data.latitude+","+data.longitude);
        latitude=data.latitude;longitude=data.longitude;
    }

    public void showPullOptions()
    {
        //Make the pull elements visible --Tried putting everything in LinearLayout but it did not work!
        TextView st=(TextView)findViewById(R.id.settingsText);
        st.setVisibility(View.VISIBLE);
        EditText et=(EditText) findViewById(R.id.priorityTags);
        et.setVisibility(View.VISIBLE);
        FButton fb=(FButton) findViewById(R.id.addPrioTags);
        fb.setVisibility(View.VISIBLE);
        EditText radius=(EditText) findViewById(R.id.radius);
        radius.setVisibility(View.VISIBLE);
        LinearLayout ll=(LinearLayout) findViewById(R.id.mapLayout);
        ll.setVisibility(View.VISIBLE);
        TextView radiusTV=(TextView)findViewById(R.id.radiusTV);
        radiusTV.setVisibility(View.VISIBLE);
    }
    public void hidePullOptions()
    {
        TextView st=(TextView)findViewById(R.id.settingsText);
        st.setVisibility(View.GONE);
        EditText et=(EditText) findViewById(R.id.priorityTags);
        et.setVisibility(View.GONE);
        FButton fb=(FButton) findViewById(R.id.addPrioTags);
        fb.setVisibility(View.GONE);
        EditText radius=(EditText) findViewById(R.id.radius);
        radius.setVisibility(View.GONE);
        LinearLayout ll=(LinearLayout) findViewById(R.id.mapLayout);
        ll.setVisibility(View.GONE);
        TextView radiusTV=(TextView)findViewById(R.id.radiusTV);
        radiusTV.setVisibility(View.GONE);
    }
    public void updatePullOptionsView()
    {
        //Following is to get the set value of no of tsrs in the database
        Cursor cursorForTSRNoSelf=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * FROM HIGH_PRIO_TAGS_TBL where MacAddr='SELF'",null);
        cursorForTSRNoSelf.moveToNext();
        String existing=cursorForTSRNoSelf.getString(1);
        et=(EditText)findViewById(R.id.priorityTags);
        et.setText(existing);

        //Following is to set the marker on the Map

        //Following is to set the radius value
        double radiusValue=0.0;
        Cursor cursorForNoOfRowsRadius=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from RADIUS_TBL where MacAddr='SELF'",null);
        while(cursorForNoOfRowsRadius.moveToNext())
        {
            radiusValue=cursorForNoOfRowsRadius.getDouble(1);
        }
        EditText radiusET=(EditText)findViewById(R.id.radius);
        radiusET.setText(Double.toString(radiusValue));
        //This part draws map and marker both
        Cursor cursorForLocation=ConstantsClass.mydatabaseLatest.rawQuery("SELECT lat,long from COORDINATES_TBL where MacAddr='SELF'",null);
        while(cursorForLocation.moveToNext())
        {
            latitude=cursorForLocation.getDouble(0);
            longitude=cursorForLocation.getDouble(1);
        }

        String markerPoint=latitude+","+longitude;
            Bundle bundle = new Bundle();
            bundle.putString("location", markerPoint);
            FragmentContact fragobj = new FragmentContact();
            fragobj.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.mapLayout, fragobj);
            fragmentTransaction.commit();






    }
    public void updatePullOptionsDB()
    {
        String newTags=et.getText().toString();
        String newRadius=((EditText)findViewById(R.id.radius)).getText().toString();
        Log.d("Settings","Mode of operation setting in DB inside Settings is:"+operationMode);
        ConstantsClass.mydatabaseLatest.execSQL("UPDATE OPERATION_MODE_TBL SET mode="+operationMode+" WHERE MacAddr='SELF'");
        ConstantsClass.mydatabaseLatest.execSQL("UPDATE HIGH_PRIO_TAGS_TBL SET SIList='"+newTags+"' WHERE MAcAddr='SELF'");
        ConstantsClass.mydatabaseLatest.execSQL("UPDATE RADIUS_TBL SET radius="+newRadius+" WHERE MacAddr='SELF'");
        ConstantsClass.mydatabaseLatest.execSQL("UPDATE COORDINATES_TBL SET lat="+latitude+",long="+longitude+" WHERE MacAddr='SELF'");
    }
}
