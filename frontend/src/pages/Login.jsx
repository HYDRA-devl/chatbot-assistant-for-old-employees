import { useState } from 'react';
import { userAPI } from '../services/api';

const Login = ({ onLogin }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    fullName: '',
    department: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLogin) {
        const response = await userAPI.getUserByUsername(formData.username);
        const user = response.data;
        
        if (user.password === formData.password) {
          await userAPI.recordLogin(user.id);
          onLogin(user);
        } else {
          setError('Invalid username or password');
        }
      } else {
        const response = await userAPI.createUser(formData);
        onLogin(response.data);
      }
    } catch (err) {
      setError(isLogin ? 'User not found' : 'Registration failed. Username or email might already exist.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left Side - Branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-blue-600 via-purple-600 to-pink-500 p-12 flex-col justify-between">
        <div>
          <div className="flex items-center space-x-3 mb-8">
            <div className="w-12 h-12 bg-white bg-opacity-20 backdrop-blur-lg rounded-xl flex items-center justify-center">
              <span className="text-2xl">🤖</span>
            </div>
            <span className="text-2xl font-bold text-white">Employee Learning</span>
          </div>
          
          <div className="space-y-6 text-white">
            <h1 className="text-5xl font-bold leading-tight">
              Stay ahead with<br />AI-powered learning
            </h1>
            <p className="text-xl text-blue-100">
              Enhance your tech skills with personalized AI assistance and gamified learning experiences.
            </p>
          </div>
        </div>

        <div className="space-y-6">
          <div className="flex items-start space-x-4">
            <div className="w-10 h-10 bg-white bg-opacity-20 backdrop-blur-lg rounded-lg flex items-center justify-center flex-shrink-0">
              <span className="text-xl">💡</span>
            </div>
            <div>
              <h3 className="font-semibold text-white mb-1">AI-Powered Assistance</h3>
              <p className="text-blue-100 text-sm">Get instant answers to your technology questions</p>
            </div>
          </div>
          <div className="flex items-start space-x-4">
            <div className="w-10 h-10 bg-white bg-opacity-20 backdrop-blur-lg rounded-lg flex items-center justify-center flex-shrink-0">
              <span className="text-xl">🏆</span>
            </div>
            <div>
              <h3 className="font-semibold text-white mb-1">Gamified Learning</h3>
              <p className="text-blue-100 text-sm">Earn points, unlock achievements, and level up</p>
            </div>
          </div>
          <div className="flex items-start space-x-4">
            <div className="w-10 h-10 bg-white bg-opacity-20 backdrop-blur-lg rounded-lg flex items-center justify-center flex-shrink-0">
              <span className="text-xl">📈</span>
            </div>
            <div>
              <h3 className="font-semibold text-white mb-1">Track Progress</h3>
              <p className="text-blue-100 text-sm">Monitor your learning journey and compete with colleagues</p>
            </div>
          </div>
        </div>
      </div>

      {/* Right Side - Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-gray-50">
        <div className="w-full max-w-md">
          {/* Mobile Logo */}
          <div className="lg:hidden flex items-center space-x-3 mb-8">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-purple-600 rounded-xl flex items-center justify-center">
              <span className="text-xl">🤖</span>
            </div>
            <span className="text-xl font-bold text-gray-800">Employee Learning</span>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-gray-900 mb-2">
                {isLogin ? 'Welcome back' : 'Get started'}
              </h2>
              <p className="text-gray-600">
                {isLogin ? 'Sign in to continue your learning journey' : 'Create your account to start learning'}
              </p>
            </div>

            {/* Toggle Tabs */}
            <div className="flex bg-gray-100 rounded-lg p-1 mb-6">
              <button
                className={`flex-1 py-2.5 rounded-md font-medium text-sm transition-all ${
                  isLogin
                    ? 'bg-white text-gray-900 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
                onClick={() => setIsLogin(true)}
              >
                Sign In
              </button>
              <button
                className={`flex-1 py-2.5 rounded-md font-medium text-sm transition-all ${
                  !isLogin
                    ? 'bg-white text-gray-900 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
                onClick={() => setIsLogin(false)}
              >
                Sign Up
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Username
                </label>
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  placeholder="Enter your username"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  Password
                </label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  placeholder="Enter your password"
                  required
                />
              </div>

              {!isLogin && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">
                      Email
                    </label>
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                      placeholder="your.email@company.com"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">
                      Full Name
                    </label>
                    <input
                      type="text"
                      name="fullName"
                      value={formData.fullName}
                      onChange={handleChange}
                      className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                      placeholder="John Doe"
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
                      className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                      placeholder="Engineering"
                      required
                    />
                  </div>
                </>
              )}

              {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-medium py-3 px-4 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-sm hover:shadow-md"
              >
                {loading ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin h-5 w-5 mr-2" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    Please wait...
                  </span>
                ) : (
                  isLogin ? 'Sign In' : 'Create Account'
                )}
              </button>
            </form>
          </div>

          <p className="text-center text-sm text-gray-600 mt-6">
            By continuing, you agree to our Terms of Service and Privacy Policy
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
