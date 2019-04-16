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
import com.bumptech.glide.request.RequestOptions;
import com.example.proyecto_final_gs.MainActivity;
import com.example.proyecto_final_gs.Utils;
import com.example.proyecto_final_gs.setup.OnFragmentInteractionListener;
import com.example.proyecto_final_gs.R;
import com.google.android.gms.tasks.Task;
import com.musyzian.firebase.FirebaseManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PersonalSetUpFragment extends Fragment {

    DatePickerDialog datePickerDialog;

    FirebaseManager manager;

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

        //firebase manager
        manager = FirebaseManager.get();

        //views import
        birthdayText = view.findViewById(R.id.birthdayText);
        nameText = view.findViewById(R.id.nameText);
        profileImageView = view.findViewById(R.id.profileImageView);
        continueButton = view.findViewById(R.id.continueButton);
        imageProgressBar = view.findViewById(R.id.imageProgressBar);

        //insertar nombre de usuario si lo hay
        manager.getUserName(new FirebaseManager.OnFirebaseLoadString() {
            @Override
            public void onResult(String string) {
                if(string!=null)
                    nameText.setText(string);
            }
        });
        //obtener imagen del usuario
        imageProgressBar.setVisibility(View.VISIBLE);
        manager.getUrlPhoto(new FirebaseManager.OnFirebaseLoadImage() {
            @Override
            public void onResult(Uri url) {
                photoUrl = url;
                //cargar la imagen de perfil
                if(photoUrl!=null)
                    Glide.with(getActivity())
                            .load(photoUrl)
                            .apply(RequestOptions.circleCropTransform()).into(profileImageView);
                imageProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        //obtener fecha de nacimiento del usuario si la tiene
        manager.getBirthday(new FirebaseManager.OnFirebaseLoadString() {
            @Override
            public void onResult(String string) {
                if(string !=null)
                    birthdayText.setText(string);
            }
        });

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
        //validar la entrada de datos de la fecha de nacimiento
        if(!validateDate(birthdayText.getText().toString())){
            Toast.makeText(getActivity(), getText(R.string.valid_date), Toast.LENGTH_SHORT).show();
            return;
        }
        //
        if(nameText.getText().toString().equals("") || birthdayText.getText().toString().equals("")
            || photoUrl==null) {
            Toast.makeText(getContext(), getText(R.string.incomplete_data), Toast.LENGTH_SHORT).show();
        }else{
            manager.uploadPersonalData(nameText.getText().toString(),
                    birthdayText.getText().toString(),
                    photoUrl.toString());

            //comprobar si debe cambiar de fragmento o volver al menu de opciones
            Intent i = getActivity().getIntent();
            String fragment = i.getStringExtra("fragment");
            if(fragment!=null)
                Utils.goToActivity(getActivity(), MainActivity.class,
                        null, true);
            else
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
                Glide.with(getActivity())
                        .load(bitmap)
                        .apply(RequestOptions.circleCropTransform()).into(profileImageView);
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
        continueButton.setEnabled(false);
        imageProgressBar.setVisibility(View.VISIBLE);
        profileImageView.setAlpha(0.3f);

        url = "profilepics/"+System.currentTimeMillis() + ".jpg";

        //subir la imagen a firebase storage
        manager.uploadImageFirebaseStorage(url, photoUrl, new FirebaseManager.OnFirebaseUploadImage() {
            @Override
            public void onResult(Task task) {
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

    public boolean validateDate(String dateText){
        /* Check if date is 'null' */
        if (dateText.trim().equals(""))
        {
            return true;
        }
        /* Date is not 'null' */
        else
        {
            /*
             * Set preferred date format,
             * For example MM-dd-yyyy, MM.dd.yyyy,dd.MM.yyyy etc.*/
            SimpleDateFormat sdfrmt = new SimpleDateFormat("dd/MM/yyyy");
            sdfrmt.setLenient(false);
            /* Create Date object
             * parse the string into date
             */
            try
            {
                Date javaDate = sdfrmt.parse(dateText);
                System.out.println(dateText+" is valid date format");
            }
            /* Date format is invalid */
            catch (ParseException e)
            {
                System.out.println(dateText+" is Invalid Date format");
                return false;
            }
            /* Return true if date format is valid */
            return true;
        }
    }
}
