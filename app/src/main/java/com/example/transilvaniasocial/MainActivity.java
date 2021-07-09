package com.example.transilvaniasocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.transilvaniasocial.authentication.LoginActivity;
import com.example.transilvaniasocial.menu.ProfileActivity;
import com.example.transilvaniasocial.menu.SearchStudentsActivity;
import com.example.transilvaniasocial.menu.SettingsActivity;
import com.example.transilvaniasocial.messages.InboxActivity;
import com.example.transilvaniasocial.model.PostsModel;
import com.example.transilvaniasocial.posts.ClickOnPostActivity;
import com.example.transilvaniasocial.posts.CommentsActivity;
import com.example.transilvaniasocial.posts.PostActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView postList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navView;
    private Toolbar mainToolBar;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUsername, FacultyDisplay;
    private ImageButton NewPostButton;
    private String currentUserFaculty;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private DocumentReference facultyRef;
    private CollectionReference colRef;
    private DocumentReference userRef;
    FirestoreRecyclerAdapter adapter;
    String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        docRef = db.collection("users").document(currentUserID);
        facultyRef = db.collection("users").document(currentUserID);
        colRef = db.collection("posts");

        facultyRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null){
                        currentUserFaculty = documentSnapshot.getString("faculty");
                        FacultyDisplay.setText(currentUserFaculty);
                        Query SortPosts = colRef.orderBy("created").whereEqualTo("faculty", currentUserFaculty);

                        FirestoreRecyclerOptions<PostsModel> options = new FirestoreRecyclerOptions.Builder<PostsModel>()
                                .setQuery(SortPosts, PostsModel.class)
                                .build();

                        adapter = new FirestoreRecyclerAdapter<PostsModel, PostsViewHolder>(options) {
                            @NonNull
                            @Override
                            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
                                return new PostsViewHolder(view);
                            }

                            @Override
                            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull PostsModel model) {
                                final String PostKey = getSnapshots().getSnapshot(position).getId();
                                holder.setTime(model.getTime());
                                holder.setDate(model.getDate());
                                holder.setDescription(model.getDescription());
                                userRef = db.collection("users").document(model.getUid());
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            if (documentSnapshot != null){
                                                holder.setFullName(documentSnapshot.getString("fullName"));
                                                holder.setProfileImage(documentSnapshot.getString("profileImage"));
                                            }
                                        }
                                    }
                                });
                                colRef.document(PostKey).collection("comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            holder.setCommentsCount(task.getResult().size() + "");
                                        }
                                    }
                                });
                                holder.setPostImage(model.getPostImage());
                                holder.mView.setLongClickable(true);
                                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        Intent clickPostIntent = new Intent(MainActivity.this, ClickOnPostActivity.class);
                                        clickPostIntent.putExtra("PostKey", PostKey);
                                        startActivity(clickPostIntent);
                                        return true;
                                    }
                                });
                                holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent commentIntent = new Intent(MainActivity.this, CommentsActivity.class);
                                        commentIntent.putExtra("PostKey", PostKey);
                                        startActivity(commentIntent);
                                    }
                                });
                            }
                        };
                        postList = (RecyclerView) findViewById(R.id.rv_posts_list);
                        postList.setHasFixedSize(true);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                        linearLayoutManager.setReverseLayout(true);
                        linearLayoutManager.setStackFromEnd(true);
                        postList.setLayoutManager(linearLayoutManager);
                        postList.setAdapter(adapter);
                        adapter.startListening();
                    }
                }
            }
        });

        mainToolBar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(mainToolBar);
        getSupportActionBar().setTitle("Postări");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawableLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navView = (NavigationView) findViewById(R.id.navigationView);

        FacultyDisplay = findViewById(R.id.tv_main_faculty);
        View profileView = navView.inflateHeaderView(R.layout.nav_header); //il folosesc pentru a putea modifica date din nav header
        NewPostButton = (ImageButton) findViewById(R.id.btn_new_post);
        NavProfileImage = (CircleImageView) profileView.findViewById(R.id.cv_nav_profile_img);
        NavProfileUsername = (TextView) profileView.findViewById(R.id.tv_nav_username);


        //iau din baza de date poza de profil si numele complet al utilizatorului si le aplic pe nav_header
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document != null) {
                        NavProfileUsername.setText(document.getString("fullName"));
                        Picasso.get().load(document.getString("profileImage")).placeholder(R.drawable.placeholderprofile).into(NavProfileImage);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Profilul nu exista", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                MenuSelector(item);
                return false;
            }
        });

        NewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPostActivity();
            }
        });

    }


    private void OpenPostActivity() {
        Intent newPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(newPostIntent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){  /* verifica la lansarea aplicatiei daca utilizatorul este logged in */
            OpenLoginActivity();
        }
    }
    
    private void OpenLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //nu lasa utilizatorul sa apese back button pentru a intra inapoi in MainActivity
        startActivity(loginIntent);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void MenuSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_new_post:
                OpenPostActivity();
                break;
            case R.id.nav_profile:
                OpenProfileActivity();
                Toast.makeText(this, "Profilul meu", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                OpenFindFriendsActivity();
                Toast.makeText(this, "Cauta Prieteni", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                OpenInboxActivity();
                Toast.makeText(this, "Mesaje", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                OpenSettingsActivity();
                Toast.makeText(this, "Setari", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                OpenLoginActivity();
                break;
        }
    }

    private void OpenInboxActivity() {
        Intent inboxIntent = new Intent(MainActivity.this, InboxActivity.class);
        startActivity(inboxIntent);
    }

    private void OpenFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, SearchStudentsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void OpenProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void OpenSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton CommentPostButton;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            CommentPostButton = mView.findViewById(R.id.ib_comment_post);
        }

        public void setFullName(String fullName){
           TextView postFullName = (TextView) mView.findViewById(R.id.post_fullname);
           postFullName.setText(fullName);
        }

        public void setProfileImage(String profileImage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_picture);
            Picasso.get().load(profileImage).into(image);
        }

        public void setPostImage(String postImage){
            ImageView image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postImage).into(image);
        }

        public void setTime(String time){
            TextView postTime = (TextView) mView.findViewById(R.id.post_time);
            postTime.setText("   " + time);
        }

        public void setDate(String date){
            TextView postDate = (TextView) mView.findViewById(R.id.post_date);
            postDate.setText("   " + date);
        }

        public void setDescription(String description){
            TextView postDescr = (TextView) mView.findViewById(R.id.post_description);
            postDescr.setText(description);
        }

        public void setCommentsCount(String commentsCount){
           TextView CommentsCount = mView.findViewById(R.id.tv_comments_count);
           CommentsCount.setText(commentsCount);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


}