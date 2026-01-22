import api from './apiClient';

const handle = (promise) => promise.then((res) => res.data);

export const authAPI = {
  login: (credentials) => handle(api.post('/auth/login', credentials)),
  register: (userData) => handle(api.post('/auth/register', userData)),
  getCurrentUser: () => handle(api.get('/auth/me')),
};

export const conversationAPI = {
  createConversation: (userId) => handle(api.post('/conversations/create', null, { params: { userId } })),
  getUserConversations: (userId) => handle(api.get(`/conversations/user/${userId}`)),
  getConversation: (id) => handle(api.get(`/conversations/${id}`)),
  getConversationMessages: (id) => handle(api.get(`/chat/conversation/${id}/messages`)),
  endConversation: (id) => handle(api.post(`/conversations/${id}/end`)),
  deleteConversation: (id) => handle(api.delete(`/conversations/${id}`))
};

export const chatAPI = {
  sendMessage: (userId, message) => handle(api.post(`/chat?userId=${userId}&message=${message}`)),
  getChatHistory: (userId) => handle(api.get(`/chat/history/${userId}`)),
  getRecentChatHistory: (userId, limit = 10) => handle(api.get(`/chat/history/${userId}/recent`, { params: { limit } }))
};

export const gamificationAPI = {
  getUserStats: (userId) => handle(api.get(`/gamification/users/${userId}/stats`)),
  getUserAchievements: (userId) => handle(api.get(`/gamification/users/${userId}/achievements`)),
  getCompletedAchievements: (userId) => handle(api.get(`/gamification/users/${userId}/achievements/completed`)),
  getAllAchievements: () => handle(api.get('/gamification/achievements')),
  getLeaderboard: () => handle(api.get('/gamification/leaderboard')),
  syncUserActivity: (userId) => handle(api.post(`/gamification/users/${userId}/sync-activity`))
};

export const quizAPI = {
  generateQuiz: (conversationId) => handle(api.post(`/quiz/generate/${conversationId}`)),
  getQuiz: (quizId) => handle(api.get(`/quiz/${quizId}`)),
  getQuizQuestions: (quizId) => handle(api.get(`/quiz/${quizId}/questions`)),
  getQuizByConversation: (conversationId) => handle(api.get(`/quiz/conversation/${conversationId}`)),
  submitQuiz: (quizId, userId, answers) => handle(api.post(`/quiz/${quizId}/submit`, { userId, answers }))
};

export const userAPI = {
  getUsers: () => handle(api.get('/users')),
  getUserById: (id) => handle(api.get(`/users/${id}`)),
  getUserByUsername: (username) => handle(api.get(`/users/username/${username}`)),
  updateUser: (id, data) => handle(api.put(`/users/${id}`, data)),
  createUser: (data) => handle(api.post('/users', data)),
  recordLogin: (id) => handle(api.post(`/users/${id}/login`))
};

export const gamificationHelpers = {
  getLevelProgress: (totalPoints) => {
    const level = Math.floor(totalPoints / 100) + 1;
    const nextLevelPoints = level * 100;
    const progress = ((totalPoints % 100) / 100) * 100;
    return { level, progress, nextLevelPoints };
  }
};

export const googleTasksAPI = {
  getTaskLists: () => handle(api.get('/google-tasks/lists')),
  getTasks: (taskListId, limit = 20) => handle(api.get('/google-tasks/tasks', { params: { taskListId, limit } }))
};

export const googleCalendarAPI = {
  getCalendars: () => handle(api.get('/google-calendar/calendars')),
  getEvents: (calendarId, limit = 20) => handle(api.get('/google-calendar/events', { params: { calendarId, limit } }))
};

export const activityAPI = {
  completeTask: (payload) => handle(api.post('/activity/tasks/complete', payload)),
  completeMeeting: (payload) => handle(api.post('/activity/meetings/complete', payload)),
  getCompleted: (userId) => handle(api.get('/activity/completed', { params: { userId } }))
};

export const skillsAPI = {
  getSkillMap: (userId, refresh = false) => handle(api.get('/skills/map', { params: { userId, refresh } }))
};

export const gmailAPI = {
  connect: () => handle(api.get('/gmail/connect')),
  fetchEmails: (userId, limit = 10) => handle(api.get('/gmail/emails', { params: { userId, limit } })),
  summarizeEmail: (userId, emailId) => handle(api.get(`/gmail/emails/${emailId}/summary`, { params: { userId } }))
};
