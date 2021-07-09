package com.example.transilvaniasocial.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transilvaniasocial.R;
import com.example.transilvaniasocial.messages.MessageActivity;
import com.example.transilvaniasocial.model.SearchStudentsModel;
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

public class SearchStudentsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchEditText;
    private RecyclerView searchResultView;
    private FirestoreRecyclerAdapter searchAdapter;
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private FirebaseAuth mAuth;
    private DocumentReference facultyRef;
    private Query SearchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        mToolbar = findViewById(R.id.find_friends_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Caută colegi");

        searchButton = findViewById(R.id.ib_search_friends);
        searchEditText = findViewById(R.id.et_search_friends);
        searchResultView = findViewById(R.id.rv_search_friends_list);
        searchResultView.setHasFixedSize(true);
        searchResultView.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = searchEditText.getText().toString();
                ShowSearchResult(searchText);
            }
        });
    }

    private void ShowSearchResult(String searchText) {
        facultyRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
        facultyRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null){
                        String currentUserName = documentSnapshot.getString("fullName");
                        String currentUserFaculty = documentSnapshot.getString("faculty");
                        SearchQuery = usersRef.orderBy("fullName").whereEqualTo("faculty", currentUserFaculty).startAt(searchText)
                                .endAt(searchText + "\uf8ff").whereNotEqualTo("fullName", currentUserName);
                        FirestoreRecyclerOptions<SearchStudentsModel> options = new FirestoreRecyclerOptions.Builder<SearchStudentsModel>()
                                .setQuery(SearchQuery, SearchStudentsModel.class)
                                .build();

                        searchAdapter = new FirestoreRecyclerAdapter<SearchStudentsModel, SearchViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull SearchViewHolder holder, int position, @NonNull SearchStudentsModel model) {
                                holder.setFullName(model.getFullName());
                                holder.setStatus(model.getStatus());
                                holder.setProfileImage(model.getProfileImage());
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String selectedProfileKey = getSnapshots().getSnapshot(position).getId();
                                        CharSequence options[] = new CharSequence[]{
                                                "Vezi profilul " + model.getFullName(),
                                                "Trimite mesaj"
                                        };
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SearchStudentsActivity.this);
                                        builder.setTitle("Selecteaza optiune");
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if(which == 0){
                                                    Intent userProfileIntent = new Intent(SearchStudentsActivity.this, UserProfileActivity.class);
                                                    userProfileIntent.putExtra("selectedProfileKey", selectedProfileKey);
                                                    startActivity(userProfileIntent);
                                                }
                                                if(which == 1){
                                                    Intent messageIntent = new Intent(SearchStudentsActivity.this, MessageActivity.class);
                                                    messageIntent.putExtra("selectedProfileKey", selectedProfileKey);
                                                    startActivity(messageIntent);
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });
                            }
                            @NonNull
                            @Override
                            public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_friends_result_layout, parent, false);
                                return new SearchViewHolder(view);
                            }
                        };
                        searchResultView.setAdapter(searchAdapter);
                        searchAdapter.startListening();
                    }
                }
            }
        });
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String fullName){
            TextView searchFullName = (TextView) mView.findViewById(R.id.tv_search_full_name);
            searchFullName.setText(fullName);
        }

        public void setProfileImage(String profileImage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.civ_search_result);
            Picasso.get().load(profileImage).into(image);
        }

        public void setStatus(String status){
            TextView searchStatus = (TextView) mView.findViewById(R.id.tv_search_result_status);
            searchStatus.setText(status);
        }
    }
}