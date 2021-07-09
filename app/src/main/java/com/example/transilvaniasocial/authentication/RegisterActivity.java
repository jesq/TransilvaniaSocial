package com.example.transilvaniasocial.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.transilvaniasocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[!@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{8,}" +                // at least 4 characters
                    "$");

    private FirebaseAuth mAuth;

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button BtnRegisterAccount;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText) findViewById(R.id.et_register_email);
        UserPassword = (EditText) findViewById(R.id.et_register_pw);
        UserConfirmPassword = (EditText) findViewById(R.id.et_register_confirm_pw);
        BtnRegisterAccount = (Button) findViewById(R.id.btn_register);

        BtnRegisterAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        boolean validEmail = true, validPw = true, validConfirmPw = true;
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirm_pw = UserConfirmPassword.getText().toString();
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            UserEmail.setError("Introdu o adresa de e-mail valida!");
            UserEmail.requestFocus();
            validEmail = false;
        }
        if(password.isEmpty() || !PASSWORD_PATTERN.matcher(password).matches()){
            UserPassword.setError("Parola trebuie sa aiba minim 8 caractere si 1 caracter special");
            UserPassword.requestFocus();
            validPw = false;
        }
        if(!password.equals(confirm_pw)){
            UserConfirmPassword.setError("Parolele nu se potrivesc!");
            UserConfirmPassword.requestFocus();
            validConfirmPw = false;
        }
        if (validEmail && validPw && validConfirmPw){
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                 if(task.isSuccessful()){
                                     Toast.makeText(RegisterActivity.this, "Contul a fost creat! Verifica adresa de e-mail!", Toast.LENGTH_LONG).show();
                                     OpenLoginActivity();
                                 }
                                 else{
                                     String message = task.getException().getMessage();
                                     Toast.makeText(RegisterActivity.this, "A aparut o eroare: " + message, Toast.LENGTH_LONG).show();
                                 }
                            }
                        });
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Contul nu poate sa fie creat: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void OpenLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

}