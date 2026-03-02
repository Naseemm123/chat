package com.collegechat.service;

import com.collegechat.model.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory room management.
 *
 * No database is used in this project. Data is reset when server restarts.
 */
@Service
public class RoomService {
    // roomId -> Room object
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    // roomId -> password (only stored for private rooms)
    private final Map<String, String> privateRoomPasswords = new ConcurrentHashMap<>();

    public RoomService() {
        // Create a couple of default public rooms.
        Room general = new Room("general", "General", false);
        Room college = new Room("college", "College", false);
        rooms.put(general.getId(), general);
        rooms.put(college.getId(), college);
    }

    /**
     * Returns all rooms (public + private).
     */
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * Creates a private room protected by password.
     */
    public Room createPrivateRoom(String name, String password) {
        String id = "private-" + UUID.randomUUID().toString().substring(0, 8);
        Room room = new Room(id, name, true);
        rooms.put(id, room);
        privateRoomPasswords.put(id, password);
        return room;
    }

    /**
     * Finds a room by id.
     */
    public Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }

    /**
     * Checks if room exists and whether it requires a password.
     */
    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    /**
     * Checks whether provided password is valid for a private room.
     * Public rooms always return true.
     */
    public boolean canJoinWithPassword(String roomId, String providedPassword) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return false;
        }
        if (!room.isPrivateRoom()) {
            return true;
        }

        String expectedPassword = privateRoomPasswords.get(roomId);
        return expectedPassword != null && expectedPassword.equals(providedPassword);
    }
}
