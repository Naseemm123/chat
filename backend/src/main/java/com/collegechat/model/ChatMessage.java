package com.collegechat.model;

/**
 * Message DTO used for WebSocket communication.
 *
 * Frontend sends and receives this JSON structure.
 */
public class ChatMessage {
    // CONNECT | JOIN | CHAT | SYSTEM | ERROR
    private String type;
    // Username of sender, or "System" for server-generated messages.
    private String sender;
    // Chat room id such as "general" or "private-xxxx".
    private String roomId;
    // Password used when trying to join a private room.
    private String roomPassword;
    // Unique per-browser client id used to identify message ownership.
    private String senderClientId;
    // Text content of the message.
    private String content;
    // ISO-8601 timestamp string, for example 2026-03-02T12:34:56Z.
    private String timestamp;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRoomPassword() {
        return roomPassword;
    }

    public void setRoomPassword(String roomPassword) {
        this.roomPassword = roomPassword;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderClientId() {
        return senderClientId;
    }

    public void setSenderClientId(String senderClientId) {
        this.senderClientId = senderClientId;
    }
}
