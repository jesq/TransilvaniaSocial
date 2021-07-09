package com.example.transilvaniasocial.posts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button MakePostButton;
    private ImageButton AddPhotoButton;
    private ImageView PostImage;
    private EditText PostText;
    private static final int GalleryPick = 1;
    private Uri ImageUri;
    private String downloadUrl;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private StorageReference postImageRef;
    private FirebaseFirestore db;
    private DocumentReference userRef;


    private String postDescription, saveCurrentDate, saveCurrentTime, postRandomName;
    private long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        postImageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(currentUserID);

        MakePostButton = (Button) findViewById(R.id.btn_make_post);
        AddPhotoButton = (ImageButton) findViewById(R.id.btn_add_image);
        PostImage = (ImageView) findViewById(R.id.iv_new_post);
        PostText = (EditText) findViewById(R.id.et_post_text);

        mToolbar = (Toolbar) findViewById(R.id.new_post_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Scrie o postare");

        AddPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        MakePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerifyPost(); //valideaza postarea inainte de a incarca datele introduse
            }
        });
    }

    private void VerifyPost() {
        postDescription = PostText.getText().toString();
        if(TextUtils.isEmpty(postDescription)){
            Toast.makeText(this, "Chiar nu ai nimic de spus?",Toast.LENGTH_SHORT).show();
        } else if(PostImage.getDrawable() == null){
            SavePost();
        }
        else {
            StoreImageToFirebase();
        }
    }

    private void StoreImageToFirebase() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calFordDate.getTime());
        postRandomName = saveCurrentDate + saveCurrentTime;

        Toast.makeText(PostActivity.this, postRandomName,Toast.LENGTH_SHORT).show();

        StorageReference filePath = postImageRef.child("post images").child(currentUserID + postRandomName + ".jpg");
        filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadUrl = uri.toString();
                        Toast.makeText(PostActivity.this, "Imaginea a fost incarcata",Toast.LENGTH_SHORT).show();
                        SavePost();
                    }
                });
            }
        });

    }

    private void SavePost() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document != null){
                        String userFaculty = document.getString("faculty");
                        Map<String, Object> post = new HashMap<>();
                        post.put("uid", currentUserID);
                        post.put("faculty", userFaculty);
                        post.put("date", saveCurrentDate);
                        post.put("time", saveCurrentTime);
                        post.put("description", postDescription);
                        post.put("postImage", downloadUrl);
                        post.put("created", FieldValue.serverTimestamp());
                        db.collection("posts").document(currentUserID + postRandomName).set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                OpenMainActivity();
                                Toast.makeText(PostActivity.this, "Postarea a fost publicata!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostActivity.this, "Postarea NU a fost publicata!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

    }

    private void OpenGallery() {
        Intent selectPhotoIntent = new Intent();
        selectPhotoIntent.setAction(Intent.ACTION_GET_CONTENT);
        selectPhotoIntent.setType("image/*");
        startActivityForResult(selectPhotoIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            PostImage.setImageURI(ImageUri);
        }
    }


    private void OpenMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


}