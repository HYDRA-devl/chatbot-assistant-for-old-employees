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

  if (loading) return <div>Loading…</div>;
  if (error) return <div className="text-red-600">{error}</div>;

  return (
    <div className="card">
      <div className="card-header">Achievements</div>
      <div className="card-body">
        {items.length === 0 && <div className="text-sm text-gray-500">No achievements yet.</div>}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map((ua) => (
            <div key={ua.id} className={`rounded-lg border p-4 ${ua.completed ? 'bg-white' : 'bg-gray-50'} border-gray-200`}>
              <div className="text-sm text-gray-500">{ua.achievement.type}</div>
              <div className="text-lg font-semibold text-gray-900">{ua.achievement.name}</div>
              <div className="text-sm text-gray-600 mt-1">{ua.achievement.description}</div>
              <div className="mt-3 flex items-center justify-between text-sm">
                <span className="text-gray-500">Points</span>
                <span className="font-medium">{ua.achievement.pointsReward}</span>
              </div>
              <div className="mt-3">
                <div className="h-2 bg-gray-200 rounded">
                  <div className="h-2 bg-accent rounded" style={{ width: `${Math.min(100, Math.round((ua.progress || 0) * 100))}%` }} />
                </div>
                <div className="mt-1 text-xs text-gray-500 flex justify-between">
                  <span>Progress</span>
                  <span>{Math.round((ua.progress || 0) * 100)}%</span>
                </div>
              </div>
              {ua.completed && (
                <div className="mt-2 text-xs text-green-700 bg-green-50 border border-green-200 rounded px-2 py-1 inline-block">Completed</div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

