package com.example.proyecto_final_gs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.musyzian.firebase.FirebaseManager;

public class RegisterActivity extends AppCompatActivity {

    //variables firebase
    FirebaseManager manager;

    //views
    EditText emailRegisterText, passwordRegisterText;
    ProgressBar progreso;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //inicializar variables
        manager = FirebaseManager.get();
        //
        emailRegisterText = findViewById(R.id.emailRegisterText);
        passwordRegisterText = findViewById(R.id.passwordEmailText);
        //
        progreso = findViewById(R.id.progressBarRegister);
    }

    public void signUpNewUser(View v){
        email = emailRegisterText.getText().toString().trim();
        password = passwordRegisterText.getText().toString().trim();
        //validacion de los edittext
        if(email.isEmpty()){
            emailRegisterText.setError(getResources().getString(R.string.email_error_empty));
            emailRegisterText.requestFocus();
            return;
        }
        if(password.isEmpty()){
            passwordRegisterText.setError(getResources().getString(R.string.password_error_empty));
            passwordRegisterText.requestFocus();
            return;
        }
        if(password.length()<6){
            passwordRegisterText.setError(getResources().getString(R.string.password_error_length));
            passwordRegisterText.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailRegisterText.setError(getResources().getString(R.string.email_error_form));
            emailRegisterText.requestFocus();
            return;
        }
        //
        progreso.setVisibility(View.VISIBLE);

        manager.createUserEmailPassword(email, password, new FirebaseManager.OnFirebaseEventListener() {
            @Override
            public void onResult(Boolean success) {
                if (success) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(getApplicationContext(),
                            getText(R.string.user_registered),
                            Toast.LENGTH_SHORT).show();
                    //enviar email de verificación
                    manager.sendEmailVerification();
                    //ir a la actividad de verificación de email
                    Bundle b = new Bundle();
                    b.putString("email", email);
                    b.putString("password", password);
                    Utils.goToActivity(RegisterActivity.this, EmailVerificationActivity.class,
                            b, true);
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(getApplicationContext(),
                            getText(R.string.user_not_registered),
                            Toast.LENGTH_SHORT).show();
                }
                progreso.setVisibility(View.INVISIBLE);
            }
        });
    }

}
