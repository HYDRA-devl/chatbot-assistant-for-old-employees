import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// User APIs
export const userAPI = {
  createUser: (userData) => api.post('/users', userData),
  getAllUsers: () => api.get('/users'),
  getUserById: (id) => api.get(`/users/${id}`),
  getUserByUsername: (username) => api.get(`/users/username/${username}`),
  updateUser: (id, userData) => api.put(`/users/${id}`, userData),
  recordLogin: (id) => api.post(`/users/${id}/login`),
};

// Chat APIs
export const chatAPI = {
  sendMessage: (message) => api.post('/chat/message', message),
  getChatHistory: (userId) => api.get(`/chat/history/${userId}`),
  getRecentChatHistory: (userId, limit = 10) => 
    api.get(`/chat/history/${userId}/recent?limit=${limit}`),
};

// Gamification APIs
export const gamificationAPI = {
  getUserStats: (userId) => api.get(`/gamification/users/${userId}/stats`),
  getUserAchievements: (userId) => api.get(`/gamification/users/${userId}/achievements`),
  getCompletedAchievements: (userId) => 
    api.get(`/gamification/users/${userId}/achievements/completed`),
  getAllAchievements: () => api.get('/gamification/achievements'),
  getLeaderboard: () => api.get('/gamification/leaderboard'),
};

export default api;
