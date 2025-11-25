# Employee Learning Platform - Frontend Redesign Specifications

## Tech Stack
- **Frontend Framework**: React 18.3.1
- **Build Tool**: Vite 5.0.8
- **Routing**: React Router DOM 6.30.2
- **HTTP Client**: Axios 1.13.2
- **Styling**: Tailwind CSS 3.4.0
- **Backend API**: Spring Boot running on `http://localhost:8081`

## Project Structure
```
frontend/
├── src/
│   ├── components/
│   │   └── Layout.jsx          # Main layout with navigation
│   ├── pages/
│   │   ├── Login.jsx           # Authentication page
│   │   ├── Dashboard.jsx       # User dashboard
│   │   ├── ChatPage.jsx        # AI chat interface
│   │   ├── Profile.jsx         # User profile management
│   │   ├── Achievements.jsx    # Achievements display
│   │   └── Leaderboard.jsx     # Ranking system
│   ├── services/
│   │   └── api.js              # API service layer
│   ├── App.jsx                 # Main app component with routing
│   ├── main.jsx               # Entry point
│   └── index.css              # Global styles
├── package.json
├── vite.config.js
└── tailwind.config.js
```

## API Endpoints

### User Management APIs
**Base URL**: `http://localhost:8081/api`

#### 1. Create User (Register)
```http
POST /users
Content-Type: application/json

Request Body:
{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string",
  "department": "string"
}

Response: 201 Created
{
  "id": number,
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string",
  "department": "string",
  "totalPoints": number,
  "level": number,
  "messagesSent": number,
  "createdAt": "ISO 8601 datetime string",
  "lastLogin": "ISO 8601 datetime string" | null,
  "chatMessages": [],
  "achievements": []
}
```

#### 2. Get All Users
```http
GET /users

Response: 200 OK
[
  {
    "id": number,
    "username": "string",
    "fullName": "string",
    "email": "string",
    "department": "string",
    "totalPoints": number,
    "level": number,
    "messagesSent": number,
    "createdAt": "datetime",
    "lastLogin": "datetime"
  }
]
```

#### 3. Get User by ID
```http
GET /users/{id}

Response: 200 OK
{
  "id": number,
  "username": "string",
  "email": "string",
  "fullName": "string",
  "department": "string",
  "totalPoints": number,
  "level": number,
  "messagesSent": number,
  "createdAt": "datetime",
  "lastLogin": "datetime"
}
```

#### 4. Get User by Username
```http
GET /users/username/{username}

Response: 200 OK
{
  "id": number,
  "username": "string",
  "password": "string",
  "email": "string",
  "fullName": "string",
  "department": "string",
  "totalPoints": number,
  "level": number,
  "messagesSent": number,
  "createdAt": "datetime",
  "lastLogin": "datetime"
}
```

#### 5. Update User
```http
PUT /users/{id}
Content-Type: application/json

Request Body:
{
  "id": number,
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string",
  "department": "string",
  "totalPoints": number,
  "level": number,
  "messagesSent": number
}

Response: 200 OK
{
  "id": number,
  "username": "string",
  "email": "string",
  "fullName": "string",
  "department": "string",
  "totalPoints": number,
  "level": number,
  "messagesSent": number
}
```

#### 6. Record Login
```http
POST /users/{id}/login

Response: 200 OK
{
  "id": number,
  "username": "string",
  "lastLogin": "datetime"
}
```

### Chat APIs

#### 1. Send Message
```http
POST /chat/message
Content-Type: application/json

Request Body:
{
  "userId": number,
  "message": "string"
}

Response: 200 OK
{
  "response": "string",        // AI response text
  "pointsEarned": number        // Points earned for this message
}
```

#### 2. Get Chat History
```http
GET /chat/history/{userId}

Response: 200 OK
[
  {
    "id": number,
    "userMessage": "string",
    "botResponse": "string",
    "createdAt": "datetime",
    "responseTimeMs": number,
    "pointsEarned": number
  }
]
```

#### 3. Get Recent Chat History
```http
GET /chat/history/{userId}/recent?limit={number}

Response: 200 OK
[
  {
    "id": number,
    "userMessage": "string",
    "botResponse": "string",
    "createdAt": "datetime",
    "pointsEarned": number
  }
]
```

### Gamification APIs

#### 1. Get User Stats
```http
GET /gamification/users/{userId}/stats

Response: 200 OK
{
  "totalPoints": number,
  "level": number,
  "messagesSent": number,
  "dailyStreak": number
}
```

#### 2. Get User Achievements
```http
GET /gamification/users/{userId}/achievements

Response: 200 OK
[
  {
    "id": number,
    "user": {
      "id": number,
      "username": "string",
      "fullName": "string"
    },
    "achievement": {
      "id": number,
      "name": "string",
      "description": "string",
      "iconUrl": "string",
      "pointsReward": number,
      "type": "MESSAGES_SENT" | "CONSECUTIVE_DAYS" | "POINTS_EARNED" | "TOPICS_EXPLORED" | "LEVEL_REACHED",
      "targetValue": number
    },
    "earnedAt": "datetime",
    "progress": number,
    "completed": boolean
  }
]
```

#### 3. Get Completed Achievements
```http
GET /gamification/users/{userId}/achievements/completed

Response: 200 OK
[
  {
    "id": number,
    "achievement": {
      "id": number,
      "name": "string",
      "description": "string",
      "iconUrl": "string",
      "pointsReward": number,
      "type": "string",
      "targetValue": number
    },
    "completedAt": "datetime"
  }
]
```

#### 4. Get All Achievements
```http
GET /gamification/achievements

Response: 200 OK
[
  {
    "id": number,
    "name": "string",
    "description": "string",
    "iconUrl": "string",
    "pointsReward": number,
    "type": "string",
    "targetValue": number
  }
]
```

#### 5. Get Leaderboard
```http
GET /gamification/leaderboard

Response: 200 OK
[
  {
    "id": number,
    "username": "string",
    "fullName": "string",
    "email": "string",
    "department": "string",
    "totalPoints": number,
    "level": number,
    "messagesSent": number
  }
]
// Sorted by totalPoints descending
```

## Data Models

### User Entity
```typescript
interface User {
  id: number;
  username: string;
  email: string;
  password: string;
  fullName: string;
  department: string;
  totalPoints: number;
  level: number;
  messagesSent: number;
  createdAt: string; // ISO 8601 datetime
  lastLogin: string | null; // ISO 8601 datetime
  chatMessages?: ChatMessage[];
  achievements?: UserAchievement[];
}
```

### ChatMessage Entity
```typescript
interface ChatMessage {
  id: number;
  userMessage: string;
  botResponse: string;
  createdAt: string; // ISO 8601 datetime
  responseTimeMs: number;
  pointsEarned: number;
}
```

### Achievement Entity
```typescript
interface Achievement {
  id: number;
  name: string;
  description: string;
  iconUrl: string;
  pointsReward: number;
  type: 'MESSAGES_SENT' | 'CONSECUTIVE_DAYS' | 'POINTS_EARNED' | 'TOPICS_EXPLORED' | 'LEVEL_REACHED';
  targetValue: number;
}
```

### UserAchievement Entity
```typescript
interface UserAchievement {
  id: number;
  achievement: Achievement;
  earnedAt: string; // ISO 8601 datetime
  progress: number;
  completed: boolean;
}
```

### UserStats
```typescript
interface UserStats {
  totalPoints: number;
  level: number;
  messagesSent: number;
  dailyStreak: number;
}
```

## Authentication Flow
1. User enters credentials on Login page
2. Frontend calls `GET /users/username/{username}`
3. Frontend compares password (client-side validation)
4. If valid, call `POST /users/{id}/login` to record login
5. Store user object in `localStorage` as 'currentUser'
6. Redirect to Dashboard

**Note**: Currently no JWT/token-based auth. Simple username/password validation.

## Routing Structure
```javascript
/ → Redirect to /login
/login → Login/Register page (public)
/dashboard → User dashboard (protected)
/chat → Chat interface (protected)
/profile → User profile settings (protected)
/achievements → Achievements page (protected)
/leaderboard → Leaderboard rankings (protected)
```

**Protected Routes**: Require user to be stored in localStorage, redirect to /login if not authenticated.

## State Management
- **User State**: Stored in App.jsx and passed as props
- **Local Storage**: Used for persisting user session
  - Key: `'currentUser'`
  - Value: JSON stringified User object

## Current API Service (api.js)
```javascript
const API_BASE_URL = 'http://localhost:8081/api';

// Example usage:
import { userAPI, chatAPI, gamificationAPI } from './services/api';

// User operations
await userAPI.createUser(userData);
await userAPI.getUserByUsername(username);
await userAPI.updateUser(id, userData);
await userAPI.recordLogin(id);

// Chat operations
await chatAPI.sendMessage({ userId, message });
await chatAPI.getChatHistory(userId);
await chatAPI.getRecentChatHistory(userId, limit);

// Gamification operations
await gamificationAPI.getUserStats(userId);
await gamificationAPI.getUserAchievements(userId);
await gamificationAPI.getCompletedAchievements(userId);
await gamificationAPI.getAllAchievements();
await gamificationAPI.getLeaderboard();
```

## Design Requirements

### General Guidelines
- Professional, corporate design aesthetic
- Minimal color palette (blacks, grays, whites, one accent color)
- Clean typography with clear hierarchy
- No emojis or playful icons
- Data-focused design
- High information density where appropriate
- Professional spacing and alignment

### Page-Specific Requirements

#### Login Page
- Clean form design
- Professional branding
- Clear error states
- Tab/toggle between login and register

#### Dashboard
- Key metrics overview (Level, Points, Messages, Achievements)
- Recent activity list
- Quick navigation to other features
- Data visualization where appropriate

#### Chat Page
- Clean conversation interface
- Clear message distinction (user vs AI)
- Professional input area
- Chat history display
- Loading states

#### Profile Page
- User information display
- Edit capability
- Statistics overview
- Account settings

#### Achievements Page
- Grid or list of achievements
- Progress tracking
- Locked/unlocked states
- Completion statistics

#### Leaderboard Page
- Ranked list of users
- User statistics (points, level, messages)
- Current user highlighting
- Sortable/filterable if desired

## Vite Configuration
```javascript
// vite.config.js
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true
    }
  }
}
```

## Backend Server
- **Port**: 8081
- **Tech**: Spring Boot with H2 in-memory database
- **AI Model**: Ollama (local) on port 11434
- **CORS**: Enabled for localhost:3000

## Notes
- All datetime strings follow ISO 8601 format
- Points system: Users earn points for sending messages
- Level calculation: Based on total points (e.g., level 1 = 0-99 points, level 2 = 100-299 points, etc.)
- Daily streak: Tracks consecutive days of login
- Response times may vary (Ollama can take 30-180 seconds depending on model and hardware)

## Error Handling
- Network errors should display user-friendly messages
- Failed API calls should not crash the application
- Loading states should be shown during async operations
- Form validation should be clear and helpful

## Performance Considerations
- Chat history can be large - implement pagination or lazy loading if needed
- Leaderboard should handle 50+ users efficiently
- Real-time updates are not implemented (manual refresh required)

## Accessibility
- Semantic HTML
- Keyboard navigation
- Screen reader compatibility
- Proper focus management
- ARIA labels where appropriate
