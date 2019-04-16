package com.example.proyecto_final_gs.setup.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.proyecto_final_gs.InstrumentsList;
import com.example.proyecto_final_gs.MainActivity;
import com.example.proyecto_final_gs.Utils;
import com.example.proyecto_final_gs.setup.OnFragmentInteractionListener;
import com.example.proyecto_final_gs.R;
import com.musyzian.firebase.FirebaseManager;

import java.util.List;

public class MusicalSetUpFragment extends Fragment {

    FirebaseManager manager;

    //views
    ListView listViewInstruments;
    Button instrumentsButton;

    //variables
    List<String> choosenInstruments;
    InstrumentsList adapter;

    //fragment listener
    private OnFragmentInteractionListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musical_set_up, container, false);

        //firebase manager
        manager = FirebaseManager.get();

        //import views
        listViewInstruments = view.findViewById(R.id.listViewInstruments);
        instrumentsButton = view.findViewById(R.id.instrumentsButton);

        //listener del boton inferior del fragment
        instrumentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadChoosenInstrumentsFirebase(v);
            }
        });

        //inckuir datos sobre instrumentos en la lista desde Firebase
        manager.loadInstrumentsList(new FirebaseManager.OnFirebaseLoadInstruments() {
            @Override
            public void onResult(List<String> instruments) {
                adapter = new InstrumentsList(getActivity(), instruments);
                listViewInstruments.setAdapter(adapter);
                //ir al final de la lista para cargar todos los instrumentos
                listViewInstruments.setSelection(adapter.getCount()-1);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        listViewInstruments.setSelection(0);
                    }
                }, 50);
            }
        });

        return view;
    }

    public void uploadChoosenInstrumentsFirebase(View v){
        choosenInstruments = adapter.getChoosenInstruments();
        if(choosenInstruments.size()==0) {
            Toast.makeText(getActivity(), getText(R.string.musical_setup_choose_instrument), Toast.LENGTH_SHORT).show();
            return;
        }
        //subir los instrumentos elegidos a firebase
        manager.uploadInstrumentsData(choosenInstruments);

        //comprobar si debe cambiar de fragmento o volver al menu de opciones
        Intent i = getActivity().getIntent();
        String fragment = i.getStringExtra("fragment");
        if(fragment!=null)
            Utils.goToActivity(getActivity(), MainActivity.class,
                    null, true);
        else
            mListener.changeFragment(3);
    }

    // region Fragment changer
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

}
