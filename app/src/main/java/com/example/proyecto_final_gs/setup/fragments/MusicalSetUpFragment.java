package com.example.proyecto_final_gs.setup.fragments;

import android.app.Activity;
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
import com.example.proyecto_final_gs.setup.OnFragmentInteractionListener;
import com.example.proyecto_final_gs.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MusicalSetUpFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    DatabaseReference refUsers = db.getReference("users").child(user.getUid()).child("instruments");
    DatabaseReference refInstruments = db.getReference("instruments");

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
        final List<String> instrumentsList = new ArrayList<>();
        refInstruments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                instrumentsList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                    instrumentsList.add(snapshot.getValue(String.class));
                adapter = new InstrumentsList(getActivity(), instrumentsList);
                listViewInstruments.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        return view;
    }

    public void uploadChoosenInstrumentsFirebase(View v){
        choosenInstruments = adapter.getChooseInstruments();
        if(choosenInstruments.size()==0) {
            Toast.makeText(getActivity(), getText(R.string.musical_setup_choose_instrument), Toast.LENGTH_SHORT).show();
            return;
        }
        //borrar primero los datos anteriores
        refUsers.removeValue();
        //Insertar los nuevos valores
        for(int i = 0;i<choosenInstruments.size();i++)
            refUsers.child(""+i).setValue(choosenInstruments.get(i));
        //subir el dato de la configuracion
        DatabaseReference ref = db.getReference("users").child(user.getUid()).child("conf").child("musicalsetupactivity");
        ref.setValue("true");
        //ir al siguiente fragmento
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
