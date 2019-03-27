package com.example.proyecto_final_gs;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void goToLoginEmailActivity(View v){
        Intent i = new Intent(this, LoginEmailActivity.class);
        startActivity(i);
    }
}
