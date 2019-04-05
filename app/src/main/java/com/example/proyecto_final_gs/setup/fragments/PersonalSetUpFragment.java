package com.example.proyecto_final_gs.setup.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.proyecto_final_gs.setup.OnFragmentInteractionListener;
import com.example.proyecto_final_gs.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class PersonalSetUpFragment extends Fragment {

    DatePickerDialog datePickerDialog;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference("users").child(user.getUid());

    //listener
    private OnFragmentInteractionListener mListener;

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

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_personal_set_up, container, false);

        //views import
        birthdayText = view.findViewById(R.id.birthdayText);
        nameText = view.findViewById(R.id.nameText);
        profileImageView = view.findViewById(R.id.profileImageView);
        continueButton = view.findViewById(R.id.continueButton);
        imageProgressBar = view.findViewById(R.id.imageProgressBar);

        //insertar nombre de usuario si lo hay
        if(user.getDisplayName()!=null){
            nameText.setText(user.getDisplayName());
        }
        //obtener imagen del usuario
        photoUrl = user.getPhotoUrl();

        if(photoUrl!=null)
            Glide.with(this).load(photoUrl).into(profileImageView);

        //listener para elegir imagen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooser(v);
            }
        });
        //listener para enviar los datos a firebase
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData(v);
            }
        });

        return view;
    }

    public void saveData(View v){
        if(nameText.getText().toString().equals("") || birthdayText.getText().toString().equals("")
            || photoUrl==null) {
            Toast.makeText(getContext(), getText(R.string.incomplete_data), Toast.LENGTH_SHORT).show();
        }else{
            ref.child("name").setValue(nameText.getText().toString());
            ref.child("birthday").setValue(birthdayText.getText().toString());
            ref.child("photoUrl").setValue("" + photoUrl);
            ref.child("conf").child("setupactivity").setValue("true");

            //cambiar de fragmento
            mListener.changeFragment(2);
        }
    }

    //metodo que se activa al hacer click sobre la imagen
    public void showImageChooser(View v){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Elige una imagen"), 101);
    }

    /**
     * Mostrar la imagen seleccionada en la actividad y la sube a firebase
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Comprobar que la imagen se ha seleccionado correctamente
        if(requestCode == 101 && data != null
                && data.getData()!=null){
            //almacenar la imagen en una variable
            photoUrl = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),
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

    // region Fragment changer
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }

    //endregion
}
