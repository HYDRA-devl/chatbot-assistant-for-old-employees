import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import { gamificationAPI, chatAPI } from '../services/api';

const Dashboard = ({ user, onLogout }) => {
  const [stats, setStats] = useState(null);
  const [recentChats, setRecentChats] = useState([]);
  const [achievements, setAchievements] = useState([]);

  useEffect(() => {
    fetchDashboardData();
  }, [user.id]);

  const fetchDashboardData = async () => {
    try {
      const [statsRes, chatsRes, achievementsRes] = await Promise.all([
        gamificationAPI.getUserStats(user.id),
        chatAPI.getRecentChatHistory(user.id, 5),
        gamificationAPI.getCompletedAchievements(user.id)
      ]);

      setStats(statsRes.data);
      setRecentChats(chatsRes.data);
      setAchievements(achievementsRes.data);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    }
  };

  if (!stats) {
    return (
      <Layout user={user} onLogout={onLogout}>
        <div className="flex items-center justify-center h-full">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p className="text-gray-600">Loading your dashboard...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout user={user} onLogout={onLogout}>
      <div className="space-y-6">
        {/* Welcome Section */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Welcome back, {user.fullName.split(' ')[0]}! 👋
          </h1>
          <p className="text-gray-600">
            Here's what's happening with your learning journey today
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between mb-4">
              <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                <span className="text-xl">🎯</span>
              </div>
              <span className="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-1 rounded-full">
                Level
              </span>
            </div>
            <div className="mb-1">
              <span className="text-3xl font-bold text-gray-900">{stats.level}</span>
            </div>
            <p className="text-sm text-gray-600">Current Level</p>
          </div>

          <div className="bg-white border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between mb-4">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <span className="text-xl">⭐</span>
              </div>
              <span className="text-xs font-medium text-green-600 bg-green-50 px-2 py-1 rounded-full">
                Points
              </span>
            </div>
            <div className="mb-1">
              <span className="text-3xl font-bold text-gray-900">{stats.totalPoints.toLocaleString()}</span>
            </div>
            <p className="text-sm text-gray-600">Total Points</p>
          </div>

          <div className="bg-white border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between mb-4">
              <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                <span className="text-xl">💬</span>
              </div>
              <span className="text-xs font-medium text-purple-600 bg-purple-50 px-2 py-1 rounded-full">
                Messages
              </span>
            </div>
            <div className="mb-1">
              <span className="text-3xl font-bold text-gray-900">{stats.messagesSent}</span>
            </div>
            <p className="text-sm text-gray-600">Messages Sent</p>
          </div>

          <div className="bg-white border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between mb-4">
              <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
                <span className="text-xl">🏆</span>
              </div>
              <span className="text-xs font-medium text-orange-600 bg-orange-50 px-2 py-1 rounded-full">
                Unlocked
              </span>
            </div>
            <div className="mb-1">
              <span className="text-3xl font-bold text-gray-900">{achievements.length}</span>
            </div>
            <p className="text-sm text-gray-600">Achievements</p>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Link
            to="/chat"
            className="group bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl p-6 text-white hover:shadow-lg transition-all"
          >
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-white bg-opacity-20 backdrop-blur-sm rounded-lg flex items-center justify-center">
                <span className="text-2xl">🤖</span>
              </div>
              <span className="text-white opacity-0 group-hover:opacity-100 transition-opacity">
                →
              </span>
            </div>
            <h3 className="text-xl font-semibold mb-2">Start Learning</h3>
            <p className="text-blue-100 text-sm">
              Continue your conversation with the AI assistant
            </p>
          </Link>

          <Link
            to="/achievements"
            className="group bg-white border border-gray-200 rounded-xl p-6 hover:shadow-lg transition-all"
          >
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-lg flex items-center justify-center">
                <span className="text-2xl">🏆</span>
              </div>
              <span className="text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity">
                →
              </span>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">View Achievements</h3>
            <p className="text-gray-600 text-sm">
              Track your progress and unlock new milestones
            </p>
          </Link>
        </div>

        {/* Recent Activity */}
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold text-gray-900">Recent Conversations</h2>
            <Link to="/chat" className="text-sm text-blue-600 hover:text-blue-700 font-medium">
              View all →
            </Link>
          </div>

          {recentChats.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-3xl">💭</span>
              </div>
              <p className="text-gray-600 mb-4">No conversations yet</p>
              <Link
                to="/chat"
                className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
              >
                Start your first chat
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {recentChats.map((chat) => (
                <div
                  key={chat.id}
                  className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors"
                >
                  <div className="flex items-start space-x-3">
                    <div className="w-8 h-8 bg-gradient-to-br from-purple-500 to-pink-500 rounded-lg flex items-center justify-center flex-shrink-0">
                      <span className="text-white text-sm">{user.fullName.charAt(0)}</span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-gray-900 mb-1">
                        {chat.userMessage}
                      </p>
                      <p className="text-sm text-gray-600 line-clamp-2">
                        {chat.botResponse}
                      </p>
                      <div className="flex items-center space-x-4 mt-2">
                        <span className="text-xs text-gray-500">
                          {new Date(chat.createdAt).toLocaleDateString('en-US', { 
                            month: 'short', 
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </span>
                        {chat.pointsEarned > 0 && (
                          <span className="text-xs text-green-600 font-medium">
                            +{chat.pointsEarned} pts
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default Dashboard;
