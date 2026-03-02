package com.collegechat.model;

/**
 * Represents one chat room.
 */
public class Room {
    // Unique id used in API and WebSocket messages.
    private String id;
    // User-friendly display name.
    private String name;
    // false -> public room, true -> private room.
    private boolean privateRoom;

    public Room() {
    }

    public Room(String id, String name, boolean privateRoom) {
        this.id = id;
        this.name = name;
        this.privateRoom = privateRoom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }
}
