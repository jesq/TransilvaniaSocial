package com.example.transilvaniasocial.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transilvaniasocial.model.MessagesModel;
import com.example.transilvaniasocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<MessagesModel> userMessagesList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference usersRef;
    public MessagesAdapter (List<MessagesModel> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.tv_sender_message);
            receiverMessageText = itemView.findViewById(R.id.tv_receiver_message);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        String messageSenderID = mAuth.getCurrentUser().getUid();
        MessagesModel messages = userMessagesList.get(position);
        String senderUserID = messages.getSender();

        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users").document(senderUserID);
        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        if(senderUserID.equals(messageSenderID)){
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_background);
            holder.senderMessageText.setTextColor(Color.WHITE);
            holder.senderMessageText.setGravity(Gravity.LEFT);
            holder.senderMessageText.setText(messages.getMessage());
        }
        else{
            holder.senderMessageText.setVisibility(View.INVISIBLE);
            holder.receiverMessageText.setVisibility(View.VISIBLE);

            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_background);
            holder.receiverMessageText.setTextColor(Color.WHITE);
            holder.receiverMessageText.setGravity(Gravity.LEFT);
            holder.receiverMessageText.setText(messages.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
