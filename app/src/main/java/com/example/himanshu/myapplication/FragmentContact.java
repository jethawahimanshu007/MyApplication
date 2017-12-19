package com.example.himanshu.myapplication;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static com.example.himanshu.myapplication.LabelApp.context;

public class FragmentContact extends Fragment implements OnMapReadyCallback {
    MapView mapView;
    GoogleMap googleMap;
    Marker m;
    double latitudeDB,longitudeDB;
    private WebView contactWebView;
    public FragmentContact(){

    }
    OnDataPass dataPasser;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }
    public void passData(String data) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        getActivity().setTitle("Contact Us");
        String AlreadyMarkedLoc = getArguments().getString("location");
         latitudeDB=Double.parseDouble(AlreadyMarkedLoc.split(",")[0]);
         longitudeDB=Double.parseDouble(AlreadyMarkedLoc.split(",")[1]);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        if (mapView != null) {
            mapView.getMapAsync(this);
        }
        return view;
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    public static String StreamToString(InputStream in) throws IOException {
        if(in == null) {
            return "";
        }
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
        }
        return writer.toString();
    }
    @Override
    public void onMapReady(GoogleMap map) {

        googleMap = map;
        if(latitudeDB!=0.0 && longitudeDB!=0.0)
        m=googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitudeDB, longitudeDB))
                .title("Selected point")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker()));
        setUpMap();
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                Log.d("FragmentContact","Map clicked [" + point.latitude + " / " + point.longitude + "]");
                dataPasser.onDataPass(new LatLng(point.latitude,point.longitude));
                if(m!=null)
                {
                    m.setPosition(point);
                }
                else {
                    m=googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(point.latitude, point.longitude))
                            .title("Selected point")
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker()));
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            LocationManager lm = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //location=getLastKnownLocation();
            Log.d("FragmentContact","Checking if location is null:"+(location==null));
            double longitude = 0.0, latitude = 0.0;
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.d("FragmentContact","Current location is:"+latitude+","+longitude);
            }

            googleMap.getUiSettings().setZoomControlsEnabled(true);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(latitude, longitude));
            LatLngBounds bounds = builder.build();
            int padding = 0;
            // Updates the location and zoom of the MapView
            ///map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));


            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            //googleMap.moveCamera(cameraUpdate);

            //googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }



    }

    public void setUpMap(){

        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
    public interface OnDataPass {
         void onDataPass(LatLng selectedLatLng);
    }

     Location getLastKnownLocation() {
         LocationManager mLocationManager;
        mLocationManager = (LocationManager)getActivity().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
         if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }}
        return bestLocation;
    }
}