package com.example.transilvaniasocial.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.example.transilvaniasocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private TextView mFullName, mBio, mFaculty, mBirthday;
    private CircleImageView mProfileImage;

    private FirebaseFirestore fStore;
    private DocumentReference mUserRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        mUserRef = fStore.collection("users").document(mCurrentUserId);

        mToolBar = findViewById(R.id.profile_activity_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Profilul meu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFullName = findViewById(R.id.tv_name_profile_activity);
        mBio = findViewById(R.id.tv_bio_profile_activity);
        mFaculty = findViewById(R.id.tv_faculty_profile_activity);
        mBirthday = findViewById(R.id.tv_birthday_profile_activity);
        mProfileImage = findViewById(R.id.civ_picture_profile_activity);

        mUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null){
                        mBio.setText(documentSnapshot.getString("status"));
                        mFullName.setText(documentSnapshot.getString("fullName"));
                        mFaculty.setText(documentSnapshot.getString("faculty"));
                        mBirthday.setText(documentSnapshot.getString("birthday"));
                        Picasso.get().load(documentSnapshot.getString("profileImage")).into(mProfileImage);
                    }
                }
            }
        });

    }
}