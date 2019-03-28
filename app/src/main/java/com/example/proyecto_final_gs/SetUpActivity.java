package com.example.proyecto_final_gs;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SetUpActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    DatePickerDialog datePickerDialog;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference(user.getUid());

    //views
    EditText nameText;
    EditText birthdayText;
    ImageView profileImageView;
    Uri photoUrl = null;

    //
    String name, birthday, photoUrlString;

    boolean skipActivity = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        //
        birthdayText = findViewById(R.id.birthdayText);
        nameText = findViewById(R.id.nameText);
        profileImageView = findViewById(R.id.profileImageView);
        //comprobar si los datos ya estan introducidos para saltar a otra actividad
        checkData();
        //
         datePickerDialog = new DatePickerDialog(
                this, this, 2019, 1, 1);
        //insertar nombre de usuario si lo hay
        if(user.getDisplayName()!=null){
            nameText.setText(user.getDisplayName());
        }
        //obtener imagen del usuario
        photoUrl = user.getPhotoUrl();

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

    public void saveData(View v){
        if(nameText.getText().toString().equals("") || birthdayText.getText().toString().equals("")
            || photoUrl==null) {
            Toast.makeText(getApplicationContext(), getText(R.string.incomplete_data), Toast.LENGTH_SHORT).show();
        }else{
            ref.child("name").setValue(nameText.getText().toString());
            ref.child("birthday").setValue(birthdayText.getText().toString());
            ref.child("photoUrl").setValue("" + photoUrl);
            goToMusicalSetUpActivity();
        }
    }

    public void goToMusicalSetUpActivity(){
        Intent i = new Intent(this, MusicalSetUpActivity.class);
        startActivity(i);
        finish();
    }

    public void checkData(){
        ref.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   name = dataSnapshot.child("name").getValue(String.class);
                   if(name==null) skipActivity = false;
                   birthday = dataSnapshot.child("birthday").getValue(String.class);
                   if(birthday==null) skipActivity = false;
                   photoUrlString = dataSnapshot.child("photoUrl").getValue(String.class);
                   if(photoUrlString==null) skipActivity = false;
                   //
                    if(skipActivity)
                        goToMusicalSetUpActivity();
           }
           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) { }
       });
    }
}
