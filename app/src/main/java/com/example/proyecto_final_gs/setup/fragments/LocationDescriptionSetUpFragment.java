package com.example.proyecto_final_gs.setup.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Toast;

import com.example.proyecto_final_gs.MainActivity;
import com.example.proyecto_final_gs.R;
import com.example.proyecto_final_gs.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.musyzian.firebase.FirebaseManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationDescriptionSetUpFragment extends Fragment {

    FirebaseManager manager;

    //views
    TextView locationText;
    TextView descriptionTextArea;
    Button locationDescriptionButton;
    ImageButton findLocationButton;
    TextView coordsText;

    //datos
    String ciudad;
    Location loc;

    FusedLocationProviderClient fusedLocationClient;
    boolean located = false;

    int MY_PERMISSIONS_REQUEST_LOCATION = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_description_set_up, container, false);

        //firebase manager
        manager = FirebaseManager.get();

        //inicialización variable geolocalizacion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        //views import
        locationText = view.findViewById(R.id.locationText);
        locationDescriptionButton = view.findViewById(R.id.locationDescriptionButton);
        findLocationButton = view.findViewById(R.id.findLocationButton);
        descriptionTextArea = view.findViewById(R.id.descriptionTextArea);
        coordsText = view.findViewById(R.id.coordsText);

        //listener del boton para encontrar la ubicacion del dispositivo
        findLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(v);
            }
        });

        //cargar los datos de descripcion y localizacion en el fragmento
        manager.getDescription(new FirebaseManager.OnFirebaseLoadString() {
            @Override
            public void onResult(String string) {
                descriptionTextArea.setText(string);
            }
        });
        //cargar nombre de la ciudad
        manager.getCityName(new FirebaseManager.OnFirebaseLoadString() {
            @Override
            public void onResult(String string) {
                locationText.setText(string);
                locationText.setTextColor(Color.RED);
            }
        });

        //cargar coordenadas de la localización del dispositivo
        manager.getLocation(new FirebaseManager.OnFirebaseLoadLocation() {
            @Override
            public void onResult(Location loc) {
                coordsText.setText("Coords: "+loc.getLatitude()+", "+loc.getLongitude());
                coordsText.setTextColor(Color.RED);
            }
        });

        //listener del boton que envia informacion de este fragmento y cambia de actividad
        locationDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!descriptionTextArea.getText().toString().isEmpty() && loc!=null && located) {
                    manager.uploadLocationDescriptionData(descriptionTextArea.getText().toString(),
                            ciudad,
                            loc);

                    //comprobar si debe cambiar de fragmento o volver al menu de opciones
                    Intent i = getActivity().getIntent();
                    String fragment = i.getStringExtra("fragment");
                        Utils.goToActivity(getActivity(), MainActivity.class,
                                null, true);
                }
                else
                    Toast.makeText(getActivity(), getText(R.string.fill_description_location), Toast.LENGTH_SHORT).show();
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
                            ciudad = getCity(location);
                            loc = location;
                            //
                            locationText.setText(ciudad);
                            locationText.setTextColor(Color.BLACK);
                            coordsText.setText("Coords: "+loc.getLatitude()+", "+loc.getLongitude());
                            coordsText.setTextColor(Color.BLACK);
                            //
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
