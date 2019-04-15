package com.example.proyecto_final_gs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.proyecto_final_gs.setup.SetUpActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.musyzian.firebase.FirebaseManager;

public class LoginActivity extends AppCompatActivity {


    FirebaseManager manager;

    GoogleSignInClient mGoogleSignInClient;

    ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        manager = FirebaseManager.get();
        //
        //comprobar que el usuario esta logueado para saltar esta actividad
        if(manager.userIsSigned()){
            setContentView(R.layout.default_layout);
            //verificar a que actividad de confgiuración tiene que ir el usuario
            if(manager.isEmailVerified())
                manager.isSetUpCompleted(new FirebaseManager.OnFirebaseEventListener() {
                    @Override
                    public void onResult(Boolean success) {
                        if(success)
                        if(success)
                            Utils.goToActivity(LoginActivity.this, MainActivity.class,
                                    null, true);
                        else
                            Utils.goToActivity(LoginActivity.this, SetUpActivity.class,
                                    null, true);
                    }
                });
            else {
                manager.signOut();
                setContentView(R.layout.activity_login);
            }
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

        manager.signInGoogle(acct.getIdToken(), new FirebaseManager.OnFirebaseEventListener() {
            @Override
            public void onResult(Boolean success) {
                if (success) {
                    manager.isSetUpCompleted(new FirebaseManager.OnFirebaseEventListener() {
                        @Override
                        public void onResult(Boolean successs) {
                            if(successs)
                                Utils.goToActivity(LoginActivity.this, MainActivity.class,
                                        null, true);
                            else
                                Utils.goToActivity(LoginActivity.this, SetUpActivity.class,
                                        null, true);
                        }
                    });
                } else {
                    progressBarLogin.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    //endregion

    //Método que se ejecuta al pulsar el botón de Log-in con cuenta de correo
    public void goToLoginEmailActivity(View v){
        Utils.goToActivity(LoginActivity.this, LoginEmailActivity.class,
                null, false);
    }

}
