package com.example.educonnect;

public class ChatMessage {
    // Keep both so older messages (saved with `sender` only) and new messages (with senderId) work.
    private String senderId;   // Firebase UID
    private String sender;     // email or displayName
    private String message;
    private long timestamp;

    public ChatMessage() { } // Required for Firestore

    public ChatMessage(String senderId, String sender, String message, long timestamp) {
        this.senderId = senderId;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
