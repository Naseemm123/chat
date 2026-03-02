package com.collegechat.websocket;

import com.collegechat.model.ChatMessage;
import com.collegechat.service.ChatService;
import com.collegechat.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Handles all incoming WebSocket messages from clients.
 *
 * Supported message types:
 * - CONNECT: first message from client with username + initial room
 * - JOIN: switch to a different room
 * - CHAT: normal chat text message
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ChatService chatService;
    private final RoomService roomService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatWebSocketHandler(ChatService chatService, RoomService roomService) {
        this.chatService = chatService;
        this.roomService = roomService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        try {
            // Parse JSON payload into ChatMessage object.
            ChatMessage message = objectMapper.readValue(textMessage.getPayload(), ChatMessage.class);
            String type = message.getType() == null ? "" : message.getType().toUpperCase();

            // Route message by type.
            switch (type) {
                case "CONNECT" -> handleConnect(session, message);
                case "JOIN" -> handleJoin(session, message);
                case "CHAT" -> chatService.handleChatMessage(session, message);
                default -> chatService.sendError(session, "Unknown message type.");
            }
        } catch (Exception e) {
            chatService.sendError(session, "Invalid message format.");
        }
    }

    /**
     * Initial handshake: validates username, checks room access, registers session.
     */
    private void handleConnect(WebSocketSession session, ChatMessage message) {
        if (message.getSender() == null || message.getSender().isBlank()) {
            chatService.sendError(session, "Username is required.");
            return;
        }

        String roomId = (message.getRoomId() == null || message.getRoomId().isBlank())
                ? "general"
                : message.getRoomId();

        if (!roomService.roomExists(roomId)) {
            chatService.sendError(session, "Room does not exist.");
            return;
        }

        if (!roomService.canJoinWithPassword(roomId, message.getRoomPassword())) {
            chatService.sendError(session, "Wrong password for this private room.");
            return;
        }

        String username = message.getSender().trim();
        chatService.connectUser(session, username, roomId);
        chatService.sendSystemMessage(session, "Connected as " + username + " in room " + roomId + ".");
    }

    /**
     * Handles room change request from an already connected client.
     */
    private void handleJoin(WebSocketSession session, ChatMessage message) {
        String username = chatService.getUsername(session);
        if (username == null) {
            chatService.sendError(session, "Connect first.");
            return;
        }

        String roomId = message.getRoomId();
        if (roomId == null || roomId.isBlank()) {
            chatService.sendError(session, "Room ID is required.");
            return;
        }

        if (!roomService.roomExists(roomId)) {
            chatService.sendError(session, "Room does not exist.");
            return;
        }

        if (!roomService.canJoinWithPassword(roomId, message.getRoomPassword())) {
            chatService.sendError(session, "Wrong password for this private room.");
            return;
        }

        chatService.joinRoom(session, roomId);
        chatService.sendSystemMessage(session, "Switched to room " + roomId + ".");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Remove session from in-memory maps.
        chatService.disconnect(session);
    }
}
