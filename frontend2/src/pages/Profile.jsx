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

  if (loading) return <div>Loading profile…</div>;
  if (error) return <div className="text-red-600">{error}</div>;

  return (
    <div className="grid gap-6 md:grid-cols-3">
      <section className="md:col-span-2 card">
        <div className="card-header">Profile</div>
        <div className="card-body">
          {success && <div className="mb-4 text-sm text-green-700 bg-green-50 border border-green-200 rounded px-3 py-2">{success}</div>}
          <form onSubmit={onSave} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="label" htmlFor="fullName">Full Name</label>
                <input className="input" id="fullName" name="fullName" value={form.fullName || ''} onChange={onChange} />
              </div>
              <div>
                <label className="label" htmlFor="username">Username</label>
                <input className="input" id="username" name="username" value={form.username || ''} onChange={onChange} />
              </div>
              <div>
                <label className="label" htmlFor="email">Email</label>
                <input className="input" id="email" name="email" type="email" value={form.email || ''} onChange={onChange} />
              </div>
              <div>
                <label className="label" htmlFor="department">Department</label>
                <input className="input" id="department" name="department" value={form.department || ''} onChange={onChange} />
              </div>
              <div className="sm:col-span-2">
                <label className="label" htmlFor="password">Password</label>
                <input className="input" id="password" name="password" type="password" value={form.password || ''} onChange={onChange} />
              </div>
            </div>
            <div>
              <button className="btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save changes'}</button>
            </div>
          </form>
        </div>
      </section>
      <section className="card">
        <div className="card-header">Statistics</div>
        <div className="card-body space-y-2 text-sm">
          <div className="flex justify-between"><span className="text-gray-500">Level</span><span className="font-medium">{stats?.level ?? '-'}</span></div>
          <div className="flex justify-between"><span className="text-gray-500">Total Points</span><span className="font-medium">{stats?.totalPoints ?? '-'}</span></div>
          <div className="flex justify-between"><span className="text-gray-500">Messages</span><span className="font-medium">{stats?.messagesSent ?? '-'}</span></div>
          <div className="flex justify-between"><span className="text-gray-500">Daily Streak</span><span className="font-medium">{stats?.dailyStreak ?? 0}</span></div>
        </div>
      </section>
    </div>
  );
}
