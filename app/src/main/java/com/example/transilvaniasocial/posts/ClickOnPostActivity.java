package com.example.transilvaniasocial.posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.transilvaniasocial.MainActivity;
import com.example.transilvaniasocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ClickOnPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescr;
    private Button DeletePost, EditPost;
    private String PostKey, currentUserId, dbUID;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference postRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_on_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        PostKey = getIntent().getExtras().get("PostKey").toString();
        db = FirebaseFirestore.getInstance();
        postRef = db.collection("posts").document(PostKey);

        PostImage = findViewById(R.id.iv_clickOnPost);
        PostDescr = findViewById(R.id.tv_clickOnPost);
        DeletePost = findViewById(R.id.btn_clickOnPost_delete);
        EditPost = findViewById(R.id.btn_clickOnPost_edit);

        DeletePost.setVisibility(View.INVISIBLE);
        EditPost.setVisibility(View.INVISIBLE);

        postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null){
                        String postDescription = documentSnapshot.getString("description");
                        PostDescr.setText(postDescription);
                        Picasso.get().load(documentSnapshot.getString("postImage")).into(PostImage);
                        dbUID = documentSnapshot.getString("uid");

                        if(currentUserId.equals(dbUID)){
                            DeletePost.setVisibility(View.VISIBLE);
                            EditPost.setVisibility(View.VISIBLE);
                        }

                        EditPost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditCurrentPost(postDescription);
                            }
                        });
                    }
                }
            }
        });
        DeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });
    }

    private void EditCurrentPost(String postDescription) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickOnPostActivity.this);
        builder.setTitle("Editeaza postarea: ");

        final EditText inputField = new EditText(ClickOnPostActivity.this);
        inputField.setText(postDescription);
        builder.setView(inputField);

        builder.setPositiveButton("Actualizeaza", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postRef.update("description", inputField.getText().toString());
                Toast.makeText(ClickOnPostActivity.this, "Postarea a fost actualizata!", Toast.LENGTH_SHORT).show();
                OpenMainActivity();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private void DeleteCurrentPost() {
        postRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ClickOnPostActivity.this, "Postarea a fost stearsa!", Toast.LENGTH_SHORT).show();
                OpenMainActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ClickOnPostActivity.this, "Postarea nu s-a putut sterge!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void OpenMainActivity() {
        Intent mainIntent = new Intent(ClickOnPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
}