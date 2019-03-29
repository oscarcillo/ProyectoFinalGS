package com.example.proyecto_final_gs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MusicalSetUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    DatabaseReference refUsers = db.getReference("users").child(user.getUid()).child("instruments");
    DatabaseReference refInstruments = db.getReference("instruments");

    //views
    ListView listViewInstruments;

    //variables
    List<String> choosenInstruments;
    InstrumentsList adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musical_set_up);
        //
        listViewInstruments = findViewById(R.id.listViewInstruments);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //
        checkData();
        //inckuir datos sobre instrumentos en la lista desde Firebase
        final List<String> instrumentsList = new ArrayList<>();
        refInstruments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                instrumentsList.clear();
               for(DataSnapshot snapshot: dataSnapshot.getChildren())
                   instrumentsList.add(snapshot.getValue(String.class));
                adapter = new InstrumentsList(MusicalSetUpActivity.this, instrumentsList);
                listViewInstruments.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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

    public void checkData(){
        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    goToArtistsSetUpActivity();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void uploadChoosenInstrumentsFirebase(View v){
        choosenInstruments = adapter.getChooseInstruments();
        //borrar primero los datos anteriores
        refUsers.removeValue();
        //Insertar los nuevos valores
        for(int i = 0;i<choosenInstruments.size();i++)
            refUsers.child(""+i).setValue(choosenInstruments.get(i));
        //ir a la siguiente actividad
        goToArtistsSetUpActivity();
    }

    public void goToArtistsSetUpActivity(){
        Intent i = new Intent(this, ArtistsSetUpActivity.class);
        startActivity(i);
    }
}
