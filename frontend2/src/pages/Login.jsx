import React from 'react';
import { useNavigate } from 'react-router-dom';
import { userAPI } from '../services/api';

export default function Login({ setUser }) {
  const navigate = useNavigate();
  const [mode, setMode] = React.useState('login'); // 'login' | 'register'
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState('');

  const [form, setForm] = React.useState({
    username: '',
    password: '',
    email: '',
    fullName: '',
    department: ''
  });

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await userAPI.getUserByUsername(form.username);
      if (!user || user.password !== form.password) {
        throw new Error('Invalid username or password');
      }
      await userAPI.recordLogin(user.id);
      setUser(user);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const payload = {
        username: form.username,
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        department: form.department,
      };
      const user = await userAPI.createUser(payload);
      await userAPI.recordLogin(user.id);
      setUser(user);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid place-items-center bg-gray-50 px-4">
      <div className="w-full max-w-md card">
        <div className="card-header flex items-center justify-between">
          <span>{mode === 'login' ? 'Sign in' : 'Create account'}</span>
          <div className="text-sm text-gray-500">
            {mode === 'login' ? (
              <button className="text-accent hover:underline" onClick={() => setMode('register')}>Register</button>
            ) : (
              <button className="text-accent hover:underline" onClick={() => setMode('login')}>Sign in</button>
            )}
          </div>
        </div>
        <div className="card-body">
          {error && (
            <div className="mb-4 rounded border border-red-200 bg-red-50 text-red-700 px-3 py-2 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={mode === 'login' ? handleLogin : handleRegister} className="space-y-4">
            <div>
              <label className="label" htmlFor="username">Username</label>
              <input className="input" id="username" name="username" value={form.username} onChange={onChange} required />
            </div>

            {mode === 'register' && (
              <>
                <div>
                  <label className="label" htmlFor="fullName">Full Name</label>
                  <input className="input" id="fullName" name="fullName" value={form.fullName} onChange={onChange} required />
                </div>
                <div>
                  <label className="label" htmlFor="email">Email</label>
                  <input className="input" id="email" name="email" type="email" value={form.email} onChange={onChange} required />
                </div>
                <div>
                  <label className="label" htmlFor="department">Department</label>
                  <input className="input" id="department" name="department" value={form.department} onChange={onChange} required />
                </div>
              </>
            )}

            <div>
              <label className="label" htmlFor="password">Password</label>
              <input className="input" id="password" name="password" type="password" value={form.password} onChange={onChange} required />
            </div>

            <button className="btn-primary w-full" disabled={loading}>
              {loading ? 'Please wait…' : (mode === 'login' ? 'Sign in' : 'Create account')}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

