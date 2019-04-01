package com.example.proyecto_final_gs;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class SetUpActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    DatePickerDialog datePickerDialog;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference("users").child(user.getUid());

    //views
    EditText nameText;
    EditText birthdayText;
    ImageView profileImageView;
    Button continueButton;
    ProgressBar imageProgressBar;

    Uri photoUrl = null;

    //
    String name, birthday, photoUrlString, url;

    boolean skipActivity = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        //
        birthdayText = findViewById(R.id.birthdayText);
        nameText = findViewById(R.id.nameText);
        profileImageView = findViewById(R.id.profileImageView);
        continueButton = findViewById(R.id.continueButton);
        imageProgressBar = findViewById(R.id.imageProgressBar);
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
            ref.child("conf").child("setupactivity").setValue("true");
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

    ////////////////////////////////////
    //metodo que se activa al hacer click sobre la imagen
    public void showImageChooser(View v){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Elige una imagen"), 101);
    }

    /**
     * Mostrar la imagen seleccionada en la actividad
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Comprobar que la imagen se ha seleccionado correctamente
        if(requestCode == 101 && resultCode == RESULT_OK && data != null
                && data.getData()!=null){
            //almacenar la imagen en una variable
            photoUrl = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        photoUrl);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //subir la imagen
        uploadImageToFirebaseStorage();
    }

    /**
     * Método que sube imagenes de usuario al Storage de Firebase
     */
    public void uploadImageToFirebaseStorage(){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        url = "profilepics/"+System.currentTimeMillis() + ".jpg";
        final StorageReference riversRef = storageRef.child(url);
        UploadTask uploadTask = riversRef.putFile(photoUrl);

        continueButton.setEnabled(false);
        imageProgressBar.setVisibility(View.VISIBLE);
        profileImageView.setAlpha(0.3f);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("1", exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("1", "La imagen se ha subido a Firebase");
            }
        });
        //
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    photoUrl = Uri.parse(task.getResult().toString());
                    Log.e("URL:", ""+photoUrl);
                    continueButton.setEnabled(true);
                    imageProgressBar.setVisibility(View.INVISIBLE);
                    profileImageView.setAlpha(1f);
                }
            }
        });
    }
}
