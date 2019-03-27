package com.example.proyecto_final_gs;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    String email, password;

    Handler handler;
    boolean isActivated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        //
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        //recibir usuario y contraseña
        Intent i = getIntent();
        email = i.getStringExtra("email");
        password = i.getStringExtra("password");

        //comprobar que el email está verificado
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isActivated)
                    checkEmailVerification();
                handler.postDelayed(this, 2500);
            }
        }, 1000);  //the time is in miliseconds
    }

    public void checkEmailVerification(){
        mAuth.signInWithEmailAndPassword(email, password);
        if(user.isEmailVerified()) {
            goToMainActivity();
            isActivated = true;
        }
    }

    public void sendEmailAgain(View v){
        user.sendEmailVerification();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void goToMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        this.finish();
    }
}
