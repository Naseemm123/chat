package com.collegechat.controller;

import com.collegechat.model.CreatePrivateRoomRequest;
import com.collegechat.model.Room;
import com.collegechat.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for room listing and room creation.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<Room>> getRooms() {
        // Return all rooms so frontend can display public + private entries.
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @PostMapping("/private")
    public ResponseEntity<Room> createPrivateRoom(@RequestBody CreatePrivateRoomRequest request) {
        // Minimal validation to keep the example simple.
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Delegate actual creation to service layer.
        Room room = roomService.createPrivateRoom(
                request.getName().trim(),
                request.getPassword().trim()
        );
        return ResponseEntity.ok(room);
    }
}
