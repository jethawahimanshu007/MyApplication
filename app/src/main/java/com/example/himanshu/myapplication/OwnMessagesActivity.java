package com.example.himanshu.myapplication;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
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

import java.io.File;
import java.util.ArrayList;

public class OwnMessagesActivity extends AppCompatActivity {

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_messages);
        gridView = (GridView) findViewById(R.id.gridViewOwn);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData(),R.id.imageGrid,R.id.textGrid);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                //Create intent
                Intent intent = new Intent(OwnMessagesActivity.this, DetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                //intent.putExtra("title", item.getTitle());
                //intent.putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);
            }
        });
    }

    private ArrayList<ImageItem> getData() {
        SQLiteDatabase mydatabase = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        DbFunctions dbFunctions = new DbFunctions();

        String mobileArray[] = dbFunctions.getOwnImages(mydatabase);
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
