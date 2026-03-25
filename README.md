# College Chat App (Java + React)

A simple room-based chat web app for a college project.

## What this project includes
- Java Spring Boot backend
- React frontend
- Real-time chat with WebSocket
- Public rooms + password-protected private rooms
- In-memory storage (no database)

## Quick setup (recommended)

### Option 1: Run with Docker (single command)

Prerequisites:
- Docker
- Docker Compose

From project root:
```bash
docker compose up --build
```

Open in browser:
- `http://localhost:3000`

Stop:
```bash
docker compose down
```

## Local setup (without Docker)

Prerequisites:
- Java 17+
- Maven 3.9+
- Node.js 18+ (or newer)
- npm

### 1) Start backend
```bash
cd backend
mvn spring-boot:run
```
Backend runs on `http://localhost:8080`

### 2) Start frontend
In a second terminal:
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173`

## How to use
1. Open the app and enter a username.
2. Join a public room directly.
3. For private rooms, click the room and enter its password.
4. Create private rooms from sidebar using room name + password.

## API and WebSocket basics

### REST endpoints
- `GET /api/rooms` -> list all rooms
- `POST /api/rooms/private` -> create private room

Example private room request:
```json
{
  "name": "Project Team",
  "password": "team123"
}
```

### WebSocket endpoint
- `/ws/chat`

Client message examples:
```json
{ "type": "CONNECT", "sender": "Aisha", "roomId": "general" }
{ "type": "JOIN", "roomId": "private-ab12cd34", "roomPassword": "team123" }
{ "type": "CHAT", "content": "Hello everyone" }
```

## Simple backend architecture
- `RoomController`: REST API for rooms.
- `RoomService`: in-memory rooms + private-room passwords.
- `ChatWebSocketHandler`: handles `CONNECT`, `JOIN`, `CHAT` messages.
- `ChatService`: tracks active sessions and broadcasts messages per room.

## Notes
- No user accounts/authentication system (kept intentionally simple).
- No database (all data resets when server restarts).

## Troubleshooting

If Docker build fails with snapshot/cache errors:
```bash
docker compose down
docker builder prune -af
docker compose build --no-cache
docker compose up -d
```

If UI changes don’t appear:
- Rebuild containers with `--build`
- Hard refresh browser (`Ctrl+Shift+R`)
