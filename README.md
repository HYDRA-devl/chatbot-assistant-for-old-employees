# Employee Learning Chatbot

A Spring Boot + React chatbot application with gamification features for helping employees stay updated with technology advancements.

## Features

- 🤖 **AI Chatbot**: Powered by Ollama (llama2) running locally
- 💬 **Chat History**: Full conversation history tracking
- 🎮 **Gamification**: Points, levels, and achievements system
- 🔒 **Privacy-First**: All data stays local with Ollama
- 📚 **RAG Ready**: Architecture prepared for Retrieval-Augmented Generation

## Tech Stack

### Backend
- Spring Boot 3.2.0
- Java 17
- H2 Database (in-memory, easily switchable to PostgreSQL)
- WebFlux for Ollama API calls
- JPA/Hibernate

### AI
- Ollama (llama2:latest)
- RAG architecture (ready for implementation)

## Prerequisites

1. **Java 17** or higher
2. **Maven 3.6+**
3. **Docker** (for Ollama)
4. **Ollama container running**:
   ```bash
   docker start fullstackollama
   ```

## Quick Start

### 1. Start Ollama
```bash
# Your container is already set up
docker start fullstackollama

# Verify it's running
curl http://localhost:11434/api/tags
```

### 2. Run the Spring Boot Application
```bash
# From the project root directory
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Access H2 Console (Optional)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:chatbotdb`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Chat Endpoints
- `POST /api/chat/message` - Send a message to the chatbot
- `GET /api/chat/history/{userId}` - Get full chat history
- `GET /api/chat/history/{userId}/recent` - Get recent messages

### User Endpoints
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `POST /api/users/{id}/login` - Record user login

### Gamification Endpoints
- `GET /api/gamification/users/{userId}/achievements` - Get user achievements
- `GET /api/gamification/users/{userId}/stats` - Get user stats
- `GET /api/gamification/leaderboard` - Get top 10 users

## Testing the API

### Create a Test User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@company.com",
    "password": "password123",
    "fullName": "John Doe",
    "department": "Engineering"
  }'
```

### Send a Chat Message
```bash
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "message": "What is Spring Boot?"
  }'
```

## Gamification System

### Points System
- **10 points** per message sent
- Level up every **100 points**
- Achievement completion awards bonus points

### Default Achievements
- 🌟 First Steps (1 message)
- 💬 Conversationalist (50 messages)
- 🎓 Expert Learner (100 messages)
- ⭐ Point Master (500 points)
- 🚀 Rising Star (Level 5)
- 👨‍💻 Tech Guru (Level 10)

## RAG Implementation (TODO)

The architecture is ready for RAG. To implement:

1. Choose a vector database (Chroma, Weaviate, Qdrant)
2. Add embedding model (sentence-transformers)
3. Implement document chunking and indexing
4. Complete the `getContextForQuery()` method in `ChatService.java`

## Database Configuration

### Switch to PostgreSQL

Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chatbot
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Project Structure

```
src/main/java/com/company/chatbot/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── repository/      # Data repositories
└── service/         # Business logic
```

## Next Steps

1. ✅ Set up React frontend
2. ⬜ Implement RAG with vector database
3. ⬜ Add user authentication (Spring Security)
4. ⬜ Add WebSocket for real-time chat
5. ⬜ Deploy to production

## Troubleshooting

### Ollama not responding
```bash
# Check if container is running
docker ps | grep fullstackollama

# Restart if needed
docker restart fullstackollama
```

### Port 8080 already in use
Change the port in `application.properties`:
```properties
server.port=8081
```

## License

MIT
