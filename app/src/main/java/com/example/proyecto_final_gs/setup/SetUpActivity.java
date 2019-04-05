package com.example.proyecto_final_gs.setup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.proyecto_final_gs.setup.fragments.ArtistsSetUpFragment;
import com.example.proyecto_final_gs.FragmentAdapter;
import com.example.proyecto_final_gs.setup.fragments.LocationDescriptionSetUpFragment;
import com.example.proyecto_final_gs.LoginActivity;
import com.example.proyecto_final_gs.setup.fragments.MusicalSetUpFragment;
import com.example.proyecto_final_gs.setup.fragments.PersonalSetUpFragment;
import com.example.proyecto_final_gs.R;
import com.example.proyecto_final_gs.SettingsActivity;
import com.example.proyecto_final_gs.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SetUpActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference("users");

    private FragmentAdapter fragmentAdapter;
    public ViewPager viewPager;
    Fragment frag1, frag2, frag3, frag4;

    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        //create fragments
        frag1 = new PersonalSetUpFragment();
        frag2 = new MusicalSetUpFragment();
        frag3 = new ArtistsSetUpFragment();
        frag4 = new LocationDescriptionSetUpFragment();

        //verificar a que fragmento debería ir
        verifyConf();
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
                Utils.goToActivity(SetUpActivity.this, LoginActivity.class,
                        null, true);
                break;
           case R.id.settings:
                Utils.goToActivity(SetUpActivity.this, SettingsActivity.class,
                        null, false);
                break;
        }
        return true;
    }
    // endregion

    public void setUpFragment(Fragment fragment){
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayoutFragments, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void changeFragment(int id) {
        if (id == 2) {
            setUpFragment(frag2);
        }
        if (id == 3) {
            setUpFragment(frag3);
        }
        if (id == 4) {
            setUpFragment(frag4);
        }
    }

    //metodo que verifica que fragmento cargar inicialmente
    public void verifyConf(){
        ref.child(mAuth.getCurrentUser().getUid()).child("conf").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String setupactivity = dataSnapshot.child("setupactivity").getValue(String.class);
                String musicalsetupactivity = dataSnapshot.child("musicalsetupactivity").getValue(String.class);
                String artistssetupactivity = dataSnapshot.child("artistssetupactivity").getValue(String.class);
                //
                if(artistssetupactivity!=null){
                   setUpFragment(frag4);
                    return;
                }
                if(musicalsetupactivity!=null){
                    setUpFragment(frag3);
                    return;
                }
                else if(setupactivity!=null) {
                    setUpFragment(frag2);
                    return;
                }
                else {
                    setUpFragment(frag1);
                    return;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}
