import React from 'react';
import { gamificationAPI } from '../services/api';

export default function Leaderboard() {
  const [rows, setRows] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState('');

  const currentUser = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  React.useEffect(() => {
    let mounted = true;
    async function fetchLeaderboard() {
      try {
        const data = await gamificationAPI.getLeaderboard();
        if (mounted) setRows(data);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    fetchLeaderboard();
    return () => { mounted = false; };
  }, []);

  if (loading) return <div>Loading leaderboard…</div>;
  if (error) return <div className="text-red-600">{error}</div>;

  return (
    <div className="card">
      <div className="card-header">Leaderboard</div>
      <div className="card-body">
        <div className="overflow-x-auto">
          <table className="min-w-full border-separate border-spacing-0">
            <thead>
              <tr className="text-left text-sm text-gray-500">
                <th className="px-4 py-2 border-b border-gray-200">Rank</th>
                <th className="px-4 py-2 border-b border-gray-200">User</th>
                <th className="px-4 py-2 border-b border-gray-200">Department</th>
                <th className="px-4 py-2 border-b border-gray-200">Level</th>
                <th className="px-4 py-2 border-b border-gray-200">Points</th>
                <th className="px-4 py-2 border-b border-gray-200">Messages</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r, idx) => {
                const isMe = currentUser && r.id === currentUser.id;
                return (
                  <tr key={r.id} className={`${isMe ? 'bg-blue-50' : ''} text-sm`}> 
                    <td className="px-4 py-2 border-b border-gray-100">{idx + 1}</td>
                    <td className="px-4 py-2 border-b border-gray-100">
                      <div className="text-gray-900 font-medium">{r.fullName || r.username}</div>
                      <div className="text-gray-500">@{r.username}</div>
                    </td>
                    <td className="px-4 py-2 border-b border-gray-100">{r.department || '-'}</td>
                    <td className="px-4 py-2 border-b border-gray-100">{r.level ?? '-'}</td>
                    <td className="px-4 py-2 border-b border-gray-100 font-medium">{r.totalPoints ?? 0}</td>
                    <td className="px-4 py-2 border-b border-gray-100">{r.messagesSent ?? 0}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

