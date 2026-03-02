package com.collegechat.model;

/**
 * Request body for creating a private room.
 */
public class CreatePrivateRoomRequest {
    // New room display name.
    private String name;
    // Password required to join this private room.
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
