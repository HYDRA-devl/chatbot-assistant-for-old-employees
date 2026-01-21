import React from 'react';
import { gamificationAPI } from '../services/api';

export default function Achievements() {
  const [items, setItems] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState('');

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  React.useEffect(() => {
    let mounted = true;
    async function fetchAchievements() {
      try {
        const data = await gamificationAPI.getUserAchievements(user.id);
        if (mounted) setItems(data);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    if (user?.id) fetchAchievements();
    return () => { mounted = false; };
  }, [user?.id]);

  if (loading) return (
    <div className="flex items-center justify-center h-full">
      <div className="text-gray-500">Loading achievements…</div>
    </div>
  );
  
  if (error) return (
    <div className="flex items-center justify-center h-full">
      <div className="text-red-600">{error}</div>
    </div>
  );

  return (
    <div className="h-full overflow-y-auto p-6">
      <div className="max-w-7xl mx-auto">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Achievements</h2>
          <p className="text-gray-600 mt-1">Track your learning milestones</p>
        </div>

        <div className="card">
          <div className="card-body">
            {items.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                No achievements yet. Keep learning to unlock achievements!
              </div>
            )}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {items.map((ua) => (
                <div 
                  key={ua.id} 
                  className={`rounded-lg border-2 p-4 transition-all ${
                    ua.completed 
                      ? 'bg-gradient-to-br from-blue-50 to-purple-50 border-blue-300' 
                      : 'bg-gray-50 border-gray-200'
                  }`}
                >
                  <div className="text-xs font-semibold text-gray-500 uppercase">{ua.achievement.type}</div>
                  <div className="text-lg font-bold text-gray-900 mt-1">{ua.achievement.name}</div>
                  <div className="text-sm text-gray-600 mt-2">{ua.achievement.description}</div>
                  
                  <div className="mt-4 flex items-center justify-between text-sm">
                    <span className="text-gray-600">Reward</span>
                    <span className="font-bold text-yellow-600">+{ua.achievement.pointsReward} pts</span>
                  </div>
                  
                  <div className="mt-4">
                    <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                      <div 
                        className={`h-2 rounded-full transition-all ${
                          ua.completed ? 'bg-gradient-to-r from-blue-500 to-purple-500' : 'bg-gray-400'
                        }`}
                        style={{ width: `${Math.min(100, Math.round((ua.progress || 0) * 100))}%` }} 
                      />
                    </div>
                    <div className="mt-1 text-xs text-gray-500 flex justify-between">
                      <span>Progress</span>
                      <span className="font-medium">{Math.round((ua.progress || 0) * 100)}%</span>
                    </div>
                  </div>
                  
                  {ua.completed && (
                    <div className="mt-3 flex items-center gap-2 text-xs text-green-700 bg-green-100 border border-green-300 rounded-lg px-3 py-1.5">
                      <span className="text-base">✓</span>
                      <span className="font-semibold">Completed</span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
