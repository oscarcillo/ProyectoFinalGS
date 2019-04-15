package com.example.proyecto_final_gs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.musyzian.firebase.FirebaseManager;

public class MainActivity extends AppCompatActivity {

    FirebaseManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        manager = FirebaseManager.get();
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
                Utils.goToActivity(MainActivity.this, LoginActivity.class,
                        null, true);
                break;
            case R.id.settings:
                Utils.goToActivity(MainActivity.this, SettingsActivity.class,
                        null, false);
                break;
        }
        return true;
    }
    // endregion

}
