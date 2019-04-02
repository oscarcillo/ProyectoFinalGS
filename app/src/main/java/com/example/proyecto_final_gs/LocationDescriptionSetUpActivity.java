package com.example.proyecto_final_gs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationDescriptionSetUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //views
    TextView locationText;

    FusedLocationProviderClient fusedLocationClient;

    int MY_PERMISSIONS_REQUEST_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_description_set_up);
        //inicialización variable geolocalizacion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //
        locationText = findViewById(R.id.locationText);
    }

    // region MENU SUPERIOR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                Utils.goToActivity(LocationDescriptionSetUpActivity.this, LoginActivity.class,
                        null, true);
                break;
        }
        return true;
    }

    // endregion

    public void getLocation(View v) {
        //dar permisos para geolocalizar el dispositivo
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        //obtener la localizacion del dispositivo
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            String ciudad = getCity(location);
                            locationText.setText(ciudad);
                        }
                    }
                });
    }

    public String getCity(Location loc) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            if(loc==null)
                return "";
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if(addresses.size()>=1) {
                //String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                //String state = addresses.get(0).getAdminArea();
                //String postalCode = addresses.get(0).getPostalCode();
                //String knownName = addresses.get(0).getFeatureName();
                return city;
            }
        }catch (IOException e){}
        return "";
    }

    public void goToMainActivity(View v){
        Utils.goToActivity(LocationDescriptionSetUpActivity.this, MainActivity.class,
                null, true);
    }
}
