import React from 'react';
import {
  gamificationAPI,
  skillsAPI,
  googleTasksAPI,
  googleCalendarAPI
} from '../services/api';

const levelColors = {
  'Not started': 'bg-gray-100 text-gray-700 border-gray-200',
  Beginner: 'bg-blue-50 text-blue-800 border-blue-200',
  Intermediate: 'bg-amber-50 text-amber-800 border-amber-200',
  Advanced: 'bg-green-50 text-green-800 border-green-200'
};

const levelProgress = {
  'Not started': 5,
  Beginner: 35,
  Intermediate: 65,
  Advanced: 90
};

export default function Dashboard() {
  const [stats, setStats] = React.useState(null);
  const [skillMap, setSkillMap] = React.useState(null);
  const [leaderboard, setLeaderboard] = React.useState([]);
  const [tasks, setTasks] = React.useState([]);
  const [meetings, setMeetings] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState('');
  const [syncing, setSyncing] = React.useState(false);
  const [syncMessage, setSyncMessage] = React.useState('');
  const [mapping, setMapping] = React.useState(false);

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  const fetchDashboard = React.useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [s, map, lb, taskData, meetingData] = await Promise.all([
        gamificationAPI.getUserStats(user.id),
        skillsAPI.getSkillMap(user.id, false),
        gamificationAPI.getLeaderboard(),
        googleTasksAPI.getTasks(undefined, 5),
        googleCalendarAPI.getEvents(undefined, 5)
      ]);
      setStats(s);
      setSkillMap(map);
      setLeaderboard(Array.isArray(lb) ? lb.slice(0, 5) : []);
      setTasks(Array.isArray(taskData) ? taskData : []);
      setMeetings(Array.isArray(meetingData) ? meetingData : []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  React.useEffect(() => {
    if (user?.id) {
      fetchDashboard();
    }
  }, [user?.id, fetchDashboard]);

  const handleSyncActivity = async () => {
    if (!user?.id) return;
    setSyncing(true);
    setSyncMessage('');
    try {
      const result = await gamificationAPI.syncUserActivity(user.id);
      setSyncMessage(
        `Synced ${result.tasksCompletedAdded} tasks and ${result.meetingsCompletedAdded} meetings. +${result.pointsEarned} points.`
      );
      const updated = await gamificationAPI.getUserStats(user.id);
      setStats(updated);
    } catch (e) {
      setSyncMessage(e.message || 'Failed to sync activity.');
    } finally {
      setSyncing(false);
    }
  };

  const handleRefreshSkillMap = async () => {
    if (!user?.id) return;
    setMapping(true);
    try {
      const map = await skillsAPI.getSkillMap(user.id, true);
      setSkillMap(map);
    } catch (e) {
      setError(e.message || 'Failed to update skill map.');
    } finally {
      setMapping(false);
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center h-full">
      <div className="text-gray-600 text-base">Loading dashboard...</div>
    </div>
  );

  if (error) return (
    <div className="flex items-center justify-center h-full">
      <div className="text-red-700 text-base">{error}</div>
    </div>
  );

  const mappings = skillMap?.mappings || [];
  const grouped = mappings.reduce((acc, item) => {
    acc[item.category] = acc[item.category] || [];
    acc[item.category].push(item);
    return acc;
  }, {});

  const learningPercent = Math.min(100, Math.round(((stats?.totalPoints || 0) % 100)));

  return (
    <div className="h-full overflow-y-auto">
      <div className="max-w-7xl mx-auto space-y-6">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
          <div>
            <h2 className="text-2xl font-semibold text-ink">Welcome back, {user?.fullName}!</h2>
            <p className="text-base text-gray-600 mt-1">Your learning progress and skill map overview</p>
          </div>
          <div className="flex flex-col sm:flex-row gap-3">
            <button className="btn-primary" onClick={handleSyncActivity} disabled={syncing}>
              {syncing ? 'Syncing activity...' : 'Sync tasks and meetings'}
            </button>
            <button className="btn-secondary" onClick={handleRefreshSkillMap} disabled={mapping}>
              {mapping ? 'Updating skill map...' : 'Update skill map'}
            </button>
          </div>
        </div>

        {syncMessage && (
          <div className="text-base text-ink bg-mist border border-border rounded-lg px-4 py-3">
            {syncMessage}
          </div>
        )}

        <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <MetricCard title="Level" value={stats?.level ?? '-'} color="blue" />
          <MetricCard title="Total Points" value={stats?.totalPoints ?? '-'} color="yellow" />
          <MetricCard title="Tasks Completed" value={stats?.tasksCompleted ?? 0} color="green" />
          <MetricCard title="Meetings Completed" value={stats?.meetingsCompleted ?? 0} color="red" />
        </section>

        <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="card">
            <div className="card-header">Learning Progress</div>
            <div className="card-body space-y-3">
              <div className="text-base text-gray-700">Next level progress</div>
              <div className="w-full bg-gray-100 rounded-full h-3">
                <div className="bg-accent h-3 rounded-full" style={{ width: `${learningPercent}%` }}></div>
              </div>
              <div className="text-sm text-gray-600">{learningPercent}% of current level completed</div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">Latest Tasks</div>
            <div className="card-body space-y-3">
              {tasks.length === 0 ? (
                <div className="text-gray-600">No tasks available.</div>
              ) : (
                tasks.map((task) => (
                  <div key={task.id} className="space-y-1">
                    <div className="text-base font-semibold text-ink">{task.title}</div>
                    <div className="text-sm text-gray-600">Status: {task.status || 'needsAction'}</div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-header">Latest Meetings</div>
            <div className="card-body space-y-3">
              {meetings.length === 0 ? (
                <div className="text-gray-600">No meetings available.</div>
              ) : (
                meetings.map((meeting) => (
                  <div key={meeting.id} className="space-y-1">
                    <div className="text-base font-semibold text-ink">{meeting.summary}</div>
                    <div className="text-sm text-gray-600">{meeting.start}</div>
                  </div>
                ))
              )}
            </div>
          </div>
        </section>

        <section className="card">
          <div className="card-header flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div>
              <div className="text-lg font-semibold text-ink">Skill Map</div>
              <div className="text-sm text-gray-600 mt-1">
                Hybrid view: predefined catalog + AI mapping from recent learning activity.
              </div>
            </div>
            <div className="text-sm text-gray-600">
              {skillMap?.updatedAt ? `Updated: ${new Date(skillMap.updatedAt).toLocaleString()}` : 'Not updated yet'}
            </div>
          </div>
          <div className="card-body space-y-6">
            {Object.keys(grouped).length === 0 && (
              <div className="text-gray-600">No skills mapped yet.</div>
            )}
            {Object.entries(grouped).map(([category, items]) => (
              <div key={category} className="space-y-3">
                <div className="text-base font-semibold text-ink">{category}</div>
                <div className="space-y-3">
                  {items.map((skill) => {
                    const bar = levelProgress[skill.level] ?? 5;
                    return (
                      <div key={skill.skillId} className="border border-border rounded-lg p-4 bg-white">
                        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3">
                          <div className="space-y-1">
                            <div className="text-base font-semibold text-ink">{skill.name}</div>
                            <div className="text-sm text-gray-600">{skill.evidence}</div>
                          </div>
                          <div className="flex flex-wrap items-center gap-2">
                            <span className={`text-sm font-semibold px-3 py-1 rounded-full border ${levelColors[skill.level] || levelColors['Not started']}`}>
                              {skill.level}
                            </span>
                            <span className="text-sm text-gray-600">Confidence: {skill.confidence}%</span>
                          </div>
                        </div>
                        <div className="mt-3">
                          <div className="w-full bg-gray-100 rounded-full h-2">
                            <div className="bg-accent h-2 rounded-full" style={{ width: `${bar}%` }}></div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="card">
          <div className="card-header">Leaderboard Snapshot</div>
          <div className="card-body space-y-3">
            {leaderboard.length === 0 ? (
              <div className="text-gray-600">No leaderboard data yet.</div>
            ) : (
              leaderboard.map((entry, index) => (
                <div key={entry.id || index} className="flex items-center justify-between">
                  <div className="text-base font-semibold text-ink">{index + 1}. {entry.fullName || entry.username}</div>
                  <div className="text-sm text-gray-600">{entry.totalPoints ?? 0} pts</div>
                </div>
              ))
            )}
          </div>
        </section>
      </div>
    </div>
  );
}

function MetricCard({ title, value, color = 'blue' }) {
  const colorClasses = {
    blue: 'bg-blue-50 text-blue-800 border-blue-200',
    yellow: 'bg-yellow-50 text-yellow-800 border-yellow-200',
    green: 'bg-green-50 text-green-800 border-green-200',
    red: 'bg-red-50 text-red-800 border-red-200'
  };

  return (
    <div className={`card border-2 ${colorClasses[color]}`}>
      <div className="card-body">
        <div className="text-base font-semibold text-gray-700">{title}</div>
        <div className="text-3xl font-semibold text-ink mt-2">{value}</div>
      </div>
    </div>
  );
}
