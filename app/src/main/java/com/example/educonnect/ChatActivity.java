package com.example.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private com.google.android.material.button.MaterialButton btnSend;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private String currentUserId;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.topAppBar);
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // not logged in -> force login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        currentUserEmail = currentUser.getEmail();

        db = FirebaseFirestore.getInstance();

        // Toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle("EduConnect Chat");

        // Recycler setup - stack from end so bottom is newest
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(this, messageList, currentUserId, currentUserEmail);
        recyclerView.setAdapter(chatAdapter);

        loadMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Type a message first", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", currentUserId);
        msg.put("sender", currentUserEmail != null ? currentUserEmail : "Unknown");
        msg.put("message", text);
        msg.put("timestamp", now);

        // optimistic UI - add to local list immediately
        ChatMessage optimistic = new ChatMessage(currentUserId, currentUserEmail, text, now);
        messageList.add(optimistic);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
        etMessage.setText("");
        hideKeyboard();

        // send to Firestore
        db.collection("chats")
                .add(msg)
                .addOnSuccessListener(docRef -> {
                    // success: Firestore snapshot listener will also add; duplicates avoided by clearing/rebuilding in listener
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMessages() {
        db.collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) -> {
                    if (e != null) {
                        // log or show error
                        return;
                    }
                    if (snapshots == null) return;

                    // Clear and rebuild list to avoid duplicates / ordering issues
                    messageList.clear();
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        // To preserve order, we will read all docs from snapshot.documents instead of using only DocumentChange
                        // but DocumentChange is fine; to be safe, use snapshot.getDocuments()
                    }
                    // use full snapshot to preserve order
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) messageList.add(msg);
                    }

                    chatAdapter.notifyDataSetChanged();
                    // scroll to bottom
                    if (!messageList.isEmpty()) {
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
