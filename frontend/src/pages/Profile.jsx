import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { userAPI, gamificationAPI } from '../services/api';

const Profile = ({ user, onLogout }) => {
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({
    fullName: user.fullName,
    email: user.email,
    department: user.department,
    password: ''
  });
  const [stats, setStats] = useState(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    fetchStats();
  }, [user.id]);

  const fetchStats = async () => {
    try {
      const response = await gamificationAPI.getUserStats(user.id);
      setStats(response.data);
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const updateData = {
        ...user,
        fullName: formData.fullName,
        email: formData.email,
        department: formData.department
      };
      
      if (formData.password) {
        updateData.password = formData.password;
      }

      await userAPI.updateUser(user.id, updateData);
      
      const updatedUser = { ...user, ...updateData };
      localStorage.setItem('currentUser', JSON.stringify(updatedUser));
      
      setMessage('Profile updated successfully!');
      setEditMode(false);
      
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      console.error('Error updating profile:', error);
      setMessage('Error updating profile. Please try again.');
    }
  };

  if (!stats) {
    return (
      <Layout user={user} onLogout={onLogout}>
        <div className="flex items-center justify-center h-full">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p className="text-gray-600">Loading profile...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout user={user} onLogout={onLogout}>
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Profile Header */}
        <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
          <div className="h-32 bg-gradient-to-r from-blue-500 via-purple-600 to-pink-500"></div>
          <div className="px-6 pb-6">
            <div className="flex items-end justify-between -mt-16 mb-4">
              <div className="flex items-end space-x-4">
                <div className="w-24 h-24 bg-gradient-to-br from-purple-500 to-pink-500 rounded-xl flex items-center justify-center text-white text-3xl font-bold border-4 border-white shadow-lg">
                  {user.fullName.charAt(0)}
                </div>
                <div className="pb-2">
                  <h1 className="text-2xl font-bold text-gray-900">{user.fullName}</h1>
                  <p className="text-gray-600">@{user.username}</p>
                </div>
              </div>
              {!editMode && (
                <button
                  onClick={() => setEditMode(true)}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium text-sm"
                >
                  Edit Profile
                </button>
              )}
            </div>

            <div className="flex flex-wrap gap-4 pt-4 border-t border-gray-200">
              <div className="flex items-center space-x-2 bg-blue-50 px-4 py-2 rounded-lg">
                <span className="text-lg">🎯</span>
                <div>
                  <p className="text-xs text-gray-600">Level</p>
                  <p className="font-semibold text-gray-900">{stats.level}</p>
                </div>
              </div>
              <div className="flex items-center space-x-2 bg-green-50 px-4 py-2 rounded-lg">
                <span className="text-lg">⭐</span>
                <div>
                  <p className="text-xs text-gray-600">Points</p>
                  <p className="font-semibold text-gray-900">{stats.totalPoints.toLocaleString()}</p>
                </div>
              </div>
              <div className="flex items-center space-x-2 bg-orange-50 px-4 py-2 rounded-lg">
                <span className="text-lg">🔥</span>
                <div>
                  <p className="text-xs text-gray-600">Streak</p>
                  <p className="font-semibold text-gray-900">{stats.dailyStreak} days</p>
                </div>
              </div>
              <div className="flex items-center space-x-2 bg-purple-50 px-4 py-2 rounded-lg">
                <span className="text-lg">💬</span>
                <div>
                  <p className="text-xs text-gray-600">Messages</p>
                  <p className="font-semibold text-gray-900">{stats.messagesSent}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Profile Information */}
        <div className="bg-white border border-gray-200 rounded-xl p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Profile Information</h2>

          {message && (
            <div className={`mb-4 p-4 rounded-lg ${
              message.includes('success') 
                ? 'bg-green-50 border border-green-200 text-green-700' 
                : 'bg-red-50 border border-red-200 text-red-700'
            }`}>
              {message}
            </div>
          )}

          {editMode ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Full Name
                </label>
                <input
                  type="text"
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Email
                </label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Department
                </label>
                <input
                  type="text"
                  name="department"
                  value={formData.department}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  New Password (leave blank to keep current)
                </label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="••••••••"
                />
              </div>

              <div className="flex space-x-3 pt-2">
                <button
                  type="submit"
                  className="px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
                >
                  Save Changes
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setEditMode(false);
                    setFormData({
                      fullName: user.fullName,
                      email: user.email,
                      department: user.department,
                      password: ''
                    });
                  }}
                  className="px-6 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium"
                >
                  Cancel
                </button>
              </div>
            </form>
          ) : (
            <div className="space-y-4">
              <div className="flex items-center py-3 border-b border-gray-100">
                <span className="text-sm text-gray-600 w-32">Username</span>
                <span className="text-sm text-gray-900 font-medium">{user.username}</span>
              </div>
              <div className="flex items-center py-3 border-b border-gray-100">
                <span className="text-sm text-gray-600 w-32">Full Name</span>
                <span className="text-sm text-gray-900 font-medium">{user.fullName}</span>
              </div>
              <div className="flex items-center py-3 border-b border-gray-100">
                <span className="text-sm text-gray-600 w-32">Email</span>
                <span className="text-sm text-gray-900 font-medium">{user.email}</span>
              </div>
              <div className="flex items-center py-3 border-b border-gray-100">
                <span className="text-sm text-gray-600 w-32">Department</span>
                <span className="text-sm text-gray-900 font-medium">{user.department}</span>
              </div>
              <div className="flex items-center py-3">
                <span className="text-sm text-gray-600 w-32">Member Since</span>
                <span className="text-sm text-gray-900 font-medium">
                  {new Date(user.createdAt).toLocaleDateString('en-US', { 
                    year: 'numeric', 
                    month: 'long', 
                    day: 'numeric' 
                  })}
                </span>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default Profile;
