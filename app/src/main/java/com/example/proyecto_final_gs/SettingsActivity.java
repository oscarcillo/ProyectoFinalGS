package com.example.proyecto_final_gs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference("users");

    //views
    ListView listViewSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //views import
        listViewSettings = findViewById(R.id.listViewSettings);

        //llenar lista con las opciones de configuración
        ArrayList<String> array = new ArrayList<>();

        array.add(""+getText(R.string.profile_information));
        array.add(""+getText(R.string.instruments_selection));
        array.add(""+getText(R.string.artist_selection));
        array.add(""+getText(R.string.location_description));
        array.add(""+getText(R.string.delete_account));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                array);
        listViewSettings.setAdapter(arrayAdapter);

        listViewSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 4:
                        deleteAccount();
                        break;
                }
            }
        });
    }

    // region *Menu Superior*
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
                Utils.goToActivity(SettingsActivity.this, LoginActivity.class,
                        null, true);
                break;
            case R.id.settings:
                Utils.goToActivity(SettingsActivity.this, SettingsActivity.class,
                        null, true);
                break;
        }
        return true;
    }
    // endregion

    public void deleteAccount(){
        FirebaseUser user = mAuth.getCurrentUser();
        ref.child(user.getUid()).removeValue();
        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), getText(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                Utils.goToActivity(getApplicationContext(), LoginActivity.class, null, true);
            }
        });
    }

}
