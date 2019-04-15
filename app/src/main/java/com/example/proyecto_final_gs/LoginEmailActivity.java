package com.example.proyecto_final_gs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_final_gs.setup.SetUpActivity;
import com.musyzian.firebase.FirebaseManager;

public class LoginEmailActivity extends AppCompatActivity {

    //variable de autenticacion
    FirebaseManager manager;

    //views
    TextView emailText, passwordText;
    ProgressBar progreso;
    Button signinButton;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        //inicializar variables
        manager = FirebaseManager.get();
        //
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        progreso = findViewById(R.id.progressBar);
        signinButton = findViewById(R.id.singinButton);
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
        signinButton.setEnabled(false);
        //
        manager.signInEmailPassword(email, password, new FirebaseManager.OnFirebaseEventListener() {
            @Override
            public void onResult(Boolean success) {
                if (success) {
                    //comprobar si el mail está verificado
                    if(manager.isEmailVerified()) {
                        manager.isSetUpCompleted(new FirebaseManager.OnFirebaseEventListener() {
                            @Override
                            public void onResult(Boolean success) {
                                if(success)
                                    Utils.goToActivity(LoginEmailActivity.this, MainActivity.class, null, true);
                                else
                                    Utils.goToActivity(LoginEmailActivity.this, SetUpActivity.class,
                                            null, true);
                            }
                        });
                    }else{
                        Bundle b = new Bundle();
                        b.putString("email", email);
                        b.putString("password", password);
                        Utils.goToActivity(LoginEmailActivity.this, EmailVerificationActivity.class,
                                b,true);
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(LoginEmailActivity.this,
                            getText(R.string.error_singin), Toast.LENGTH_SHORT).show();
                }
                progreso.setVisibility(View.INVISIBLE);
                signinButton.setEnabled(true);
            }
        });
    }

    public void goToRegisterActivity(View v){
            Utils.goToActivity(LoginEmailActivity.this, RegisterActivity.class,
                    null, true);
    }
}