import { useEffect, useMemo, useRef, useState } from 'react'

const API_BASE = ''

function getWebSocketUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${protocol}://${window.location.host}/ws/chat`
}

function App() {
  const [username, setUsername] = useState('')
  const [inputUsername, setInputUsername] = useState('')
  const [rooms, setRooms] = useState([])
  const [selectedRoom, setSelectedRoom] = useState('general')
  const [messages, setMessages] = useState([])
  const [messageInput, setMessageInput] = useState('')

  const [privateRoomName, setPrivateRoomName] = useState('')
  const [privateRoomPassword, setPrivateRoomPassword] = useState('')
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [clientId] = useState(() => {
    const randomPart = Math.random().toString(36).slice(2, 10)
    return `client-${Date.now()}-${randomPart}`
  })

  const socketRef = useRef(null)
  const messagesEndRef = useRef(null)

  const isConnected = useMemo(
    () => !!socketRef.current && socketRef.current.readyState === WebSocket.OPEN,
    [messages]
  )

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const fetchRooms = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/rooms`)
      if (!res.ok) return
      const data = await res.json()
      setRooms(data)

      if (!data.find((room) => room.id === selectedRoom) && data.length > 0) {
        setSelectedRoom(data[0].id)
      }
    } catch {
      // Ignore request failures in this simple project.
    }
  }

  const connect = () => {
    const cleanName = inputUsername.trim()
    if (!cleanName) return

    setUsername(cleanName)
    fetchRooms()

    const socket = new WebSocket(getWebSocketUrl())
    socketRef.current = socket

    socket.onopen = () => {
      socket.send(
        JSON.stringify({
          type: 'CONNECT',
          sender: cleanName,
          roomId: selectedRoom,
          senderClientId: clientId
        })
      )
    }

    socket.onmessage = (event) => {
      const payload = JSON.parse(event.data)
      setMessages((prev) => [...prev, payload])
    }

    socket.onclose = () => {
      socketRef.current = null
    }
  }

  const sendChat = () => {
    const cleanMessage = messageInput.trim()
    if (!cleanMessage || !socketRef.current) return

    socketRef.current.send(
      JSON.stringify({
        type: 'CHAT',
        content: cleanMessage
      })
    )
    setMessageInput('')
  }

  const joinRoom = (roomId, roomPassword = '') => {
    setSelectedRoom(roomId)
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
      socketRef.current.send(
        JSON.stringify({
          type: 'JOIN',
          roomId,
          roomPassword
        })
      )
      setMessages([])
    }
  }

  const createPrivateRoom = async () => {
    const cleanName = privateRoomName.trim()
    const cleanPassword = privateRoomPassword.trim()
    if (!cleanName || !cleanPassword || !username) return

    try {
      const res = await fetch(`${API_BASE}/api/rooms/private`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: cleanName,
          password: cleanPassword
        })
      })

      if (!res.ok) return
      const room = await res.json()
      await fetchRooms()
      joinRoom(room.id, cleanPassword)
      setPrivateRoomName('')
      setPrivateRoomPassword('')
    } catch {
      // Ignore request failures in this simple project.
    }
  }

  const handleRoomClick = (room) => {
    if (!room.privateRoom) {
      joinRoom(room.id)
      return
    }

    const password = window.prompt(`Enter password for "${room.name}"`)
    if (password == null) {
      return
    }
    joinRoom(room.id, password)
  }

  if (!username) {
    return (
      <div className="login-page">
        <div className="login-card">
          <h1>Chat App</h1>
          <p className="muted">Enter your username to continue</p>
          <input
            value={inputUsername}
            onChange={(e) => setInputUsername(e.target.value)}
            placeholder="e.g. Aisha"
            onKeyDown={(e) => e.key === 'Enter' && connect()}
          />
          <button onClick={connect}>Continue</button>
        </div>
      </div>
    )
  }

  return (
    <div className={sidebarOpen ? 'layout' : 'layout sidebar-collapsed'}>
      <aside className={sidebarOpen ? 'sidebar' : 'sidebar collapsed'}>
        <div className="sidebar-top">
          {sidebarOpen ? <h1>Chat App</h1> : null}
          <button
            className="icon-toggle"
            onClick={() => setSidebarOpen((prev) => !prev)}
            aria-label={sidebarOpen ? 'Collapse sidebar' : 'Expand sidebar'}
            title={sidebarOpen ? 'Collapse' : 'Expand'}
          >
            {sidebarOpen ? '◀' : '▶'}
          </button>
        </div>

        {sidebarOpen ? (
          <>
            <p className="muted">Simple room-based chat</p>

            <div className="card">
              <strong>{username}</strong>
              <span className="status">{isConnected ? 'Connected' : 'Disconnected'}</span>
              <button onClick={fetchRooms}>Refresh Rooms</button>
            </div>

            <div className="card">
              <h2>Rooms</h2>
              {rooms.map((room) => (
                <button
                  key={room.id}
                  className={room.id === selectedRoom ? 'room active' : 'room'}
                  onClick={() => handleRoomClick(room)}
                >
                  {room.privateRoom ? '🔒 ' : ''}
                  {room.name}
                </button>
              ))}
            </div>

            <div className="card">
              <h2>New Private Room</h2>
              <input
                value={privateRoomName}
                onChange={(e) => setPrivateRoomName(e.target.value)}
                placeholder="Room name"
              />
              <input
                type="password"
                value={privateRoomPassword}
                onChange={(e) => setPrivateRoomPassword(e.target.value)}
                placeholder="Room password"
              />
              <button onClick={createPrivateRoom}>Create</button>
            </div>
          </>
        ) : null}
      </aside>

      <main className="chat">
        <div className="chat-header">
          <h2>Room: {selectedRoom}</h2>
        </div>

        <div className="messages">
          {messages.map((message, idx) => {
            const isSystem = message.type === 'SYSTEM' || message.type === 'ERROR'
            const sameClient = message.senderClientId && message.senderClientId === clientId
            const sameUsername =
              (message.sender || '').trim().toLowerCase() === username.trim().toLowerCase()
            const isMine = !isSystem && (sameClient || sameUsername)
            const rowClass = isSystem ? 'message-row system' : isMine ? 'message-row mine' : 'message-row other'
            const bubbleClass = isSystem ? 'message system' : isMine ? 'message mine' : 'message other'

            return (
              <div key={idx} className={rowClass}>
                <div className={bubbleClass}>
                  <div className="meta">
                    <strong>{isMine ? 'You' : message.sender}</strong>
                    <span>{message.timestamp ? new Date(message.timestamp).toLocaleTimeString() : ''}</span>
                  </div>
                  <p>{message.content}</p>
                </div>
              </div>
            )
          })}
          <div ref={messagesEndRef} />
        </div>

        <div className="composer">
          <input
            value={messageInput}
            onChange={(e) => setMessageInput(e.target.value)}
            placeholder="Type your message"
            onKeyDown={(e) => e.key === 'Enter' && sendChat()}
          />
          <button onClick={sendChat}>Send</button>
        </div>
      </main>
    </div>
  )
}

export default App
