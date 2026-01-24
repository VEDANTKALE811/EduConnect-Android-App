package com.example.educonnect;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatMessage> messageList;
    private final String currentUserId;
    private final String currentUserEmail;

    public ChatAdapter(Context context, List<ChatMessage> messageList, String currentUserId, String currentUserEmail) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        holder.bind(msg);

        // ✅ Safe null check before equals
        if (msg.getSenderId() != null && currentUserId != null && msg.getSenderId().equals(currentUserId)) {
            holder.layoutMain.setBackgroundResource(R.drawable.bg_message_outgoing);
            holder.layoutMain.setPadding(100, 10, 20, 10);
            holder.tvSender.setText("You");
        } else {
            holder.layoutMain.setBackgroundResource(R.drawable.bg_message_incoming);
            holder.layoutMain.setPadding(20, 10, 100, 10);
            holder.tvSender.setText(msg.getSender() != null ? msg.getSender() : "Unknown");
        }

        // 🗑️ Long press delete option for sender only
        holder.itemView.setOnLongClickListener(v -> {
            if (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setMessage("Do you want to delete this message?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteMessage(msg))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            return true;
        });
    }

    private void deleteMessage(ChatMessage msg) {
        FirebaseFirestore.getInstance()
                .collection("chats")
                .whereEqualTo("timestamp", msg.getTimestamp())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        for (var doc : query.getDocuments()) {
                            doc.getReference().delete()
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvSender, tvTime;
        LinearLayout layoutMain;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMain = itemView.findViewById(R.id.layoutMain);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(ChatMessage msg) {
            tvMessage.setText(msg.getMessage());
            tvSender.setText(msg.getSender());
            tvTime.setText(formatTime(msg.getTimestamp()));
        }

        private String formatTime(long timestamp) {
            return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(new Date(timestamp));
        }
    }
}
