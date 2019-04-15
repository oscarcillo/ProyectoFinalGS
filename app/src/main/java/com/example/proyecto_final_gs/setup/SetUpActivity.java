package com.example.proyecto_final_gs.setup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.proyecto_final_gs.MainActivity;
import com.example.proyecto_final_gs.setup.fragments.ArtistsSetUpFragment;
import com.example.proyecto_final_gs.FragmentAdapter;
import com.example.proyecto_final_gs.setup.fragments.LocationDescriptionSetUpFragment;
import com.example.proyecto_final_gs.LoginActivity;
import com.example.proyecto_final_gs.setup.fragments.MusicalSetUpFragment;
import com.example.proyecto_final_gs.setup.fragments.PersonalSetUpFragment;
import com.example.proyecto_final_gs.R;
import com.example.proyecto_final_gs.SettingsActivity;
import com.example.proyecto_final_gs.Utils;
import com.musyzian.firebase.FirebaseManager;

public class SetUpActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    FirebaseManager manager;

    Fragment frag1, frag2, frag3, frag4;

    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        //inicializar firebase manager
        manager = FirebaseManager.get();

        //create fragments
        frag1 = new PersonalSetUpFragment();
        frag2 = new MusicalSetUpFragment();
        frag3 = new ArtistsSetUpFragment();
        frag4 = new LocationDescriptionSetUpFragment();

        //verificar si cargar el setup wizard o cargar solo un fragmento estático
        Intent i = getIntent();
        String fragment = i.getStringExtra("fragment");
        if(fragment!=null){
            if(fragment.equals("personalsetup"))
                setUpFragment(frag1);
            if(fragment.equals("musicalsetup"))
                setUpFragment(frag2);
            if(fragment.equals("artistssetup"))
                setUpFragment(frag3);
            if(fragment.equals("locationdescriptionsetup"))
                setUpFragment(frag4);
        }
        else
            goToFragment(); //verificar a que fragmento debería ir
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
                manager.signOut();
                Utils.goToActivity(SetUpActivity.this, LoginActivity.class,
                        null, true);
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
    public void goToFragment(){
        manager.fragmentNavigation(new FirebaseManager.OnFragmentNavigation() {
            @Override
            public void onResult(FirebaseManager.NAVIGATION nav) {
                if(nav.equals(FirebaseManager.NAVIGATION.MainActivity)){
                    Utils.goToActivity(SetUpActivity.this, MainActivity.class,null, true);
                    return;
                }
                if(nav.equals(FirebaseManager.NAVIGATION.LocationDescriptionSetUp)){
                    setUpFragment(frag4);
                    return;
                }
                if(nav.equals(FirebaseManager.NAVIGATION.ArtistsSetUp)){
                    setUpFragment(frag3);
                    return;
                }
                if(nav.equals(FirebaseManager.NAVIGATION.MusicalSetUp)) {
                    setUpFragment(frag2);
                    return;
                }
                else {
                    setUpFragment(frag1);
                    return;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
