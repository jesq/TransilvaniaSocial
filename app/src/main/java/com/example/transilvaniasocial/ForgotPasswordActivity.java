package com.example.transilvaniasocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private EditText EmailInput;
    private Button ResetPasswordBtn;
    private TextView ResetPasswordTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = findViewById(R.id.forgot_pw_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reseteaza parola");
        EmailInput = findViewById(R.id.et_forgot_pw_email);
        ResetPasswordBtn = findViewById(R.id.btn_forgot_pw);
        ResetPasswordTxtView = findViewById(R.id.tv_reset_pw);

        ResetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = EmailInput.getText().toString();
                if(TextUtils.isEmpty(userEmail)){
                    EmailInput.setError("Introdu o adresa de e-mail!");
                    EmailInput.requestFocus();
                }
                else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                ResetPasswordTxtView.setVisibility(View.VISIBLE);
                                Toast.makeText(ForgotPasswordActivity.this, "Linkul de resetare a parolei a fost trimis!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(ForgotPasswordActivity.this, "A aparut o eroare: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}