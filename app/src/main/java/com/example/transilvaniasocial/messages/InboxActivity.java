package com.example.transilvaniasocial.messages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.transilvaniasocial.MainActivity;
import com.example.transilvaniasocial.R;
import com.example.transilvaniasocial.model.InboxModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class InboxActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView inboxView;
    FirestoreRecyclerAdapter inboxAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference chatRoomRef;
    private DocumentReference userRef;
    private String currentUserId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        mToolbar = findViewById(R.id.inbox_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mesaje");
        db = FirebaseFirestore.getInstance();
        chatRoomRef = db.collection("chatRooms");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        Query chatRoomQuery = chatRoomRef.whereEqualTo("id1", currentUserId).orderBy("lastSent");

        FirestoreRecyclerOptions<InboxModel> options = new FirestoreRecyclerOptions.Builder<InboxModel>()
                .setQuery(chatRoomQuery, InboxModel.class)
                .build();

        inboxAdapter = new FirestoreRecyclerAdapter<InboxModel, InboxViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull InboxViewHolder holder, int position, @NonNull InboxModel model) {
                userRef = db.collection("users").document(model.getId2());
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot != null){
                                holder.setProfileImage(documentSnapshot.getString("profileImage"));
                                holder.setFullName(documentSnapshot.getString("fullName"));
                                holder.setStatus(documentSnapshot.getString("status"));
                            }
                        }
                    }
                });
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedProfileKey = model.getId2();
                        Intent messageIntent = new Intent(InboxActivity.this, MessageActivity.class);
                        messageIntent.putExtra("selectedProfileKey", selectedProfileKey);
                        startActivity(messageIntent);
                    }
                });
            }

            @NonNull
            @Override
            public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_chartroom_layout, parent, false);
                return new InboxViewHolder(view);
            }
        };

        inboxView = (RecyclerView) findViewById(R.id.rv_inbox);
        inboxView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(InboxActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        inboxView.setLayoutManager(linearLayoutManager);
        inboxView.setAdapter(inboxAdapter);
    }

    private class InboxViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String fullName){
            TextView chatFullName = (TextView) mView.findViewById(R.id.tv_inbox_person_full_name);
            chatFullName.setText(fullName);
        }

        public void setProfileImage(String profileImage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.civ_inbox_person_profile);
            Picasso.get().load(profileImage).into(image);
        }

        public void setStatus(String status){
            TextView chatStatus = (TextView) mView.findViewById(R.id.tv_inbox_person_status);
            chatStatus.setText(status);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        inboxAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        inboxAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}