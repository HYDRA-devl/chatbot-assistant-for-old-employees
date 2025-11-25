import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { gamificationAPI } from '../services/api';

const Achievements = ({ user, onLogout }) => {
  const [achievements, setAchievements] = useState([]);
  const [completedAchievements, setCompletedAchievements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAchievements();
  }, [user.id]);

  const fetchAchievements = async () => {
    try {
      const [allRes, completedRes] = await Promise.all([
        gamificationAPI.getAllAchievements(),
        gamificationAPI.getCompletedAchievements(user.id)
      ]);

      setAchievements(allRes.data);
      setCompletedAchievements(completedRes.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching achievements:', error);
      setLoading(false);
    }
  };

  const isCompleted = (achievementId) => {
    return completedAchievements.some(ca => ca.achievement.id === achievementId);
  };

  const getCompletionDate = (achievementId) => {
    const completed = completedAchievements.find(ca => ca.achievement.id === achievementId);
    return completed ? new Date(completed.completedAt).toLocaleDateString() : null;
  };

  const completionPercentage = () => {
    if (achievements.length === 0) return 0;
    return Math.round((completedAchievements.length / achievements.length) * 100);
  };

  if (loading) {
    return (
      <Layout user={user} onLogout={onLogout}>
        <div className="flex items-center justify-center h-full">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p className="text-gray-600">Loading achievements...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout user={user} onLogout={onLogout}>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">🏆 Achievements</h1>
          <p className="text-gray-600">
            You've completed {completedAchievements.length} of {achievements.length} achievements
          </p>
        </div>

        {/* Progress Overview */}
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <p className="text-sm text-gray-600 mb-1">Overall Progress</p>
              <p className="text-3xl font-bold text-gray-900">{completionPercentage()}%</p>
            </div>
            <div className="text-right">
              <p className="text-sm text-gray-600 mb-1">Completed</p>
              <p className="text-2xl font-bold text-blue-600">
                {completedAchievements.length}/{achievements.length}
              </p>
            </div>
          </div>
          
          <div className="relative h-3 bg-gray-100 rounded-full overflow-hidden">
            <div
              className="absolute top-0 left-0 h-full bg-gradient-to-r from-blue-500 to-purple-600 rounded-full transition-all duration-500"
              style={{ width: `${completionPercentage()}%` }}
            />
          </div>
        </div>

        {/* Achievement Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {achievements.map((achievement) => {
            const completed = isCompleted(achievement.id);
            const completionDate = getCompletionDate(achievement.id);

            return (
              <div
                key={achievement.id}
                className={`relative border rounded-xl p-6 transition-all ${
                  completed
                    ? 'bg-gradient-to-br from-yellow-50 to-orange-50 border-yellow-200 shadow-sm'
                    : 'bg-white border-gray-200 hover:border-gray-300'
                }`}
              >
                {completed && (
                  <div className="absolute top-3 right-3">
                    <div className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                      <svg className="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                    </div>
                  </div>
                )}

                <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-4 ${
                  completed 
                    ? 'bg-gradient-to-br from-yellow-400 to-orange-500' 
                    : 'bg-gray-100'
                }`}>
                  <span className={`text-2xl ${completed ? '' : 'opacity-40 grayscale'}`}>
                    {getAchievementIcon(achievement.name)}
                  </span>
                </div>

                <h3 className={`text-lg font-semibold mb-2 ${
                  completed ? 'text-gray-900' : 'text-gray-700'
                }`}>
                  {achievement.name}
                </h3>

                <p className={`text-sm mb-4 ${
                  completed ? 'text-gray-700' : 'text-gray-600'
                }`}>
                  {achievement.description}
                </p>

                <div className="flex items-center justify-between pt-4 border-t border-gray-200">
                  <div className="flex items-center space-x-1.5">
                    <span className="text-yellow-500">⭐</span>
                    <span className={`text-sm font-semibold ${
                      completed ? 'text-gray-900' : 'text-gray-600'
                    }`}>
                      {achievement.pointsReward} points
                    </span>
                  </div>
                  {completed && completionDate && (
                    <span className="text-xs text-gray-500">
                      {completionDate}
                    </span>
                  )}
                </div>

                {!completed && (
                  <div className="mt-3 pt-3 border-t border-gray-200">
                    <p className="text-xs text-gray-500">
                      🔒 Keep learning to unlock
                    </p>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Empty State */}
        {achievements.length === 0 && (
          <div className="bg-white border border-gray-200 rounded-xl p-12 text-center">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-4xl">🎯</span>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No Achievements Yet</h3>
            <p className="text-gray-600 mb-6">
              Start chatting with the AI assistant to unlock achievements!
            </p>
          </div>
        )}
      </div>
    </Layout>
  );
};

const getAchievementIcon = (name) => {
  const lowerName = name.toLowerCase();
  if (lowerName.includes('first') || lowerName.includes('begin')) return '🌟';
  if (lowerName.includes('streak') || lowerName.includes('week')) return '🔥';
  if (lowerName.includes('explorer') || lowerName.includes('curious')) return '🧭';
  if (lowerName.includes('master') || lowerName.includes('expert')) return '👑';
  if (lowerName.includes('chat') || lowerName.includes('message')) return '💬';
  if (lowerName.includes('level') || lowerName.includes('rank')) return '⭐';
  if (lowerName.includes('social') || lowerName.includes('friend')) return '👥';
  if (lowerName.includes('tech') || lowerName.includes('learn')) return '💡';
  if (lowerName.includes('champion') || lowerName.includes('winner')) return '🏆';
  if (lowerName.includes('dedicated') || lowerName.includes('commit')) return '💪';
  return '🎯';
};

export default Achievements;
