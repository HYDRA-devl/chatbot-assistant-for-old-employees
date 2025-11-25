import axios from 'axios';

const API_BASE_URL = '/api'; // proxied to http://localhost:8081

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

const handle = async (promise) => {
  try {
    const res = await promise;
    return res.data;
  } catch (err) {
    const message = err?.response?.data?.message || err.message || 'Request failed';
    throw new Error(message);
  }
};

export const userAPI = {
  createUser: (data) => handle(api.post('/users', data)),
  getAllUsers: () => handle(api.get('/users')),
  getUserById: (id) => handle(api.get(`/users/${id}`)),
  getUserByUsername: (username) => handle(api.get(`/users/username/${encodeURIComponent(username)}`)),
  updateUser: (id, data) => handle(api.put(`/users/${id}`, data)),
  recordLogin: (id) => handle(api.post(`/users/${id}/login`)),
};

export const chatAPI = {
  sendMessage: ({ userId, message }) => handle(api.post('/chat/message', { userId, message })),
  getChatHistory: (userId) => handle(api.get(`/chat/history/${userId}`)),
  getRecentChatHistory: (userId, limit = 10) => handle(api.get(`/chat/history/${userId}/recent`, { params: { limit } })),
};

export const gamificationAPI = {
  getUserStats: (userId) => handle(api.get(`/gamification/users/${userId}/stats`)),
  getUserAchievements: (userId) => handle(api.get(`/gamification/users/${userId}/achievements`)),
  getCompletedAchievements: (userId) => handle(api.get(`/gamification/users/${userId}/achievements/completed`)),
  getAllAchievements: () => handle(api.get('/gamification/achievements')),
  getLeaderboard: () => handle(api.get('/gamification/leaderboard')),
};

