package com.example.himanshu.myapplication;

import android.content.Intent;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class ShowReceivedImages extends AppCompatActivity {

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_received_images);



        gridView = (GridView) findViewById(R.id.gridViewReceived);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                //Create intent
                Intent intent = new Intent(ShowReceivedImages.this, DetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                //intent.putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);
            }
        });
    }

    private ArrayList<ImageItem> getData() {
        SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        DbFunctions dbFunctions = new DbFunctions();

        String mobileArray[] = dbFunctions.getReceivedImages(mydatabase);
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        if (mobileArray != null) {
            for(int i=0;i<mobileArray.length;i++)
            {
                File sd = Environment.getExternalStorageDirectory();
                File image = new File(mobileArray[i]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                imageItems.add(new ImageItem(bitmap,image.getAbsolutePath()));
            }
        }

        return imageItems;
    }

}
