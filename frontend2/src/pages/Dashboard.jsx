import React from 'react';
import { gamificationAPI, chatAPI } from '../services/api';

export default function Dashboard() {
  const [stats, setStats] = React.useState(null);
  const [recent, setRecent] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState('');

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  React.useEffect(() => {
    let mounted = true;
    async function fetchAll() {
      try {
        const [s, r] = await Promise.all([
          gamificationAPI.getUserStats(user.id),
          chatAPI.getRecentChatHistory(user.id, 5)
        ]);
        if (!mounted) return;
        setStats(s);
        setRecent(r);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    if (user?.id) fetchAll();
    return () => { mounted = false; };
  }, [user?.id]);

  if (loading) return <div>Loading dashboard…</div>;
  if (error) return <div className="text-red-600">{error}</div>;

  return (
    <div className="space-y-6">
      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard title="Level" value={stats?.level ?? '-'} />
        <MetricCard title="Total Points" value={stats?.totalPoints ?? '-'} />
        <MetricCard title="Messages" value={stats?.messagesSent ?? '-'} />
        <MetricCard title="Daily Streak" value={stats?.dailyStreak ?? 0} />
      </section>

      <section className="card">
        <div className="card-header">Recent Activity</div>
        <div className="card-body divide-y divide-gray-100">
          {recent?.length === 0 && (
            <div className="text-sm text-gray-500">No recent chats. Head to Chat to start.</div>
          )}
          {recent?.map((m) => (
            <div key={m.id} className="py-3">
              <div className="text-sm text-gray-900">You: {m.userMessage}</div>
              <div className="text-sm text-gray-600 mt-1">AI: {m.botResponse}</div>
              <div className="text-xs text-gray-500 mt-1">Points earned: {m.pointsEarned ?? 0}</div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

function MetricCard({ title, value }) {
  return (
    <div className="card">
      <div className="card-body">
        <div className="text-sm text-gray-500">{title}</div>
        <div className="mt-1 text-2xl font-semibold text-gray-900">{value}</div>
      </div>
    </div>
  );
}

