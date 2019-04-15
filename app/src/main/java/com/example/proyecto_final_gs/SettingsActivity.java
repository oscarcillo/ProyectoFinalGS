package com.example.proyecto_final_gs;

import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.proyecto_final_gs.setup.SetUpActivity;
import com.musyzian.firebase.FirebaseManager;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    FirebaseManager manager;

    ArrayList<String> array;

    //views
    ListView listViewSettings;

    boolean isGoogleAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //inicialización del manager de firebase
        manager = FirebaseManager.get();

        //views import
        listViewSettings = findViewById(R.id.listViewSettings);

        //llenar lista con las opciones de configuración
        array = new ArrayList<>();

        //
        manager.isGoogleAccount(new FirebaseManager.OnFirebaseEventListener() {
            @Override
            public void onResult(Boolean success) {
                array.add(""+getText(R.string.profile_information));
                array.add(""+getText(R.string.instruments_selection));
                array.add(""+getText(R.string.artist_selection));
                array.add(""+getText(R.string.location_description));
                //si la cuenta no es de google añadir la posibilidad de cambio de contraseña
                if(!success)
                    array.add(""+getText(R.string.change_password));
                else
                    isGoogleAccount = true;
                array.add(""+getText(R.string.delete_account));
                //
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        getApplicationContext(),
                        android.R.layout.simple_list_item_1,
                        array);
                listViewSettings.setAdapter(arrayAdapter);
            }
        });
        //

        listViewSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle b = new Bundle();
                switch(position){
                    case 0:
                        b.putString("fragment", "personalsetup");
                        Utils.goToActivity(SettingsActivity.this, SetUpActivity.class,
                                b, false);
                        break;
                    case 1:
                        b.putString("fragment", "musicalsetup");
                        Utils.goToActivity(SettingsActivity.this, SetUpActivity.class,
                                b, false);
                        break;
                    case 2:
                        b.putString("fragment", "artistssetup");
                        Utils.goToActivity(SettingsActivity.this, SetUpActivity.class,
                                b, false);
                        break;
                    case 3:
                        b.putString("fragment", "locationdescriptionsetup");
                        Utils.goToActivity(SettingsActivity.this, SetUpActivity.class,
                                b, false);
                        break;
                    case 4:
                        if(isGoogleAccount)
                            deleteAccount();
                        else
                            changePassword();
                        break;
                    case 5:
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
                manager.signOut();
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
        showDeleteAccountDialog();
    }

    public void showDeleteAccountDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText password = new EditText(this);
        password.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setTitle(getText(R.string.delete_account));
        if(!isGoogleAccount)
            builder.setView(password);

        if(!isGoogleAccount)
            builder.setMessage(getText(R.string.delete_account_confirmation));
        else
            builder.setMessage(getText(R.string.delete_account_confirmation2));

        builder.setPositiveButton(getText(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                manager.deleteUser(isGoogleAccount, password.getText().toString(), new FirebaseManager.OnFirebaseEventListener() {
                    @Override
                    public void onResult(Boolean success) {
                        manager.signOut();
                        if(success) {
                            Toast.makeText(SettingsActivity.this,
                                    getText(R.string.user_deleted),
                                    Toast.LENGTH_SHORT).show();
                            Utils.goToActivity(SettingsActivity.this, LoginActivity.class, null, true);
                        } else
                            Toast.makeText(SettingsActivity.this,
                                    getText(R.string.user_not_deleted),
                                    Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton(getText(R.string.dialog_cancel), null);
        builder.show();
    }

    public void changePassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        //cuadro de texto para la contraseña vieja
        final EditText oldPassword = new EditText(this);
        oldPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oldPassword.setHint("Old password");
        layout.addView(oldPassword);
        //cuadro de texto para la contraseña nueva
        final EditText newPassword = new EditText(this);
        newPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPassword.setHint("New password");
        layout.addView(newPassword);

        //añadir layout al dialogo
        builder.setView(layout);

        builder.setTitle(getText(R.string.change_password));

        builder.setPositiveButton(getText(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                manager.changePassword(oldPassword.getText().toString(),
                        newPassword.getText().toString(),
                        new FirebaseManager.OnFirebaseEventListener() {
                    @Override
                    public void onResult(Boolean success) {
                        if(success)
                            Toast.makeText(SettingsActivity.this,
                                    getText(R.string.password_changed), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(SettingsActivity.this,
                                    getText(R.string.password_not_changed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton(getText(R.string.dialog_cancel), null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.goToActivity(SettingsActivity.this, MainActivity.class, null, true);
    }
}
