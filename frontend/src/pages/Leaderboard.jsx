import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { gamificationAPI } from '../services/api';

const Leaderboard = ({ user, onLogout }) => {
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentUserRank, setCurrentUserRank] = useState(null);

  useEffect(() => {
    fetchLeaderboard();
  }, [user.id]);

  const fetchLeaderboard = async () => {
    try {
      const response = await gamificationAPI.getLeaderboard();
      setLeaderboard(response.data);
      
      const userIndex = response.data.findIndex(u => u.id === user.id);
      if (userIndex !== -1) {
        setCurrentUserRank(userIndex + 1);
      }
      
      setLoading(false);
    } catch (error) {
      console.error('Error fetching leaderboard:', error);
      setLoading(false);
    }
  };

  const getRankBadge = (rank) => {
    switch (rank) {
      case 1: return { emoji: '🥇', color: 'from-yellow-400 to-yellow-600' };
      case 2: return { emoji: '🥈', color: 'from-gray-300 to-gray-500' };
      case 3: return { emoji: '🥉', color: 'from-orange-400 to-orange-600' };
      default: return { emoji: '👤', color: 'from-blue-400 to-blue-600' };
    }
  };

  if (loading) {
    return (
      <Layout user={user} onLogout={onLogout}>
        <div className="flex items-center justify-center h-full">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p className="text-gray-600">Loading leaderboard...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout user={user} onLogout={onLogout}>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">📈 Leaderboard</h1>
            <p className="text-gray-600">
              See how you rank among {leaderboard.length} colleagues
            </p>
          </div>
          {currentUserRank && (
            <div className="bg-white border border-gray-200 rounded-xl px-6 py-4 text-center">
              <p className="text-sm text-gray-600 mb-1">Your Rank</p>
              <p className="text-3xl font-bold text-blue-600">#{currentUserRank}</p>
            </div>
          )}
        </div>

        {/* Top 3 Podium */}
        {leaderboard.length >= 3 && (
          <div className="grid grid-cols-3 gap-4">
            {/* 2nd Place */}
            <div className="pt-8">
              <div className="bg-white border-2 border-gray-300 rounded-xl p-6 text-center shadow-sm">
                <div className="w-16 h-16 bg-gradient-to-br from-gray-300 to-gray-500 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-3xl">🥈</span>
                </div>
                <div className="text-2xl font-bold text-gray-700 mb-1">2nd</div>
                <div className="text-sm font-medium text-gray-900 mb-2 truncate">
                  {leaderboard[1].fullName}
                </div>
                <div className="text-lg font-bold text-gray-700">{leaderboard[1].totalPoints.toLocaleString()}</div>
                <div className="text-xs text-gray-500">Level {leaderboard[1].level}</div>
              </div>
            </div>

            {/* 1st Place */}
            <div>
              <div className="bg-gradient-to-br from-yellow-400 to-yellow-600 rounded-xl p-6 text-center shadow-lg transform scale-105">
                <div className="w-20 h-20 bg-white bg-opacity-30 backdrop-blur-sm rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-4xl">👑</span>
                </div>
                <div className="text-3xl font-bold text-white mb-1">1st</div>
                <div className="text-base font-semibold text-white mb-2 truncate">
                  {leaderboard[0].fullName}
                </div>
                <div className="text-2xl font-bold text-white">{leaderboard[0].totalPoints.toLocaleString()}</div>
                <div className="text-sm text-yellow-100">Level {leaderboard[0].level}</div>
              </div>
            </div>

            {/* 3rd Place */}
            <div className="pt-8">
              <div className="bg-white border-2 border-orange-300 rounded-xl p-6 text-center shadow-sm">
                <div className="w-16 h-16 bg-gradient-to-br from-orange-400 to-orange-600 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-3xl">🥉</span>
                </div>
                <div className="text-2xl font-bold text-gray-700 mb-1">3rd</div>
                <div className="text-sm font-medium text-gray-900 mb-2 truncate">
                  {leaderboard[2].fullName}
                </div>
                <div className="text-lg font-bold text-gray-700">{leaderboard[2].totalPoints.toLocaleString()}</div>
                <div className="text-xs text-gray-500">Level {leaderboard[2].level}</div>
              </div>
            </div>
          </div>
        )}

        {/* Full Leaderboard */}
        <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">All Rankings</h2>
          </div>

          <div className="divide-y divide-gray-200">
            {leaderboard.map((leaderUser, index) => {
              const rank = index + 1;
              const isCurrentUser = leaderUser.id === user.id;
              const badge = getRankBadge(rank);

              return (
                <div
                  key={leaderUser.id}
                  className={`px-6 py-4 ${
                    isCurrentUser ? 'bg-blue-50 border-l-4 border-blue-500' : 'hover:bg-gray-50'
                  } transition-colors`}
                >
                  <div className="flex items-center space-x-4">
                    {/* Rank */}
                    <div className="flex items-center space-x-2 w-16">
                      {rank <= 3 ? (
                        <span className="text-2xl">{badge.emoji}</span>
                      ) : (
                        <span className="text-lg font-bold text-gray-500">#{rank}</span>
                      )}
                    </div>

                    {/* Avatar */}
                    <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${badge.color} flex items-center justify-center text-white font-bold flex-shrink-0`}>
                      {leaderUser.fullName.charAt(0)}
                    </div>

                    {/* User Info */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2">
                        <p className="font-semibold text-gray-900 truncate">
                          {leaderUser.fullName}
                        </p>
                        {isCurrentUser && (
                          <span className="px-2 py-0.5 bg-blue-500 text-white text-xs rounded-full">
                            You
                          </span>
                        )}
                      </div>
                      <div className="flex items-center space-x-3 mt-0.5">
                        <p className="text-sm text-gray-500">@{leaderUser.username}</p>
                        <span className="px-2 py-0.5 bg-gray-100 text-gray-700 text-xs rounded-full">
                          {leaderUser.department}
                        </span>
                      </div>
                    </div>

                    {/* Stats */}
                    <div className="hidden md:flex items-center space-x-6">
                      <div className="text-right">
                        <p className="text-xs text-gray-500">Level</p>
                        <p className="text-sm font-semibold text-gray-900">{leaderUser.level}</p>
                      </div>
                      <div className="text-right">
                        <p className="text-xs text-gray-500">Messages</p>
                        <p className="text-sm font-semibold text-gray-900">{leaderUser.messagesSent}</p>
                      </div>
                      <div className="text-right min-w-[80px]">
                        <p className="text-xs text-gray-500">Points</p>
                        <p className="text-lg font-bold text-blue-600">
                          {leaderUser.totalPoints.toLocaleString()}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Stats Summary */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white border border-gray-200 rounded-xl p-6 text-center">
            <p className="text-sm text-gray-600 mb-2">Total Participants</p>
            <p className="text-3xl font-bold text-gray-900">{leaderboard.length}</p>
          </div>
          <div className="bg-white border border-gray-200 rounded-xl p-6 text-center">
            <p className="text-sm text-gray-600 mb-2">Total Points</p>
            <p className="text-3xl font-bold text-gray-900">
              {leaderboard.reduce((sum, u) => sum + u.totalPoints, 0).toLocaleString()}
            </p>
          </div>
          <div className="bg-white border border-gray-200 rounded-xl p-6 text-center">
            <p className="text-sm text-gray-600 mb-2">Total Messages</p>
            <p className="text-3xl font-bold text-gray-900">
              {leaderboard.reduce((sum, u) => sum + u.messagesSent, 0).toLocaleString()}
            </p>
          </div>
        </div>

        {/* Empty State */}
        {leaderboard.length === 0 && (
          <div className="bg-white border border-gray-200 rounded-xl p-12 text-center">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-4xl">🏆</span>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No Rankings Yet</h3>
            <p className="text-gray-600">
              Be the first to start learning and climb the leaderboard!
            </p>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default Leaderboard;
