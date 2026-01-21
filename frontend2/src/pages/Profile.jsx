import React from 'react';
import { userAPI, gamificationAPI } from '../services/api';

export default function Profile() {
  const [form, setForm] = React.useState(null);
  const [stats, setStats] = React.useState(null);
  const [loading, setLoading] = React.useState(true);
  const [saving, setSaving] = React.useState(false);
  const [error, setError] = React.useState('');
  const [success, setSuccess] = React.useState('');

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  React.useEffect(() => {
    let mounted = true;
    async function fetchData() {
      try {
        const [u, s] = await Promise.all([
          userAPI.getUserById(user.id),
          gamificationAPI.getUserStats(user.id),
        ]);
        if (!mounted) return;
        setForm(u);
        setStats(s);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    if (user?.id) fetchData();
    return () => { mounted = false; };
  }, [user?.id]);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const updated = await userAPI.updateUser(form.id, {
        id: form.id,
        username: form.username,
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        department: form.department,
        totalPoints: form.totalPoints,
        level: form.level,
        messagesSent: form.messagesSent,
      });
      localStorage.setItem('currentUser', JSON.stringify(updated));
      setSuccess('Profile updated successfully');
    } catch (e) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center h-full">
      <div className="text-gray-500">Loading profile…</div>
    </div>
  );

  return (
    <div className="h-full overflow-y-auto p-6">
      <div className="max-w-7xl mx-auto">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Profile Settings</h2>
          <p className="text-gray-600 mt-1">Manage your account information</p>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          <section className="md:col-span-2 card">
            <div className="card-header">Profile Information</div>
            <div className="card-body">
              {success && (
                <div className="mb-4 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg px-4 py-3">
                  ✓ {success}
                </div>
              )}
              {error && (
                <div className="mb-4 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
                  {error}
                </div>
              )}
              <form onSubmit={onSave} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="label" htmlFor="fullName">Full Name</label>
                    <input 
                      className="input" 
                      id="fullName" 
                      name="fullName" 
                      value={form.fullName || ''} 
                      onChange={onChange} 
                    />
                  </div>
                  <div>
                    <label className="label" htmlFor="username">Username</label>
                    <input 
                      className="input" 
                      id="username" 
                      name="username" 
                      value={form.username || ''} 
                      onChange={onChange} 
                    />
                  </div>
                  <div>
                    <label className="label" htmlFor="email">Email</label>
                    <input 
                      className="input" 
                      id="email" 
                      name="email" 
                      type="email" 
                      value={form.email || ''} 
                      onChange={onChange} 
                    />
                  </div>
                  <div>
                    <label className="label" htmlFor="department">Department</label>
                    <input 
                      className="input" 
                      id="department" 
                      name="department" 
                      value={form.department || ''} 
                      onChange={onChange} 
                    />
                  </div>
                  <div className="sm:col-span-2">
                    <label className="label" htmlFor="password">New Password (leave empty to keep current)</label>
                    <input 
                      className="input" 
                      id="password" 
                      name="password" 
                      type="password" 
                      placeholder="••••••••"
                      value={form.password || ''} 
                      onChange={onChange} 
                    />
                  </div>
                </div>
                <div>
                  <button className="btn-primary" disabled={saving}>
                    {saving ? 'Saving…' : 'Save Changes'}
                  </button>
                </div>
              </form>
            </div>
          </section>
          
          <section className="card">
            <div className="card-header">Statistics</div>
            <div className="card-body space-y-4">
              <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg">
                <div>
                  <div className="text-xs text-gray-600">Level</div>
                  <div className="text-2xl font-bold text-blue-600">{stats?.level ?? '-'}</div>
                </div>
                <div className="text-3xl">🎯</div>
              </div>
              
              <div className="flex items-center justify-between p-3 bg-yellow-50 rounded-lg">
                <div>
                  <div className="text-xs text-gray-600">Total Points</div>
                  <div className="text-2xl font-bold text-yellow-600">{stats?.totalPoints ?? '-'}</div>
                </div>
                <div className="text-3xl">⭐</div>
              </div>
              
              <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
                <div>
                  <div className="text-xs text-gray-600">Messages</div>
                  <div className="text-2xl font-bold text-green-600">{stats?.messagesSent ?? '-'}</div>
                </div>
                <div className="text-3xl">💬</div>
              </div>
              
              <div className="flex items-center justify-between p-3 bg-red-50 rounded-lg">
                <div>
                  <div className="text-xs text-gray-600">Daily Streak</div>
                  <div className="text-2xl font-bold text-red-600">{stats?.dailyStreak ?? 0}</div>
                </div>
                <div className="text-3xl">🔥</div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
