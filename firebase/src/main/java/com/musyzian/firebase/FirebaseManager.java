package com.musyzian.firebase;

import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class FirebaseManager {

    private static FirebaseManager INSTANCE;

    public static FirebaseManager get(){
        if(INSTANCE == null)
            INSTANCE = new FirebaseManager();
        return INSTANCE;
    }

    private FirebaseManager(){
        mAuth = FirebaseAuth.getInstance();
    }

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    private DatabaseReference usersRef = db.getReference("users");
    private DatabaseReference instrumentsRef = db.getReference("instruments");


    //METHODS
    /**
     * Indica si el usuario existe
     * @return Booleano que indica si el usuario tiene sesión iniciado o no
     */
    public boolean userIsSigned(){
        if(mAuth.getCurrentUser()==null)
            return false;
        else
            return true;
    }

    /**
     * Indica si el email del usuario está verificado
     * @return Booleano que indica si el email está verificado o no
     */
    public boolean isEmailVerified(){
        user = mAuth.getCurrentUser();
        if(user.isEmailVerified())
            return true;
        else
            return false;
    }

    /**
     * Método que comprueba en la base de datos si la cuenta actual es de Google
     */
    public void isGoogleAccount(final OnFirebaseEventListener callback){
        user = mAuth.getCurrentUser();
        usersRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("googleAccount").getValue()!=null)
                    callback.onResult(true);
                else
                    callback.onResult(false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Indica si la configuración inicial de los datos del usuario está terminada o no
     * @param callback Listener que se activa al comprobar si la configuración está terminada o no
     */
    public void isSetUpCompleted(final OnFirebaseEventListener callback){
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!userIsSigned())
                    return;
                String resultado = dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("conf")
                        .child("locationdescriptionactivity").getValue(String.class);
                if(resultado!=null)
                    callback.onResult(true);
                else
                    callback.onResult(false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Inicia sesión con cuenta de Google
     * @param token Token de Google
     * @param callbacker Listener que se activa al completar el inicio de sesión
     */
    public void signInGoogle(String token, final OnFirebaseEventListener callbacker){
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        usersRef.child(mAuth.getCurrentUser().getUid()).child("googleAccount").setValue("true");
                        callbacker.onResult(task.isSuccessful());
                    }
                });
    }

    /**
     * Inicia sesión de usuario con el email y la contras
     * @param email Email
     * @param password Contraseñs
     * @param callback Listener que se activa al completarse el inicio de sesión
     */
    public void signInEmailPassword(String email, String password, final OnFirebaseEventListener callback){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       callback.onResult(task.isSuccessful());
                    }
                });
    }

    /**
     * Envía un email de verificación al correo asociado a la cuenta de usuario
     */
    public void sendEmailVerification(){
        user = mAuth.getCurrentUser();
        user.sendEmailVerification();
    }

    /**
     * Cierra la sesión de usuario
     */
    public void signOut(){
        mAuth.signOut();
    }

    /**
     * Método que elimina el usuario que tiene la sesión iniciada en la aplicación en ese momento
     * @param callback Listener que se activa cuando el borrado de usuario se ejecuta correctamente
     */
    public void deleteUser(boolean isGoogleAccount, String password, final OnFirebaseEventListener callback){
        if(isGoogleAccount) {
            usersRef.child(user.getUid()).removeValue();
            callback.onResult(true);
            return;
        }
        //
        user = mAuth.getCurrentUser();
        AuthCredential authCredential;
        //
        authCredential = EmailAuthProvider.getCredential(user.getEmail(), password);
        //reautenticar usuario para poder eliminarlo
        user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //eliminar los datos del usuario de la base de datos
                    usersRef.child(user.getUid()).removeValue();

                    //eliminar cuenta de usuario
                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                mAuth.signOut();
                                callback.onResult(true);
                            } else
                                callback.onResult(false);
                        }
                    });
                }
                else
                    callback.onResult(false);
            }
        });
    }

    /**
     * Método que cambia la contraseña de la cuenta de usuario
     * @param oldPassword Cadena con la contraseña antigua
     * @param newPassword Cadena con la nueva contraseña
     * @param callback Listener que se activa al actualizar la contraseña
     */
    public void changePassword(final String oldPassword, final String newPassword, final OnFirebaseEventListener callback){
        user = mAuth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        //reautenticar usuario para poder eliminarlo
        user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            callback.onResult(task.isSuccessful());
                        }
                    });
                }
                else
                    callback.onResult(false);
            }
        });
    }

    /**
     * Obtiene el nombre de usuario asociado a la cuenta google
     * @return Cadena con el nombre de usuario
     */
    public void getUserName(final OnFirebaseLoadString callback){
            usersRef.child(mAuth.getCurrentUser().getUid()).child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null){
                        callback.onResult(dataSnapshot.getValue().toString());
                        return;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        callback.onResult(mAuth.getCurrentUser().getDisplayName());
}

    /**
     * Obtiene la URL de la foto de perfil, dependiendo de la cuenta de Google y si
     * se ha cambiado la foto de perfil o sigue siendo la de Google
     * @param callback Listener que se activa al cargar la URL de la imagen de perfil
     */
    public void getUrlPhoto(final OnFirebaseLoadImage callback){
        user = mAuth.getCurrentUser();
        //comprobar si el usuario tiene foto de perfil en la base de datos
        usersRef.child(user.getUid()).child("photoUrl").addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.getValue()!=null) {
                   callback.onResult(Uri.parse(dataSnapshot.getValue().toString()));
                   return;
               }
           }
           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        callback.onResult(mAuth.getCurrentUser().getPhotoUrl());
    }

    /**
     * Método que carga la fecha de nacimiento del usuario de la base de datos
     * @param callback Listener que se activa al cargar los datos
     */
    public void getBirthday(final OnFirebaseLoadString callback){
        user = mAuth.getCurrentUser();
        usersRef.child(user.getUid()).child("birthday").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                    callback.onResult(dataSnapshot.getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Método que carga de la base de datos la descripción del usuario
     * @param callback Listener que se activa al cargar los datos
     */
    public void getDescription(final OnFirebaseLoadString callback){
        user = mAuth.getCurrentUser();
        usersRef.child(user.getUid()).child("description").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                    callback.onResult(dataSnapshot.getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Método que carga el nombre de la ciudad de la base de datos
     * @param callback Listener que se activa al cargar los datos
     */
    public void getCityName(final OnFirebaseLoadString callback){
        user = mAuth.getCurrentUser();
        usersRef.child(user.getUid()).child("cityName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                    callback.onResult(dataSnapshot.getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Método que carga las coordenadas del dispositivo de la base de datos
     * @param callback Listener que se activa cuando se cargan los datos y devuelve un objeto
     *                 Location con la localización del dispositivo
     */
    public void getLocation(final OnFirebaseLoadLocation callback){
        user = mAuth.getCurrentUser();
        final Location loc = new Location("");
        //
        usersRef.child(user.getUid()).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("latitude").getValue(String.class)!=null){
                    loc.setLatitude(Double.parseDouble(dataSnapshot.child("latitude").getValue(String.class)));
                    loc.setLongitude(Double.parseDouble(dataSnapshot.child("longitude").getValue(String.class)));
                    callback.onResult(loc);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Crea un usuario a partir de un email y contraseña
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param callback Listener que se activa al crear el usuario
     */
    public void createUserEmailPassword(String email, String password, final OnFirebaseEventListener callback){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        callback.onResult(task.isSuccessful());
                    }
                });
    }

    /**
     * Comprueba fragmento de configuración debe cargar
     * @param callback Listener que se activa al recuperar los datos de la base de datos
     */
    public void fragmentNavigation(final OnFragmentNavigation callback){
        usersRef.child(mAuth.getCurrentUser().getUid()).child("conf").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String setupactivity = dataSnapshot.child("setupactivity").getValue(String.class);
                String musicalsetupactivity = dataSnapshot.child("musicalsetupactivity").getValue(String.class);
                String artistssetupactivity = dataSnapshot.child("artistssetupactivity").getValue(String.class);
                String locationdescriptionactivity = dataSnapshot.child("locationdescriptionactivity").getValue(String.class);
                //

                if(locationdescriptionactivity!=null){
                    callback.onResult(NAVIGATION.MainActivity);
                    return;
                }
                if(artistssetupactivity!=null){
                    callback.onResult(NAVIGATION.LocationDescriptionSetUp);
                    return;
                }
                if(musicalsetupactivity!=null){
                    callback.onResult(NAVIGATION.ArtistsSetUp);
                    return;
                }
                else if(setupactivity!=null) {
                    callback.onResult(NAVIGATION.MusicalSetUp);
                    return;
                }
                else {
                    callback.onResult(NAVIGATION.PersonalSetUp);
                    return;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Método que sube a la base de datos la información personal del usuario
     * @param name Nombre del usuario
     * @param birth Fecha de nacimiento del usuario
     * @param url Url de la imgaen del usuario
     */
    public void uploadPersonalData(String name, String birth, String url){
        user = mAuth.getCurrentUser();
        //subir información personal
        usersRef.child(user.getUid()).child("name").setValue(name);
        usersRef.child(user.getUid()).child("birthday").setValue(birth);
        usersRef.child(user.getUid()).child("photoUrl").setValue(url);
        //subir estado de la configuración del usuario
        usersRef.child(user.getUid()).child("conf").child("setupactivity").setValue("true");
    }

    /**
     * Método que sube a la base de datos los artistas seleccionados por el usuario
     * @param artistsChoosen Lista de artistas seleccionados por el usuario
     */
    public void uploadArtistsData(List<String> artistsChoosen){
        user = mAuth.getCurrentUser();
        usersRef.child(user.getUid()).child("artists").removeValue();
        usersRef.child(user.getUid()).child("conf").child("artistssetupactivity").setValue("true");
        //subir los artistas elegidos del arraylist
        for(int i = 0; i < artistsChoosen.size();i++)
            usersRef.child(user.getUid()).child("artists").push().setValue(artistsChoosen.get(i)
                    .replace("\"", ""));
    }

    /**
     * Método que sube a la base de datos los instrumentos seleccionados por el usuario
     * @param choosenInstruments Lista de instrumentos seleccionados por el usuario
     */
    public void uploadInstrumentsData(List<String> choosenInstruments){
        user = mAuth.getCurrentUser();
        DatabaseReference usersRefins = usersRef.child(user.getUid()).child("instruments");
        //borrar primero los datos anteriores
        usersRefins.removeValue();
        //Insertar los nuevos valores
        for(int i = 0;i<choosenInstruments.size();i++)
            usersRefins.child(""+i).setValue(choosenInstruments.get(i));
        //subir el dato de la configuracion
        DatabaseReference ref = db.getReference("users").child(user.getUid()).child("conf").child("musicalsetupactivity");
        ref.setValue("true");
    }

    /**
     * Método que subre a la base de datos la localización y descripción personal de usuario
     * @param description Cadena con la descripción del usuario
     * @param cityName Cadena con el nombre de la ciudad
     * @param loc Objecto Location con la localización del usuario
     */
    public void uploadLocationDescriptionData(String description, String cityName, Location loc){
        user = mAuth.getCurrentUser();
        //subir los datos a Firebase database
        usersRef.child(user.getUid()).child("description").setValue(description);
        usersRef.child(user.getUid()).child("cityName").setValue(cityName);
        //subir localización
        usersRef.child(user.getUid()).child("location").child("latitude").setValue(""+loc.getLatitude());
        usersRef.child(user.getUid()).child("location").child("longitude").setValue(""+loc.getLongitude());
        //subir estado de la configuracion
        usersRef.child(user.getUid()).child("conf").child("locationdescriptionactivity").setValue("true");
    }

    /**
     * Método que carga la lista general de instrumentos de la base de datos
     * @param callback Listener que se activa al cargar la lista de instrumentos
     */
    public void loadInstrumentsList(final OnFirebaseLoadInstruments callback){
        final List<String> instrumentsList = new ArrayList<>();
        instrumentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                instrumentsList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                    instrumentsList.add(snapshot.getValue(String.class));
                callback.onResult(instrumentsList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Método que carga los instrumentos ya seleccionados del usuario actual
     * @param callback Listener que se activa cuando los datos son cargados
     */
    public void loadChoosenInstrumentsList(final OnFirebaseLoadInstruments callback){
        final List<String> choosenInstruments = new ArrayList<>();
        usersRef.child(mAuth.getCurrentUser().getUid()).child("instruments")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                choosenInstruments.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                    choosenInstruments.add(snapshot.getValue(String.class));
                callback.onResult(choosenInstruments);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Método que carga la lista de artistas seleccionados por el usuario
     * @param callback Listener que se activa al cargar la lista de artistas
     */
    public void loadArtistsList(final OnFirebaseLoadInstruments callback){
        final List<String> artists = new ArrayList<>();
        usersRef.child(mAuth.getCurrentUser().getUid()).child("artists")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        artists.clear();
                        for(DataSnapshot snapshot: dataSnapshot.getChildren())
                            artists.add(snapshot.getValue(String.class));
                        callback.onResult(artists);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    /**
     * Método que sube al almacenamiento de Firebase la imagen de perfil de usuario
     * @param url Nombre de la imagen
     * @param photoUrl Uri donde se guardará la imagen, en este caso será el Storage de Firebase
     * @param callback Listener que se activa cuando se sube la imagen
     */
    public void uploadImageFirebaseStorage(String url, Uri photoUrl, final OnFirebaseUploadImage callback){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        url = "profilepics/"+System.currentTimeMillis() + ".jpg";
        final StorageReference riversRef = storageRef.child(url);
        UploadTask uploadTask = riversRef.putFile(photoUrl);

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
                callback.onResult(task);
            }
        });
    }


    //region *INTERFACES*
    public interface OnFirebaseEventListener{
        void onResult(Boolean success);
    }

    public interface OnFragmentNavigation{
        void onResult(NAVIGATION nav);
    }

    public interface OnFirebaseUploadImage{
        void onResult(Task task);
    }

    public interface OnFirebaseLoadInstruments{
        void onResult(List<String> instruments);
    }

    public interface OnFirebaseLoadImage{
        void onResult(Uri url);
    }

    public interface OnFirebaseLoadString {
        void onResult(String string);
    }

    public interface OnFirebaseLoadLocation {
        void onResult(Location loc);
    }
    //endregion


    public enum NAVIGATION{
        ArtistsSetUp,
        PersonalSetUp,
        MusicalSetUp,
        LocationDescriptionSetUp,
        MainActivity
    }
}