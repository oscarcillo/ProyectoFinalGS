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

import java.util.ArrayList;
import java.util.List;

public class InstrumentsList extends ArrayAdapter<String> {

    private Activity context;
    private List<String> instrumentsList;
    private List<String> choosenInstruments;

    public InstrumentsList(Activity context, List<String> instrumentsList){
        super(context, R.layout.layout_instruments_list, instrumentsList);
        this.context = context;
        this.instrumentsList = instrumentsList;
        this.choosenInstruments = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.layout_instruments_list, null, true);

        final CheckBox checkBoxInstruments = listViewItem.findViewById(R.id.instrumentCheckbox);

        String instrument = instrumentsList.get(position);

        checkBoxInstruments.setText(instrument);

        //listener que se activa al pulsar los checkbox
        checkBoxInstruments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    choosenInstruments.add(checkBoxInstruments.getText().toString());
                else
                    choosenInstruments.remove(checkBoxInstruments.getText().toString());
                for(int i = 0; i<choosenInstruments.size();i++){
                    Log.e("CHECKBOX", choosenInstruments.get(i));
                }
            }
        });

        return listViewItem;
    }

    public List<String> getChooseInstruments(){
        return choosenInstruments;
    }
}
