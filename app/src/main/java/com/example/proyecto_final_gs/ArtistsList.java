package com.example.proyecto_final_gs;

import android.app.Activity;
import android.media.Image;
import android.net.Uri;
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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ArtistsList extends ArrayAdapter<String> {

    private Activity context;
    private List<String> artistsList;
    private List<String> imageList;
    private List<String> enabled;
    private ImageView checkImage;

    public ArtistsList(Activity context, List<String> artistsList, List<String> imageList, List<String> enabled){
        super(context, R.layout.layout_artists_list, artistsList);
        this.context = context;
        this.artistsList = artistsList;
        this.imageList = imageList;
        this.enabled = enabled;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.layout_artists_list, null, true);

        final TextView artistsText = listViewItem.findViewById(R.id.artistText);
        final ImageView artistImage = listViewItem.findViewById(R.id.artistImage);
        final ImageView checkImage = listViewItem.findViewById(R.id.checkImage);

        String artist = artistsList.get(position);
        String url = imageList.get(position);
        artist = artist.replace("\"","");
        url = url.replace("\"","");

        Log.e("urls", url);

        artistsText.setText(artist);
        Glide.with(context).load(url).into(artistImage);

        if(enabled.get(position).equals("true"))
            checkImage.setVisibility(View.VISIBLE);

        return listViewItem;
    }
}
