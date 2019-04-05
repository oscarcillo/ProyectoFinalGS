package com.example.proyecto_final_gs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.proyecto_final_gs.setup.SetUpActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference("users");

    GoogleSignInClient mGoogleSignInClient;

    ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //
        mAuth = FirebaseAuth.getInstance();
        //comprobar que el usuario esta logueado para saltar esta actividad
        if(mAuth.getCurrentUser()!=null){
            //verificar a que actividad de confgiuración tiene que ir el usuario
            Utils.goToActivity(LoginActivity.this, SetUpActivity.class,
                    null, true);
        }
        //
        progressBarLogin = findViewById(R.id.progressBarLogin);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // region google signin
    public void signIn(View v) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                //progressBarLogin.setVisibility(View.INVISIBLE);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("sdfgsdfg", "Google sign in failed", e);
                progressBarLogin.setVisibility(View.INVISIBLE);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        progressBarLogin.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Utils.goToActivity(LoginActivity.this, SetUpActivity.class,
                                    null, true);
                        } else {
                            // If sign in fails, display a message to the user.
                            progressBarLogin.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
    // endregion

    /*public void verifyConf(){
        ref.child(mAuth.getCurrentUser().getUid()).child("conf").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String setupactivity = dataSnapshot.child("setupactivity").getValue(String.class);
                String musicalsetupactivity = dataSnapshot.child("musicalsetupactivity").getValue(String.class);
                String artistssetupactivity = dataSnapshot.child("artistssetupactivity").getValue(String.class);
                //
                if(artistssetupactivity!=null){
                    Utils.goToActivity(LoginActivity.this, LocationDescriptionSetUpFragment.class,
                            null, true);
                    return;
                }
                if(musicalsetupactivity!=null){
                    Utils.goToActivity(LoginActivity.this, ArtistsSetUpFragment.class,
                            null, true);
                    return;
                }
                else if(setupactivity!=null) {
                    Utils.goToActivity(LoginActivity.this, MusicalSetUpFragment.class,
                            null, true);
                    return;
                }
                else {
                    Bundle bundle = new Bundle();
                    bundle.putString("name", mAuth.getCurrentUser().getEmail());
                    Utils.goToActivity(LoginActivity.this, PersonalSetUpFragment.class,
                            bundle, true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }*/

    //Método que se ejecuta al pulsar el botón de Log-in con cuenta de correo
    public void goToLoginEmailActivity(View v){
        Utils.goToActivity(LoginActivity.this, LoginEmailActivity.class,
                null, false);
    }

}
