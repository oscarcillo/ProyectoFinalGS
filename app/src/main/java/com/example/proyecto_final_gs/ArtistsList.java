package com.example.proyecto_final_gs;

import android.app.Activity;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ArtistsList extends ArrayAdapter<String> {

    private Activity context;
    private List<String> artistsList;

    public ArtistsList(Activity context, List<String> artistsList){
        super(context, R.layout.layout_artists_list, artistsList);
        this.context = context;
        this.artistsList = artistsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.layout_artists_list, null, true);

        final TextView artistsText = listViewItem.findViewById(R.id.artistText);
        final ImageView artistImage = listViewItem.findViewById(R.id.artistImage);

        String artist = artistsList.get(position);

        artistsText.setText(artist);

        return listViewItem;
    }
}
