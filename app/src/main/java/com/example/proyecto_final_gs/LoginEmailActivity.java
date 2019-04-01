package com.example.proyecto_final_gs;

import android.content.Intent;
import android.opengl.Visibility;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginEmailActivity extends AppCompatActivity {

    //variable de autenticacion
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference("users");

    //views
    TextView emailText, passwordText;
    ProgressBar progreso;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        //inicializar variables
        mAuth = FirebaseAuth.getInstance();
        //
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        progreso = findViewById(R.id.progressBar);
    }

    public void createUserOrLogin(View v){
        email = emailText.getText().toString().trim();
        password = passwordText.getText().toString().trim();
        //validacion de los edittext
        if(email.isEmpty()){
            emailText.setError(getResources().getString(R.string.email_error_empty));
            emailText.requestFocus();
            return;
        }
        if(password.isEmpty()){
            passwordText.setError(getResources().getString(R.string.password_error_empty));
            passwordText.requestFocus();
            return;
        }
        if(password.length()<6){
            passwordText.setError(getResources().getString(R.string.password_error_length));
            passwordText.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailText.setError(getResources().getString(R.string.email_error_form));
            emailText.requestFocus();
            return;
        }
        //
        progreso.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    user = mAuth.getCurrentUser();
                    //comprobar si el mail está verificado
                    if(user.isEmailVerified())
                        verifyConf();
                    else
                        goToEmailVerificationActivity();

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(LoginEmailActivity.this,
                            getResources().getString(R.string.error_singin),
                            Toast.LENGTH_SHORT).show();
                }
                progreso.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void goToRegisterActivity(View v){
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
    }

    public void goToEmailVerificationActivity(){
        Intent i = new Intent(this, EmailVerificationActivity.class);
        i.putExtra("email", email);
        i.putExtra("password", password);
        startActivity(i);
        finish();
    }

    public void goToLocationDescriptionSetUpActivity(){
        Intent i = new Intent(this, LocationDescriptionSetUpActivity.class);
        startActivity(i);
        finish();
    }

    ///////////////////////////
    ////////////////////////

    public void goToSetupActivity(){
        Intent i = new Intent(this, SetUpActivity.class);
        i.putExtra("name", mAuth.getCurrentUser().getEmail());
        startActivity(i);
        finish();
    }

    public void goToMusicalSetUpActivity(){
        Intent i = new Intent(this, MusicalSetUpActivity.class);
        startActivity(i);
        finish();
    }

    public void goToArtistsSetUpActivity(){
        Intent i = new Intent(this, ArtistsSetUpActivity.class);
        startActivity(i);
        finish();
    }

    public void goToMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
    //////////////////////////
    /////////////////////

    public void verifyConf(){
        ref.child(mAuth.getCurrentUser().getUid()).child("conf").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String setupactivity = dataSnapshot.child("setupactivity").getValue(String.class);
                String musicalsetupactivity = dataSnapshot.child("musicalsetupactivity").getValue(String.class);
                String artistssetupactivity = dataSnapshot.child("artistssetupactivity").getValue(String.class);
                //
                if(artistssetupactivity!=null){
                    goToLocationDescriptionSetUpActivity();
                    return;
                }
                if(musicalsetupactivity!=null){
                    goToArtistsSetUpActivity();
                    return;
                }
                else if(setupactivity!=null) {
                    goToMusicalSetUpActivity();
                    return;
                }
                else
                    goToSetupActivity();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}
