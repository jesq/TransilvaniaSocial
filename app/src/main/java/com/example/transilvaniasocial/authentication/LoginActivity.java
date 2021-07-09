package com.example.transilvaniasocial.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.transilvaniasocial.MainActivity;
import com.example.transilvaniasocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    private EditText Email_et, Password_et; //datele introduse de utilizator pentru login
    private Button Login_btn; //butonul de login
    private TextView CreateAccount_tv, ForgotPassword; //trimitere catre RegisterActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Email_et = (EditText) findViewById(R.id.et_login_email);
        Password_et = (EditText) findViewById(R.id.et_login_pw);
        Login_btn = (Button) findViewById(R.id.btn_login);
        CreateAccount_tv = (TextView) findViewById(R.id.tv_login_register);
        ForgotPassword = findViewById(R.id.tv_login_forgot_pw);

        CreateAccount_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenRegisterActivity();
            }
        });
        
        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenForgotPasswordActivity();
            }
        });

        Login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });

    }

    private void OpenForgotPasswordActivity() {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null && mAuth.getCurrentUser().isEmailVerified()){
            mAuth.getCurrentUser().reload().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        Toast.makeText(LoginActivity.this, "Ai fost deconectat", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    userRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.exists()){
                                String currentUserFaculty = documentSnapshot.getString("faculty");
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainIntent.putExtra("currentUserFaculty", currentUserFaculty);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                            else{
                                OpenNewProfileActivity();
                            }
                        }
                    });
                }
            });
        }
    }

    private void LoginUser() {
        String email = Email_et.getText().toString().trim();
        String password = Password_et.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Completeaza adresa de e-mail!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Completeaza parola!", Toast.LENGTH_SHORT).show();
        }
        else{
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        if(mAuth.getCurrentUser().isEmailVerified()){
                            userRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
                            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if(documentSnapshot.exists()){
                                        OpenMainActivity();
                                    }
                                    else{
                                        OpenNewProfileActivity();
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Adresa de e-mail trebuie verificata!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, "Eroare: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void OpenNewProfileActivity() {
        Intent newProfileIntent = new Intent(LoginActivity.this, NewProfileActivity.class);
        newProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(newProfileIntent);
        finish();
    }

    private void OpenMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void OpenRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}