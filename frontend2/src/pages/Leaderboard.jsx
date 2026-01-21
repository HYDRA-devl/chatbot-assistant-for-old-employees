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

  if (loading) return (
    <div className="flex items-center justify-center h-full">
      <div className="text-gray-500">Loading leaderboard…</div>
    </div>
  );
  
  if (error) return (
    <div className="flex items-center justify-center h-full">
      <div className="text-red-600">{error}</div>
    </div>
  );

  const getMedalEmoji = (rank) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return null;
  };

  return (
    <div className="h-full overflow-y-auto p-6">
      <div className="max-w-7xl mx-auto">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Leaderboard</h2>
          <p className="text-gray-600 mt-1">Top performers in the organization</p>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="overflow-x-auto">
              <table className="min-w-full border-separate border-spacing-0">
                <thead>
                  <tr className="text-left text-xs font-semibold text-gray-500 uppercase">
                    <th className="px-4 py-3 border-b-2 border-gray-200">Rank</th>
                    <th className="px-4 py-3 border-b-2 border-gray-200">User</th>
                    <th className="px-4 py-3 border-b-2 border-gray-200">Department</th>
                    <th className="px-4 py-3 border-b-2 border-gray-200">Level</th>
                    <th className="px-4 py-3 border-b-2 border-gray-200">Points</th>
                    <th className="px-4 py-3 border-b-2 border-gray-200">Messages</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((r, idx) => {
                    const isMe = currentUser && r.id === currentUser.id;
                    const rank = idx + 1;
                    const medal = getMedalEmoji(rank);
                    
                    return (
                      <tr 
                        key={r.id} 
                        className={`${
                          isMe 
                            ? 'bg-blue-50 border-l-4 border-l-blue-500' 
                            : ''
                        } hover:bg-gray-50 transition-colors`}
                      > 
                        <td className="px-4 py-3 border-b border-gray-100">
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-gray-900">{rank}</span>
                            {medal && <span className="text-xl">{medal}</span>}
                          </div>
                        </td>
                        <td className="px-4 py-3 border-b border-gray-100">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-semibold">
                              {(r.fullName || r.username).charAt(0)}
                            </div>
                            <div>
                              <div className="text-sm font-medium text-gray-900">
                                {r.fullName || r.username}
                                {isMe && <span className="ml-2 text-xs text-blue-600 font-semibold">(You)</span>}
                              </div>
                              <div className="text-xs text-gray-500">@{r.username}</div>
                            </div>
                          </div>
                        </td>
                        <td className="px-4 py-3 border-b border-gray-100">
                          <span className="text-sm text-gray-700">{r.department || '-'}</span>
                        </td>
                        <td className="px-4 py-3 border-b border-gray-100">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                            Lvl {r.level ?? '-'}
                          </span>
                        </td>
                        <td className="px-4 py-3 border-b border-gray-100">
                          <span className="text-sm font-bold text-yellow-600">{r.totalPoints ?? 0} pts</span>
                        </td>
                        <td className="px-4 py-3 border-b border-gray-100">
                          <span className="text-sm text-gray-700">{r.messagesSent ?? 0}</span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
