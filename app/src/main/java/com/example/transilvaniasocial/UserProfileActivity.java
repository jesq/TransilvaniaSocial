package com.example.transilvaniasocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private DocumentReference mUserRef;

    private Toolbar mToolBar;
    private TextView mFullName, mBio, mFaculty, mBirthday;
    private CircleImageView mProfileImage;
    private Button SendMessageButton;
    private String senderUserID, receiverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("selectedProfileKey").toString();
        mUserRef = fStore.collection("users").document(receiverUserId);

        mToolBar = findViewById(R.id.user_profile_activity_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFullName = findViewById(R.id.tv_name_user_profile_activity);
        mBio = findViewById(R.id.tv_bio_user_profile_activity);
        mFaculty = findViewById(R.id.tv_faculty_user_profile_activity);
        mBirthday = findViewById(R.id.tv_birthday_user_profile_activity);
        mProfileImage = findViewById(R.id.civ_picture_user_profile_activity);

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