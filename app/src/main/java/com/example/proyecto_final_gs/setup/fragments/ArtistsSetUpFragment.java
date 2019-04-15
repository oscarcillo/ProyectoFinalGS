package com.example.proyecto_final_gs.setup.fragments;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_final_gs.ArtistsList;
import com.example.proyecto_final_gs.SettingsActivity;
import com.example.proyecto_final_gs.Utils;
import com.example.proyecto_final_gs.setup.OnFragmentInteractionListener;
import com.example.proyecto_final_gs.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.musyzian.firebase.FirebaseManager;

import java.util.ArrayList;
import java.util.List;

public class ArtistsSetUpFragment extends Fragment {

    FirebaseManager manager;

    //views
    ListView listViewArtists;
    EditText searchText;
    TextView artistsAddedText;
    Button artistsButton;

    List<String> artistsChoosen = new ArrayList<>();
    List<String> enabled = new ArrayList<>();

    //listener fragment
    private OnFragmentInteractionListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists_set_up, container, false);

        //firebase manager
        manager = FirebaseManager.get();

        //views import
        listViewArtists = view.findViewById(R.id.listViewArtists);
        searchText = view.findViewById(R.id.searchText);
        artistsAddedText = view.findViewById(R.id.artistsAddedText);
        artistsButton = view.findViewById(R.id.artistsButton);

        //listener del boton para enviar la informacion
        artistsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadArtists(v);
            }
        });

        //cargar lista de artistas de la base de datos
        manager.loadArtistsList(new FirebaseManager.OnFirebaseLoadInstruments() {
            @Override
            public void onResult(List<String> instruments) {
                artistsChoosen = instruments;
                for(int i = 0; i < instruments.size();i++)
                    artistsAddedText.append(instruments.get(i)
                            .replace("\"", "") + ", ");
            }
        });

        //actualizar lista de artistas al introducir texto en el recuadro
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForArtists();
            }
            @Override
            public void afterTextChanged(Editable s) {
                searchForArtists();
            }
        });

        return view;
    }

    public void searchForArtists(){
        Log.e("text",  searchText.getText().toString());
        readJsonUrl("http://ws.audioscrobbler.com/2.0/?method=artist.search&artist=" +
                searchText.getText().toString() +
                "&api_key="+getText(R.string.lastfm_key)+"&format=json");
        //
    }

    public void readJsonUrl(String url){
        //crear lista de nombres de artistas
        final List<String> lista = new ArrayList<>();
        final List<String> listaImages = new ArrayList<>();
        //
        if(!searchText.getText().toString().equals("")){
            Ion.with(this)
                    .load(url)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            //buscar el array de artistas
                            JsonArray array = result
                                    .get("results").getAsJsonObject()
                                    .get("artistmatches").getAsJsonObject()
                                    .get("artist").getAsJsonArray();
                            //recorrer el array
                            for(int i = 0; i <= 9;i++) {
                                //obtener el nombre del artista
                                lista.add(array.get(i).getAsJsonObject().get("name").toString());
                                //obtener la imagen del artista
                                JsonArray arrayImages = array.get(i).getAsJsonObject().get("image").getAsJsonArray();
                                listaImages.add(arrayImages.get(2).getAsJsonObject().get("#text").toString());
                                enabled.add("false");
                            }
                            //crear la lista
                            final ArtistsList adapter = new ArtistsList(getActivity(), lista, listaImages, enabled);
                            listViewArtists.setAdapter(adapter);

                            ////////////////////////
                            //listener para añadir artistas al hacer click sobre ellos
                            listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    //if que comprueba que el artista aun no este seleccionado para que no se repitan
                                    if(!artistsChoosen.contains(lista.get(position))) {
                                        //comprobar que no hay mas de 20 artistas ya seleccionados
                                        if(artistsChoosen.size()>=20) {
                                            Toast.makeText(getActivity(),
                                                    getText(R.string.no_more_artists), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        //
                                        artistsChoosen.add(lista.get(position).replace("\"", ""));
                                        Toast.makeText(getActivity(),
                                                getText(R.string.artist_added)+": "+lista.get(position),
                                                Toast.LENGTH_SHORT).show();
                                        //array que contiene string para comprobar si la lista debe mostrar o no el icono de check
                                        //enabled.add(position, "true");
                                        //listViewArtists.setAdapter(adapter);
                                        //mostrar artistas seleccionados en la parte inferior de la actividad
                                        artistsAddedText.setText(getText(R.string.artists_added));
                                        for(int i = 0; i < artistsChoosen.size();i++)
                                            artistsAddedText.append(artistsChoosen.get(i)
                                                    .replace("\"", "") + ", ");
                                    }
                                    else
                                        Toast.makeText(getActivity(),
                                                getText(R.string.artist_already_added),
                                                Toast.LENGTH_SHORT).show();
                                }
                            });

                            /////////////////////////
                            //borrar artistas del array al hacer una pulsacion larga
                            listViewArtists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                    artistsChoosen.remove(lista.get(position).replace("\"", ""));
                                    Log.e("delete", ""+artistsChoosen);
                                    //enabled.set(position, "false");
                                    // listViewArtists.setAdapter(adapter);
                                    //mostrar mensaje de artista borrado
                                    Toast.makeText(getContext(), getText(R.string.artist_deleted),
                                            Toast.LENGTH_SHORT).show();
                                    //
                                    artistsAddedText.setText(getText(R.string.artists_added));
                                    for(int i = 0; i < artistsChoosen.size();i++)
                                        artistsAddedText.append(artistsChoosen.get(i)
                                                .replace("\"", "") + ", ");
                                    return true;
                                }
                            });
                            /////////////////////
                        }
                    });
        }
    }

    public void uploadArtists(View v){
        if(artistsChoosen.size()>0){
            manager.uploadArtistsData(artistsChoosen);

            //comprobar si debe cambiar de fragmento o volver al menu de opciones
            Intent i = getActivity().getIntent();
            String fragment = i.getStringExtra("fragment");
            if(fragment!=null)
                Utils.goToActivity(getActivity(), SettingsActivity.class,
                        null, true);
            else
                mListener.changeFragment(4);
        }
       else
           Toast.makeText(getActivity(), getText(R.string.choose_one_artist), Toast.LENGTH_SHORT).show();
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
