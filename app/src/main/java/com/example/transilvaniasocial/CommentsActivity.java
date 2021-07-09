package com.example.transilvaniasocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {
    private ImageButton PostComment;
    private EditText CommentInput;
    private RecyclerView CommentsList;
    private String PostKey;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference usersRef, postsRef;
    private DocumentReference userRef;
    private String currentUID;
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mToolbar = findViewById(R.id.comments_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Comentarii");
        PostKey = getIntent().getExtras().get("PostKey").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        postsRef = db.collection("posts").document(PostKey).collection("comments");


        PostComment = findViewById(R.id.ib_post_comment);
        CommentInput = findViewById(R.id.et_comments_add);

        Query SortComments = postsRef.orderBy("created");
        FirestoreRecyclerOptions<CommentsModel> options = new FirestoreRecyclerOptions.Builder<CommentsModel>()
                .setQuery(SortComments, CommentsModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<CommentsModel, CommentsViewHolder>(options) {
            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
                return new CommentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull CommentsModel model) {
                userRef = db.collection("users").document(model.getUid());
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot != null){
                                holder.setProfilePicture(documentSnapshot.getString("profileImage"));
                                holder.setFullName(documentSnapshot.getString("fullName"));
                            }
                        }
                    }
                });
                holder.setCommentText(model.getCommentText());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
            }

        };

        CommentsList = findViewById(R.id.rv_comments);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);
        CommentsList.setAdapter(adapter);


        PostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.document(currentUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot != null){
                                String fullName = documentSnapshot.getString("fullName");
                                ValidateComment(fullName);
                                CommentInput.setText("");
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        adapter.startListening();
        super.onStart();
    }

    @Override
    protected void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setCommentText(String commentText){
            TextView textComment = mView.findViewById(R.id.tv_comment_text);
            textComment.setText(commentText);
        }

        public void setFullName(String fullName){
            TextView commentFullName = mView.findViewById(R.id.tv_comment_fullName);
            commentFullName.setText(fullName);
        }

        public void setDate(String date){
            TextView commentDate = mView.findViewById(R.id.tv_comment_date);
            commentDate.setText(date);
        }

        public void setTime(String time){
            TextView commentTime = mView.findViewById(R.id.tv_comment_time);
            commentTime.setText(time);
        }

        public void setProfilePicture(String profilePicture){
            CircleImageView commentProfilePicture = mView.findViewById(R.id.civ_comment);
            Picasso.get().load(profilePicture).into(commentProfilePicture);
        }
    }

    private void ValidateComment(String fullName) {
        String commentText = CommentInput.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)){
            CommentInput.setError("Nu poti lasa un comentariu gol!");
            CommentInput.requestFocus();
        }
        else{
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String saveCurrentDate = currentDate.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calendarTime.getTime());

            final String RandomKey = currentUID + saveCurrentDate + saveCurrentTime;

            Map<String, Object> comment = new HashMap<>();
            comment.put("uid", currentUID);
            comment.put("commentText", commentText);
            comment.put("date", saveCurrentDate);
            comment.put("time", saveCurrentTime);
            comment.put("created", FieldValue.serverTimestamp());
            postsRef.document(RandomKey).set(comment).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CommentsActivity.this, "Comentariul NU a fost publicat!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}