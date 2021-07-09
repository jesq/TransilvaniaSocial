package com.example.transilvaniasocial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference receiverUserRef, senderUserRef;
    private CollectionReference chatRoomRef;
    private CollectionReference messageSenderRef, messageReceiverRef;

    private Toolbar mToolbar;
    private ImageButton sendTextBtn;
    private EditText messageInput;
    private RecyclerView messagesView;
    private final List<MessagesModel> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private String receiverUserID, senderUserID;
    private TextView userName;
    private CircleImageView receiverProfileImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        mToolbar = findViewById(R.id.message_bar_layout);
        sendTextBtn = findViewById(R.id.ib_send_message);
        messageInput = findViewById(R.id.et_input_message);
        setSupportActionBar(mToolbar);
        receiverUserID = getIntent().getExtras().get("selectedProfileKey").toString();
        db = FirebaseFirestore.getInstance();
        chatRoomRef = db.collection("chatRooms");
        receiverUserRef = db.collection("users").document(receiverUserID);
        senderUserRef = db.collection("users").document(senderUserID);
        messageSenderRef = db.collection("messages").document(senderUserID).collection(receiverUserID);
        messageReceiverRef = db.collection("messages").document(receiverUserID).collection(senderUserID);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.messages_toolbar, null);
        actionBar.setCustomView(action_bar_view);
        userName = findViewById(R.id.tv_tb_messages_name);
        receiverProfileImg = findViewById(R.id.civ_tb_messages);
        messagesAdapter = new MessagesAdapter(messagesList);
        messagesView = findViewById(R.id.rv_messages);
        linearLayoutManager = new LinearLayoutManager(this);
        messagesView.setHasFixedSize(true);
        messagesView.setLayoutManager(linearLayoutManager);
        messagesView.setAdapter(messagesAdapter);

        receiverUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null){
                        userName.setText(documentSnapshot.getString("fullName"));
                        Picasso.get().load(documentSnapshot.getString("profileImage")).into(receiverProfileImg);
                    }
                }
            }
        });
        sendTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        FetchMessages();
    }

    private void FetchMessages() {
        messageSenderRef.orderBy("sent").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null){
                    for (DocumentChange dc : value.getDocumentChanges()){
                        switch (dc.getType()) {
                            case ADDED:
                                MessagesModel messages = dc.getDocument().toObject(MessagesModel.class);
                                messagesList.add(messages);
                                messagesAdapter.notifyDataSetChanged();
                                messagesView.scrollToPosition(messagesAdapter.getItemCount() - 1);
                                break;
                        }
                    }
                }
            }
        });
    }

    private void SendMessage() {
        String message = messageInput.getText().toString().trim();
        if(!TextUtils.isEmpty(message)){
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String saveCurrentDate = currentDate.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calendarTime.getTime());

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("time", saveCurrentTime);
            messageMap.put("date", saveCurrentDate);
            messageMap.put("sent", FieldValue.serverTimestamp());
            messageMap.put("sender", senderUserID);

            receiverUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot != null){
                            Map<String, Object> chatRoomMap = new HashMap<>();
                            chatRoomMap.put("id1", senderUserID);
                            chatRoomMap.put("id2", receiverUserID);
                            chatRoomMap.put("lastSent", FieldValue.serverTimestamp());
                            chatRoomRef.document(senderUserID + receiverUserID).set(chatRoomMap);
                        }
                    }
                }
            });
            senderUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot != null){
                            Map<String, Object> chatRoomMap = new HashMap<>();
                            chatRoomMap.put("id1", receiverUserID);
                            chatRoomMap.put("id2", senderUserID);
                            chatRoomMap.put("lastSent", FieldValue.serverTimestamp());
                            chatRoomRef.document(receiverUserID + senderUserID).set(chatRoomMap);
                        }
                    }
                }
            });

            final String messageKey = messageSenderRef.document().getId();
            messageSenderRef.document(messageKey).set(messageMap).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, "Mesajul nu a fost trimis!", Toast.LENGTH_SHORT).show();
                }
            });

            messageReceiverRef.document(messageKey).set(messageMap).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, "Mesajul nu poate sa fie primit!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        messageInput.setText("");
    }
}