package com.example.tracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    String TAG="Location ";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mLocation;
    private GoogleMap mMap;
    private Marker currentMarker;

    Context context;

    TextView lat,lon,address;
    Double d_lat, d_long;

    String fetched_address="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lat = (TextView) findViewById(R.id.lat);
        lon = (TextView) findViewById(R.id.lon);
        address = (TextView) findViewById(R.id.address);

        context = getApplicationContext();

        checkLocationPermission();
        init();
    }

    public void checkLocationPermission(){
        Log.d(TAG, "check location");

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                        init();
                    }
                }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }
    public void stopLocationUpdates()
    {
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(task -> {Log.d(TAG, "stop location updates");});
    }

    private void receiveLocation(LocationResult locationResult){

        mLocation=locationResult.getLastLocation();

        Log.d(TAG, "latitude : "+mLocation.getLatitude());
        Log.d(TAG, "longitude: "+mLocation.getLongitude());
        Log.d(TAG, "Altitude: "+mLocation.getAltitude());

        String s_lat=String.format(Locale.ROOT, "%.6f", mLocation.getLatitude());
        String s_lon=String.format(Locale.ROOT, "%.6f", mLocation.getLongitude());

        d_lat = mLocation.getLatitude();
        d_long = mLocation.getLongitude();
        LatLng myPosition = new LatLng(d_lat, d_long);

        for(Location location : locationResult.getLocations()){
            if(currentMarker != null){
                currentMarker.remove();
            }
            currentMarker = mMap.addMarker(new MarkerOptions().position(myPosition).title("My Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15));
        }

        lat.setText(""+s_lat);
        lon.setText(""+s_lon);

        try {
            Geocoder geocoder=new Geocoder(this, Locale.getDefault());
            List<Address> addresses=geocoder.getFromLocation(d_lat,d_long,1);

            fetched_address=addresses.get(0).getAddressLine(0);

            Log.d(TAG,""+fetched_address);
            address.setText(fetched_address+"");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void init()
    {
        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient=LocationServices.getSettingsClient(this);
        mLocationCallback=new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                receiveLocation(locationResult);
            }
        };

        mLocationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(100);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest=builder.build();
        startLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapDebug", "onMapReady called");
            mMap = googleMap;
            mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}