package com.example.proyecto_final_gs;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.musyzian.firebase.FirebaseManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstrumentsList extends ArrayAdapter<String> {

    FirebaseManager manager = FirebaseManager.get();

    private Activity context;
    private List<String> instrumentsList;
    private List<String> choosenInstruments;
    private List<String> databaseChoosenInstruments;

    private boolean[] selectedItems;

    public InstrumentsList(Activity context, List<String> instrumentsList){
        super(context, R.layout.layout_instruments_list, instrumentsList);
        this.context = context;
        this.instrumentsList = instrumentsList;
        this.choosenInstruments = new ArrayList<>();
        selectedItems = new boolean[100];
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.layout_instruments_list, null, true);

        final CheckBox checkBoxInstruments = listViewItem.findViewById(R.id.instrumentCheckbox);

        final String instrument = instrumentsList.get(position);

        checkBoxInstruments.setText(instrument);

        //cargar los instrumentos seleccionados de la base de datos
        manager.loadChoosenInstrumentsList(new FirebaseManager.OnFirebaseLoadInstruments() {
            @Override
            public void onResult(List<String> instruments) {
                databaseChoosenInstruments = instruments;
                //checkear los instrumentos seleccionados de la base de datos
                for(int i = 0; i < databaseChoosenInstruments.size(); i++)
                    if(databaseChoosenInstruments.get(i).equals(instrument))
                        checkBoxInstruments.setChecked(true);
            }
        });

        //eliminar instrumentos repetidos
        Set<String> set = new HashSet<>(choosenInstruments);
        choosenInstruments.clear();
        choosenInstruments.addAll(set);

        //listener que se activa al pulsar los checkbox
        checkBoxInstruments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    choosenInstruments.add(checkBoxInstruments.getText().toString());
                    selectedItems[position] = true;
                    Log.e("anadido", ""+choosenInstruments);
                }
                else {
                    choosenInstruments.remove(checkBoxInstruments.getText().toString());
                    selectedItems[position] = false;
                    Log.e("elimimado", ""+choosenInstruments);
                }
            }
        });

        //comprobar si el checkbox tiene que estar checkeado o no
        if(selectedItems[position])
            checkBoxInstruments.setChecked(true);
        else
            checkBoxInstruments.setChecked(false);

        return listViewItem;
    }

    public List<String> getChoosenInstruments(){
        Set<String> set = new HashSet<>(choosenInstruments);
        choosenInstruments.clear();
        choosenInstruments.addAll(set);
        return choosenInstruments;
    }
}
