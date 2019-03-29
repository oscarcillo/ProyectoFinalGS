package com.example.proyecto_final_gs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ArtistsSetUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //views
    ListView listViewArtists;
    EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists_set_up);
        //evita que el layout se mueva cuando sale el teclado
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //
        listViewArtists = findViewById(R.id.listViewArtists);
        searchText = findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchForArtists();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForArtists();
            }
            @Override
            public void afterTextChanged(Editable s) {
                searchForArtists();
            }
        });
        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                searchForArtists();
            }
        });
    }

    //MENU SUPERIOR/////////////////////////
    ////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                goToLoginActivity();
                break;
        }
        return true;
    }
    public void goToLoginActivity(){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
    ///////////////////////////////////////////
    //////////////////////////////////////////

    public void searchForArtists(){
        List<String> artistsList = new ArrayList<>();
        //
        artistsList = readJsonUrl("http://ws.audioscrobbler.com/2.0/?method=artist.search&artist=" +
                searchText.getText().toString() +
                "&api_key=70ffbde8c4000bc56aa92a1b062261dc&format=json");
        //
        ArtistsList adapter = new ArtistsList(this, artistsList);
        listViewArtists.setAdapter(adapter);
    }

    public List<String> readJsonUrl(String url){
        //crear lista de nombres de artistas
        final List<String> lista = new ArrayList<>();
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
                            for(int i = 0; i < array.size();i++)
                                lista.add(array.get(i).getAsJsonObject().get("name").toString());
                        }
                    });
            return lista;
        }
        //lista.add("");
        return lista;
    }
}
