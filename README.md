# College Chat App (Java + React)

A simple chat web application for a college project.

## Tech stack
- Backend: Java 17 + Spring Boot
- Frontend: React + Vite
- Communication: WebSocket (live messages) + REST (room list/create)
- Data storage: In-memory (no database)

## Features
- Join with a username
- Chat in public rooms (`General`, `College`)
- Create private rooms with passwords
- Switch rooms
- Real-time chat updates

## Simple backend architecture
- `RoomService`: stores rooms and private-room passwords in memory.
- `ChatService`: tracks connected users/sessions and broadcasts messages.
- `ChatWebSocketHandler`: receives WebSocket messages (`CONNECT`, `JOIN`, `CHAT`) and calls services.
- `RoomController`: REST endpoints for room list and private room creation.

This is intentionally simple and easy to explain in a viva/demo.

## 60-second viva explanation
You can explain the backend in this order:
1. `RoomController` handles HTTP requests for room list and private room creation.
2. `RoomService` keeps room data in memory and checks who can access private rooms.
3. `ChatWebSocketHandler` handles real-time WebSocket events (`CONNECT`, `JOIN`, `CHAT`).
4. `ChatService` tracks connected sessions and broadcasts chat messages to users in the same room.
5. Everything is in-memory, so data resets when server restarts (no database complexity).

## Data flow (easy to remember)

### A) User opens app and enters username
1. Frontend opens WebSocket connection to `/ws/chat`.
2. Frontend sends `CONNECT` message with username and initial room.
3. Backend validates room access and stores `sessionId -> username/room`.
4. Backend sends a `SYSTEM` message confirming connection.

### B) User sends a chat message
1. Frontend sends WebSocket message with `type = CHAT`.
2. Backend adds sender, roomId, and timestamp.
3. `ChatService` broadcasts the message to all sessions in that room.
4. Frontend receives and renders the new message.

### C) User creates private room
1. Frontend calls `POST /api/rooms/private`.
2. `RoomController` validates input and calls `RoomService`.
3. `RoomService` creates a room id and stores the room password.
4. Frontend refreshes rooms and joins the new private room.

## Run with Docker (single command)

From the project root:
```bash
docker compose up --build
```

Open: `http://localhost:3000`

This starts both backend and frontend together.

To stop:
```bash
docker compose down
```

## Run without Docker

### 1) Start backend
```bash
cd backend
mvn spring-boot:run
```

### 2) Start frontend
In a second terminal:
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173`

## WebSocket message format
Client to server:
```json
{ "type": "CONNECT", "sender": "Aisha", "roomId": "general" }
{ "type": "JOIN", "roomId": "private-xxxx", "roomPassword": "1234" }
{ "type": "CHAT", "content": "Hello everyone" }
```

Server to client:
```json
{ "type": "CHAT", "sender": "Aisha", "roomId": "general", "content": "Hello everyone", "timestamp": "..." }
{ "type": "SYSTEM", "sender": "System", "content": "Connected as Aisha in room general.", "timestamp": "..." }
```

## Notes
- No user authentication/accounts (kept simple for project clarity)
- No database (data resets when server restarts)
