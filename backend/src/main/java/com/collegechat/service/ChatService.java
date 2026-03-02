package com.collegechat.service;

import com.collegechat.model.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory WebSocket session manager + message broadcaster.
 */
@Service
public class ChatService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // roomId -> all active WebSocket sessions in that room
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    // sessionId -> username
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();
    // sessionId -> current roomId
    private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

    /**
     * Registers username for this WebSocket connection and puts user in room.
     */
    public void connectUser(WebSocketSession session, String username, String roomId) {
        String sessionId = session.getId();
        sessionToUser.put(sessionId, username);
        joinRoom(session, roomId);
    }

    /**
     * Moves a session from old room (if any) to new room.
     */
    public void joinRoom(WebSocketSession session, String roomId) {
        String sessionId = session.getId();
        leaveCurrentRoom(session);

        roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(roomId).add(session);
        sessionToRoom.put(sessionId, roomId);
    }

    /**
     * Converts incoming message to a server-authoritative CHAT event and broadcasts it.
     */
    public void handleChatMessage(WebSocketSession session, ChatMessage message) {
        String roomId = sessionToRoom.get(session.getId());
        if (roomId == null) {
            sendError(session, "Join a room first.");
            return;
        }

        message.setRoomId(roomId);
        message.setType("CHAT");
        message.setSender(sessionToUser.get(session.getId()));
        message.setTimestamp(Instant.now().toString());

        broadcastToRoom(roomId, message);
    }

    /**
     * Cleans up maps when user disconnects.
     */
    public void disconnect(WebSocketSession session) {
        leaveCurrentRoom(session);
        sessionToUser.remove(session.getId());
    }

    /**
     * Returns username for current WebSocket session.
     */
    public String getUsername(WebSocketSession session) {
        return sessionToUser.get(session.getId());
    }

    /**
     * Removes a session from whichever room it currently belongs to.
     */
    private void leaveCurrentRoom(WebSocketSession session) {
        String sessionId = session.getId();
        String currentRoom = sessionToRoom.remove(sessionId);
        if (currentRoom == null) {
            return;
        }

        Set<WebSocketSession> sessions = roomSessions.get(currentRoom);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(currentRoom);
            }
        }
    }

    /**
     * Sends a server informational message to one client.
     */
    public void sendSystemMessage(WebSocketSession session, String content) {
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setType("SYSTEM");
        systemMessage.setSender("System");
        systemMessage.setContent(content);
        systemMessage.setTimestamp(Instant.now().toString());

        sendToSession(session, systemMessage);
    }

    /**
     * Sends a server error message to one client.
     */
    public void sendError(WebSocketSession session, String content) {
        ChatMessage errorMessage = new ChatMessage();
        errorMessage.setType("ERROR");
        errorMessage.setSender("System");
        errorMessage.setContent(content);
        errorMessage.setTimestamp(Instant.now().toString());

        sendToSession(session, errorMessage);
    }

    /**
     * Sends one message to every active session in the room.
     */
    private void broadcastToRoom(String roomId, ChatMessage message) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) {
            return;
        }

        for (WebSocketSession roomSession : sessions) {
            sendToSession(roomSession, message);
        }
    }

    /**
     * Serializes ChatMessage to JSON and sends it to a single session.
     */
    private void sendToSession(WebSocketSession session, ChatMessage message) {
        if (!session.isOpen()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(payload));
        } catch (JsonProcessingException e) {
            // Ignore malformed serialization errors for this small demo.
        } catch (IOException e) {
            // Ignore transient send errors in this small demo.
        }
    }
}
