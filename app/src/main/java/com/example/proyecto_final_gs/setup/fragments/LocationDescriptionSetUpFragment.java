package com.example.proyecto_final_gs.setup.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.proyecto_final_gs.MainActivity;
import com.example.proyecto_final_gs.R;
import com.example.proyecto_final_gs.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationDescriptionSetUpFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //views
    TextView locationText;
    Button locationDescriptionButton;
    ImageButton findLocationButton;

    FusedLocationProviderClient fusedLocationClient;
    boolean located = false;

    int MY_PERMISSIONS_REQUEST_LOCATION = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_description_set_up, container, false);

        //inicialización variable geolocalizacion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        //views import
        locationText = view.findViewById(R.id.locationText);
        locationDescriptionButton = view.findViewById(R.id.locationDescriptionButton);
        findLocationButton = view.findViewById(R.id.findLocationButton);

        //listener del boton para encontrar la ubicacion del dispositivo
        findLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(v);
            }
        });

        locationDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToActivity(getContext(), MainActivity.class, null, true);
            }
        });

        return view;
    }

    public void getLocation(View v) {
        //dar permisos para geolocalizar el dispositivo
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        //obtener la localizacion del dispositivo
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            String ciudad = getCity(location);
                            locationText.setText(ciudad);
                            located = true;
                        }
                    }
                });
    }

    public String getCity(Location loc) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

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
}
