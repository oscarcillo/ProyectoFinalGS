package com.example.proyecto_final_gs;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.proyecto_final_gs.setup.SetUpActivity;
import com.example.proyecto_final_gs.setup.fragments.PersonalSetUpFragment;

import com.musyzian.firebase.FirebaseManager;

public class EmailVerificationActivity extends AppCompatActivity {

    FirebaseManager manager;

    String email, password;

    Handler handler;
    boolean isActivated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        //
        manager = FirebaseManager.get();
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
                handler.postDelayed(this, 1800);
            }
        }, 1000);  //the time is in miliseconds
    }

    public void checkEmailVerification(){
        manager.signInEmailPassword(email, password, new FirebaseManager.OnFirebaseEventListener() {
            @Override
            public void onResult(Boolean success) {
                if(manager.isEmailVerified()) {
                    Utils.goToActivity(EmailVerificationActivity.this, SetUpActivity.class,
                            null, true);
                    isActivated = true;
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.email_verified), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void sendEmailAgain(View v){
        manager.sendEmailVerification();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
