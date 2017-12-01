package com.example.himanshu.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;

//This file is used to show the grid view message list for all the enemies within one mile radius of my location
public class EnemiesWithinAnArea extends AppCompatActivity {

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_messages);
        gridView = (GridView) findViewById(R.id.gridViewOwn);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData(),R.id.imageGrid,R.id.textGrid);
        gridView.setAdapter(gridAdapter);

        //When an image is clicked, forward the title to the DetailsActivity to show all the details
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                //Create intent
                Intent intent = new Intent(EnemiesWithinAnArea.this, DetailsActivity.class);
                intent.putExtra("title", item.getTitle());

                //Start details activity
                startActivity(intent);
            }
        });
    }

    //This function populates the grid view of the images with all the images tagged with the keyword "Enemy"
    private ArrayList<ImageItem> getData() {

        ArrayList<String> enemiesNearMeAL=new ArrayList<String>();
        //Before my location can be taken, it needs to be checked if the location setting is enable or not
        if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
        }
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        double longitude = 0.0, latitude = 0.0;
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        LatLng userLoc=new LatLng(latitude,longitude);
        //This query gets all the images tagged with the keyword enemy
        Cursor enemiesNearMe=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from MESSAGE_TBL where tagsForCurrentImage like '%enemy%'",null);
        //For all the images tagged with the keyword enemy, calculate their distance from me in the following while loop
        while(enemiesNearMe.moveToNext())
        {
            String imgPath=enemiesNearMe.getString(0);
            double tempLat=enemiesNearMe.getDouble(1);
            double tempLong=enemiesNearMe.getDouble(2);
            LatLng imgLoc=new LatLng(tempLat,tempLong);
            if(DbFunctions.calculationByDistance(userLoc,imgLoc)<1)
            {
                enemiesNearMeAL.add(imgPath);
            }
        }


        //Finally, add all the images within one mile radius to the GridView
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        if (enemiesNearMeAL != null) {
            for(int i=0;i<enemiesNearMeAL.size();i++)
            {
                File image = new File(enemiesNearMeAL.get(i));
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                imageItems.add(new ImageItem(bitmap,image.getAbsolutePath()));
            }
        }

        return imageItems;
    }

}
