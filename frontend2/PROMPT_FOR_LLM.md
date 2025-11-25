# Prompt for Another LLM - Frontend Redesign

Copy and paste this to another LLM:

---

I need you to redesign a React frontend for an Employee Learning Platform. The design should be **extremely professional, minimal, and corporate** - think enterprise SaaS applications like Stripe Dashboard, Linear, or GitHub.

## Design Philosophy
- **NO emojis or playful icons** - use professional iconography only
- **Minimal color palette** - blacks, grays, whites, and ONE subtle accent color (suggest blue or indigo)
- **Clean typography** - clear hierarchy, professional fonts
- **Data-focused** - emphasize information and functionality over decoration
- **High contrast** - excellent readability
- **Generous whitespace** - not cluttered
- **Subtle shadows and borders** - professional depth

## Tech Stack
- React 18.3.1 + Vite
- React Router DOM
- Axios for API calls
- Tailwind CSS for styling
- Backend API on `http://localhost:8081/api`

## Pages to Redesign
1. **Login Page** - Split or centered form, professional authentication
2. **Dashboard** - Stats overview, recent activity
3. **Chat Interface** - Clean conversation UI (like ChatGPT but more corporate)
4. **Profile Page** - User settings and information
5. **Achievements Page** - Progress tracking grid/list
6. **Leaderboard** - Ranked user list
7. **Layout Component** - Sidebar navigation

## API Endpoints Summary

### Authentication
- `POST /users` - Register user
- `GET /users/username/{username}` - Login validation
- `POST /users/{id}/login` - Record login

### User
- `GET /users/{id}` - Get user details
- `PUT /users/{id}` - Update user

### Chat
- `POST /chat/message` - Send message, returns AI response + points earned
- `GET /chat/history/{userId}` - Get chat history
- `GET /chat/history/{userId}/recent?limit=N` - Get recent chats

### Gamification
- `GET /gamification/users/{userId}/stats` - Get user stats (points, level, streak, messages)
- `GET /gamification/users/{userId}/achievements` - Get user achievements
- `GET /gamification/achievements` - Get all achievements
- `GET /gamification/leaderboard` - Get ranked users

## Key Data Structures

**User Object:**
```json
{
  "id": 1,
  "username": "john.doe",
  "fullName": "John Doe",
  "email": "john@company.com",
  "department": "Engineering",
  "totalPoints": 1500,
  "level": 5,
  "messagesSent": 42,
  "createdAt": "2024-01-15T10:30:00Z",
  "lastLogin": "2024-11-23T14:20:00Z"
}
```

**Chat Message:**
```json
{
  "id": 1,
  "userMessage": "What is React?",
  "botResponse": "React is a JavaScript library...",
  "createdAt": "2024-11-23T14:25:00Z",
  "pointsEarned": 10
}
```

**Achievement:**
```json
{
  "id": 1,
  "name": "First Steps",
  "description": "Send your first message",
  "pointsReward": 50,
  "type": "MESSAGES_SENT",
  "targetValue": 1
}
```

**User Stats:**
```json
{
  "totalPoints": 1500,
  "level": 5,
  "messagesSent": 42,
  "dailyStreak": 7
}
```

## Functional Requirements

### Authentication Flow
1. User enters username/password
2. Call `GET /users/username/{username}` to fetch user
3. Validate password client-side
4. Call `POST /users/{id}/login` to record login
5. Store user in localStorage as 'currentUser'
6. Redirect to /dashboard

### Chat Flow
1. User types message
2. POST to `/chat/message` with `{userId, message}`
3. Display loading state
4. Show AI response + points earned
5. Update chat history

### Protected Routes
- Check localStorage for 'currentUser'
- Redirect to /login if not authenticated

## Design Deliverables
Please provide:
1. **All 7 page components** with complete code
2. **Layout component** with professional sidebar navigation
3. **Updated index.css** with professional styling
4. **Color palette definition** - specify exact colors used
5. **Typography system** - font families, sizes, weights

## Design Inspiration
- Stripe Dashboard (clean data tables, minimal colors)
- Linear (excellent typography, subtle animations)
- GitHub (professional, functional, clear hierarchy)
- Vercel Dashboard (modern, clean, monochromatic)
- Notion (organized, clear information architecture)

## Must-Have Design Elements
- Professional navigation sidebar (collapsible optional)
- Clean table designs for leaderboard
- Card-based layouts for dashboard
- Professional form inputs with clear labels
- Subtle hover states and transitions
- Loading states (spinners, skeleton screens)
- Empty states with clear CTAs
- Proper focus states for accessibility

## Constraints
- Must use Tailwind CSS only (no external UI libraries)
- Must maintain all existing functionality
- Must work with existing API structure
- No need for authentication tokens (simple localStorage)

Please provide complete, production-ready code for all components. Focus on creating a design that looks like it belongs in a Fortune 500 company's internal tools.
