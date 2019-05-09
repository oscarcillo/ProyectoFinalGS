package com.example.proyecto_final_gs;

import android.content.DialogInterface;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.proyecto_final_gs.setup.SetUpActivity;
import com.musyzian.firebase.FirebaseManager;
import com.musyzian.firebase.User;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    FirebaseManager manager = FirebaseManager.get();

    //drawer layout
    DrawerLayout drawer;

    //views
    Toolbar toolbar;

    //bundle
    Bundle b = new Bundle();

    boolean isGoogleAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //cargar los datos de usuario para mostrar en el chat
        loadUserData();

        //cargar los mensajes de la conversacion
        loadMessages();

        //listener para el boton de enviar mensaje
        findViewById(R.id.chatSendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //inicializar drawer y toolbar
        drawer = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_without_filter);

        //region listener que da funcionalidad al menu
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.logoutButton:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

                        builder.setMessage(getText(R.string.dialog_signout_confirmation));

                        builder.setPositiveButton(getText(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                manager.signOut();
                                Utils.goToActivity(ChatActivity.this, LoginActivity.class,
                                        null, true);
                            }
                        });
                        builder.setNegativeButton(getText(R.string.dialog_cancel), null);
                        builder.show();
                        break;

                    case R.id.filterButton:
                        break;
                }
                return true;
            }
        });
        //endregion

        //comprobar si la cuenta actual es de google
        manager.isGoogleAccount(new FirebaseManager.OnFirebaseEventListener() {
            @Override
            public void onResult(Boolean success) {
                if(success) {
                    isGoogleAccount = true;
                    //
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    Menu menu = navigationView.getMenu();
                    menu.removeItem(R.id.nav_change_password);
                }
            }
        });

        //configurar el cambio entre el navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,
                toolbar,
                R.string.common_open_on_phone,
                R.string.account_deleted);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //cargar datos al navigation drawer
        loadDataToNavigationDrawer();

        //Listener para navegar entre las diferentes opciones del menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_chats:
                        Utils.goToActivity(getApplicationContext(), ChatListActivity.class,
                                null, false);
                        break;
                    case R.id.nav_profile_information:
                        menuItem.setChecked(false);
                        b.putString("fragment", "personalsetup");
                        Utils.goToActivity(getApplicationContext(), SetUpActivity.class,
                                b, false);
                        break;
                    case R.id.nav_instrument_selection:
                        b.putString("fragment", "musicalsetup");
                        Utils.goToActivity(getApplicationContext(), SetUpActivity.class,
                                b, false);
                        break;
                    case R.id.nav_artist_selection:
                        b.putString("fragment", "artistssetup");
                        Utils.goToActivity(getApplicationContext(), SetUpActivity.class,
                                b, false);
                        break;
                    case R.id.nav_location_description:
                        b.putString("fragment", "locationdescriptionsetup");
                        Utils.goToActivity(getApplicationContext(), SetUpActivity.class,
                                b, false);
                        break;
                    case R.id.nav_change_password:
                        changePassword();
                        break;
                    case R.id.nav_delete_account:
                        showDeleteAccountDialog();
                        break;
                }
                return true;
            }
        });


    }

    //metodo que carga los mensajes de la conversacion
    public void loadMessages(){
        final User user = getIntent().getParcelableExtra("user");
        final TextView messagesText = findViewById(R.id.messages);

        manager.loadChatMessages(manager.getUserId(), user.getId(), new FirebaseManager.OnFirebaseLoadChatMessages() {
            @Override
            public void onResult(List<String> messages, List<Boolean> owner) {

                String cadena = "";

                for(int i = 0; i < messages.size(); i++){

                    if(owner.get(i))
                        cadena = cadena + "Tú: ";
                    else
                        cadena = cadena + user.getName() +": ";

                    cadena = cadena + messages.get(i);
                    cadena = cadena + "\n";
                }

                messagesText.setText(cadena);
            }
        });
    }

    //metodo para enviar mensaje
    public void sendMessage(){
        User user = getIntent().getParcelableExtra("user");

        EditText chatWriteText = findViewById(R.id.chatWriteText);

        manager.sendChatMessage(manager.getUserId(), user.getId(), chatWriteText.getText().toString());

        chatWriteText.setText("");
    }

    //metodo que carga toda la informacion del usuario en esta actividad
    public void loadUserData(){
        User user = getIntent().getParcelableExtra("user");

        ImageView image = findViewById(R.id.toolbarPhotoImage);
        TextView text = findViewById(R.id.toolbarNameText);

        Utils.loadImageWithGlideSize(this, user.getProfileImageUrl(), image, 100, 100);
        text.setText(user.getName());

        //listener para la imagen de perfil del chat
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProfileImageClick();
            }
        });
    }

    //metodo que lleva al perfil del usuario del chat
    public void onProfileImageClick(){
        Bundle b = new Bundle();
        b.putParcelable("user", getIntent().getParcelableExtra("user"));
        Utils.goToActivity(ChatActivity.this, UserActivity.class, b, false);
    }

    //metodo para borrar la cuenta de usuario
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
                            Toast.makeText(ChatActivity.this,
                                    getText(R.string.user_deleted),
                                    Toast.LENGTH_SHORT).show();
                            Utils.goToActivity(ChatActivity.this, LoginActivity.class, null, true);
                        } else
                            Toast.makeText(getApplicationContext(),
                                    getText(R.string.user_not_deleted),
                                    Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton(getText(R.string.dialog_cancel), null);
        builder.show();
    }

    //metodo para cambiar la contraseña del usuario
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
                                    Toast.makeText(ChatActivity.this,
                                            getText(R.string.password_changed), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(ChatActivity.this,
                                            getText(R.string.password_not_changed), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        builder.setNegativeButton(getText(R.string.dialog_cancel), null);
        builder.show();
    }

    //metodo para cargar los datos en el navigation drawer
    public void loadDataToNavigationDrawer(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        //cargar nombre de usuario
        final TextView navUsername = headerView.findViewById(R.id.userName);
        manager.getUserName(new FirebaseManager.OnFirebaseLoadString() {
            @Override
            public void onResult(String string) {
                navUsername.setText(string);
            }
        });

        //cargar imagen de perfil
        final ProgressBar progreso = headerView.findViewById(R.id.progressBarNav);
        progreso.setVisibility(View.VISIBLE);
        final ImageView profilePhoto = headerView.findViewById(R.id.profilePhoto);
        manager.getUrlPhoto(new FirebaseManager.OnFirebaseLoadImage() {
            @Override
            public void onResult(Uri url) {
                Glide.with(getApplicationContext())
                        .load(url)
                        .apply(RequestOptions.circleCropTransform()).into(profilePhoto);
                profilePhoto.getLayoutParams().height = 250;
                progreso.setVisibility(View.INVISIBLE);
            }
        });

        //cargar nombre de la ciudad
        final TextView cityText = headerView.findViewById(R.id.cityText);
        manager.getCityName(new FirebaseManager.OnFirebaseLoadString() {
            @Override
            public void onResult(String string) {
                cityText.setText(string);
            }
        });

    }

    ///
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

}
