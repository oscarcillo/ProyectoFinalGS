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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    //variables firebase
    private FirebaseAuth mAuth;

    //views
    EditText emailRegisterText, passwordRegisterText;
    ProgressBar progreso;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //inicializar variables
        mAuth = FirebaseAuth.getInstance();
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
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                           Toast.makeText(getApplicationContext(),
                                   getResources().getString(R.string.user_registered),
                                   Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification();
                            //mAuth.signInWithEmailAndPassword(email, password);
                            goToEmailVerificationActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.user_not_registered),
                                    Toast.LENGTH_SHORT).show();
                        }
                        progreso.setVisibility(View.INVISIBLE);
                    }
                });
    }

    public void goToLoginEmailActivity(View v){
        Intent i = new Intent(this, LoginEmailActivity.class);
        startActivity(i);
    }

    public void goToEmailVerificationActivity(){
        Intent i = new Intent(this, EmailVerificationActivity.class);
        i.putExtra("email", email);
        i.putExtra("password", password);
        startActivity(i);
        finish();
    }
}
