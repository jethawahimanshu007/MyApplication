package com.example.himanshu.myapplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class EventsDemoActivity extends FragmentActivity
        implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,FragmentContact.OnDataPass {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_demo);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_wrapper, new FragmentContact());
        fragmentTransaction.commit();

        }

    private void setUpMap()
    {

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
    }

}

