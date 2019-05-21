package com.musyzian.firebase;

import android.location.Location;
import android.net.Uri;
import android.provider.ContactsContract;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
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
    private DatabaseReference chatsRef = db.getReference("chats");


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
     * Método que obtiene el id del usuario con la sesión iniciada
     */
    public String getUserId(){
        return mAuth.getUid();
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
     * Metodo que carga de la base de datos la descripcion a partir de un usuario
     * @param id
     * @param callback
     */
    public void getDescriptionByUserId(String id, final OnFirebaseLoadString callback){
        usersRef.child(id).child("description").addValueEventListener(new ValueEventListener() {
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
     * Método que devuelve un objeto usuario a partir de su Id
     * @param callback
     */
    public void loadUserByUId(final String id, final OnFirebaseLoadUser callback){
        usersRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String url = dataSnapshot.child("photoUrl").getValue(String.class);
                final String name = dataSnapshot.child("name").getValue(String.class);

                //calcular edad
                String age = dataSnapshot.child("birthday").getValue(String.class);
                age = age.substring(age.length() - 4);
                int year = Calendar.getInstance().get(Calendar.YEAR);
                year = year - Integer.parseInt(age);
                age = year+"";
                final String anios = age;
                //

                final String city = dataSnapshot.child("cityName").getValue(String.class);

                //cargar lista de instrumentos
                final List<String> instruments = new ArrayList<>();
                final List<String> artists = new ArrayList<>();
                for(int i = 0; i < dataSnapshot.child("instruments").getChildrenCount(); i++){
                    instruments.add(dataSnapshot.child("instruments").child(i+"").getValue(String.class));
                }
                //cargar lista de artistas
                for(DataSnapshot dataSnapshot1 : dataSnapshot.child("artists").getChildren()){
                    artists.add(dataSnapshot1.getValue(String.class));
                }

                //cargar localizacion
                final Location location = new Location("");
                location.setLatitude(Double.parseDouble(dataSnapshot.child("location").child("latitude").getValue(String.class)));
                location.setLongitude(Double.parseDouble(dataSnapshot.child("location").child("longitude").getValue(String.class)));

                getLocation(new OnFirebaseLoadLocation() {
                    @Override
                    public void onResult(Location loc) {
                        //calcular distancia entre usuarios
                        Float distance = loc.distanceTo(location);

                        User user = new User(Uri.parse(url), name, anios, city, instruments, artists, location, distance, id);

                        callback.onResult(user);
                    }
                });
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
     * Método que carga la lista con todos los usuarios de la aplicación
     * @param callback
     */
    public void loadUsersList(final int distanceFilter, final OnFirebaseLoadUsers callback){
        final List<User> users = new ArrayList<>();

        user = mAuth.getCurrentUser();

        //coger la localizacion del usuario con la sesion iniciada
        getLocation(new OnFirebaseLoadLocation() {
            @Override
            public void onResult(Location loc) {
                //coger la localizacion del usuario que tiene la sesion iniciada
                final Location locCurrentUser = loc;

                //cargar todos los datos de los usuarios
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isLoaded = false;
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            //comprobar que la configuracion de usuario esta completada
                            if(snapshot.child("conf").child("locationdescriptionactivity").getValue(String.class)!=null
                                    && !snapshot.getKey().equals(user.getUid())) {

                                //cargar localizacion
                                Location location = new Location("");
                                location.setLatitude(Double.parseDouble(snapshot.child("location").child("latitude").getValue(String.class)));
                                location.setLongitude(Double.parseDouble(snapshot.child("location").child("longitude").getValue(String.class)));
                                //calcular distancia entre usuarios
                                Float distance = locCurrentUser.distanceTo(location);

                                //comprobar si el usuario esta dentro de la distancia del filtro
                                if(distance/1000<distanceFilter){
                                    //crear listas
                                    List<String> instruments = new ArrayList<>();
                                    List<String> artists = new ArrayList<>();

                                    //cargar datos a la lista de usuarios
                                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                                    String name = snapshot.child("name").getValue(String.class);
                                    //calcular edad
                                    String age = snapshot.child("birthday").getValue(String.class);
                                    age = age.substring(age.length() - 4);
                                    int year = Calendar.getInstance().get(Calendar.YEAR);
                                    year = year - Integer.parseInt(age);
                                    age = year+"";
                                    //
                                    String city = snapshot.child("cityName").getValue(String.class);

                                    //cargar lista de instrumentos
                                    for(int i = 0; i < snapshot.child("instruments").getChildrenCount(); i++){
                                        instruments.add(snapshot.child("instruments").child(i+"").getValue(String.class));
                                    }
                                    //cargar lista de artistas
                                    for(DataSnapshot dataSnapshot1 : snapshot.child("artists").getChildren()){
                                        artists.add(dataSnapshot1.getValue(String.class));
                                    }

                                    //crear la lista de usuarios
                                    users.add(new User(Uri.parse(photoUrl), name, age, city, instruments, artists, location, distance, snapshot.getKey()));
                                }
                            }
                        }
                        //devolver la lista de usuarios con el listener
                        callback.onResult(users);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
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

    /**
     * Método que subre al almacenamiento de Firebase un archivo de audio
     * @param name Nombre del archivo de audio
     * @param audio Enlace URI con el archivo de audio
     * @param callback Listener que se activa al subir el audio
     */
    public void uploadAudioFirebaseStorage(final String name, final Uri audio, final OnFirebaseEventListener callback){
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        verifyAudioNumber(getUserId(), new OnFirebaseLoadInt() {
            @Override
            public void onResult(int number) {
                String url = "audios/"+mAuth.getCurrentUser().getUid()+"/"+(number+1);
                final StorageReference audioRef = storageRef.child(url);
                UploadTask uploadTask = audioRef.putFile(audio);

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("1", exception.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("1", "El audio se ha subido a Firebase");
                    }
                });
                //
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return audioRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        callback.onResult(task.isSuccessful());

                        //subir numero de canciones a la base de datos del usuario
                        final DatabaseReference ref = usersRef.child(getUserId()).child("audios");
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue(int.class)==null)
                                    ref.setValue(1);
                                else
                                    ref.setValue(dataSnapshot.getValue(int.class)+1);
                                ref.removeEventListener(this);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                });
            }
        });
    }

    /**
     * Metodo que comprueba cuantos audios ha subido el usuario actual
     * @param callback Listener que se activa al cargar el dato
     */
    public void verifyAudioNumber(final String id, final OnFirebaseLoadInt callback){
        usersRef.child(id).child("audios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int number;
                if(dataSnapshot.getValue(int.class)==null)
                    number = 0;
                else
                    number = dataSnapshot.getValue(int.class);
                callback.onResult(number);
                usersRef.child(id).child("audios").removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Método que guarda los datos necesarios del chat entre un usuario que envia un mensaje y otro que lo recibe
     * @param senderUser Id del usuario que envia el mensaje
     * @param receiverUser Id del usuario que recibe el mensaje
     */
    public void sendChatMessage(final String senderUser, final String receiverUser, final String message) {
        usersRef.child(senderUser).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chatId = "";
                //
                if(dataSnapshot.child(receiverUser).getValue(String.class)!=null){
                    //obtener el id de grupo
                    chatId = dataSnapshot.child(receiverUser).getValue(String.class);
                    //escribir el mensaje
                    chatsRef.child(chatId).child(System.currentTimeMillis()+","+senderUser).setValue(message);
                }else{
                    //Crear el id de grupo
                    chatId = chatsRef.push().getKey();
                    //escriber el id de grupo en los dos usuarios
                    usersRef.child(senderUser).child("chats").child(receiverUser).setValue(chatId);
                    usersRef.child(receiverUser).child("chats").child(senderUser).setValue(chatId);
                    //escribir el mensaje
                    chatsRef.child(chatId).child(System.currentTimeMillis()+","+senderUser).setValue(message);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Metodo para cargar la lista de chats de un usuario
     * @param callback
     */
    public void loadChatList(final OnFirebaseLoadChatList callback){

        final List<String> urls = new ArrayList<>();
        final List<String> username = new ArrayList<>();
        final List<String> lastMessage = new ArrayList<>();
        final List<String> uids = new ArrayList<>();

        usersRef.child(mAuth.getUid()).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String uid = snapshot.getKey();
                    uids.add(uid);
                    final String chatId = snapshot.getValue(String.class);

                    //comprobar el nombre de usuario a partir del id de usuario
                    usersRef.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            username.add(dataSnapshot.child("name").getValue(String.class));
                            urls.add(dataSnapshot.child("photoUrl").getValue(String.class));

                            //buscar ultima conversacion
                            chatsRef.child(chatId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String last = "";
                                    for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                        last = snapshot1.getValue(String.class);
                                    }
                                    lastMessage.add(last);
                                    callback.onResult(urls, username, lastMessage, uids);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void loadAudioList(final String userid, final OnFirebaseLoadInstruments callback){
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        verifyAudioNumber(userid, new OnFirebaseLoadInt() {
            @Override
            public void onResult(int number) {
                String url = "audios/"+userid;
                final StorageReference audioRef = storageRef.child(url);

                if(number==0)
                    return;

                final List<String> urls = new ArrayList<>();

                for(int i = 1; i <= number; i++){
                    audioRef.child(""+i).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            urls.add(""+uri);
                            callback.onResult(urls);
                        }
                    });
                }
            }
        });
    }

    /**
     * Método que carga toda los mensajes de una conversación entre dos usuarios
     */
    public void loadChatMessages(String idUser1, String idUser2, final OnFirebaseLoadChatMessages callback){

        final List<String> messages = new ArrayList<>();
        final List<Boolean> owner = new ArrayList<>();
        final List<String> time = new ArrayList<>();

        usersRef.child(idUser1).child("chats").child(idUser2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chatId = dataSnapshot.getValue(String.class);
                //cargar los mensajes de la conversacion
                if(chatId==null)
                    return;
                chatsRef.child(chatId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        messages.clear();
                        owner.clear();
                        time.clear();
                        //
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            messages.add(snapshot.getValue(String.class));

                            //comprobar de quien es el mensaje
                            String cadena = snapshot.getKey();
                            String[] parts = cadena.split(",");
                            if(parts[1].equals(mAuth.getUid()))
                                owner.add(true);
                            else
                                owner.add(false);

                            time.add(parts[0]);
                        }
                        callback.onResult(messages, owner, time);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
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

    public interface OnFirebaseLoadUsers {
        void onResult(List<User> users);
    }

    public interface OnFirebaseLoadUser {
        void onResult(User user);
    }

    public interface OnFirebaseLoadChatList {
        void onResult(List<String> urls, List<String> username, List<String> lastMessage, List<String> uid);
    }

    public interface OnFirebaseLoadChatMessages {
        void onResult(List<String> messages, List<Boolean> owner, List<String> time);
    }

    public interface OnFirebaseLoadInt {
        void onResult(int number);
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
