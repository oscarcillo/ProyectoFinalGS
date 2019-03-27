package com.example.proyecto_final_gs;

import android.content.Intent;
import android.opengl.Visibility;
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

public class LoginEmailActivity extends AppCompatActivity {

    //variable de autenticacion
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    //views
    TextView emailText, passwordText;
    ProgressBar progreso;

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
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
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
}
