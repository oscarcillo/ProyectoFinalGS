package com.example.proyecto_final_gs;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class SetUpActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    DatePickerDialog datePickerDialog;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    //views
    EditText nameText;
    EditText birthdayText;
    ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        //
        birthdayText = findViewById(R.id.birthdayText);
        nameText = findViewById(R.id.nameText);
        profileImageView = findViewById(R.id.profileImageView);
        //
         datePickerDialog = new DatePickerDialog(
                this, this, 2019, 1, 1);
        //insertar nombre de usuario si lo hay
        if(user.getDisplayName()!=null){
            nameText.setText(user.getDisplayName());
        }
        //obtener imagen del usuario
        Uri photoUrl = null;
        for (UserInfo profile : user.getProviderData()) {
            photoUrl = profile.getPhotoUrl();
        }
        if(photoUrl!=null)
            Glide.with(this).load(photoUrl).into(profileImageView);
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
    ///////////////////////////////////////////
    //////////////////////////////////////////

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        birthdayText.setText(dayOfMonth+"/"+(month+1)+"/"+year);
    }

    public void showDatePickerDialog(View v){
        datePickerDialog.show();
    }

    public void goToLoginActivity(){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
