import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';

const Layout = ({ user, onLogout, children }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  const isActive = (path) => location.pathname === path;

  const navItems = [
    { path: '/dashboard', icon: '📊', label: 'Dashboard' },
    { path: '/chat', icon: '💬', label: 'Chat' },
    { path: '/achievements', icon: '🏆', label: 'Achievements' },
    { path: '/leaderboard', icon: '📈', label: 'Leaderboard' },
  ];

  return (
    <div className="flex h-screen bg-gray-50 overflow-hidden">
      {/* Sidebar */}
      <aside
        className={`${
          isSidebarOpen ? 'w-64' : 'w-20'
        } bg-white border-r border-gray-200 flex flex-col transition-all duration-300 ease-in-out`}
      >
        {/* Logo/Brand */}
        <div className="h-16 flex items-center px-4 border-b border-gray-200">
          {isSidebarOpen ? (
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-white font-bold text-sm">
                AI
              </div>
              <span className="font-semibold text-gray-800">Employee Learning</span>
            </div>
          ) : (
            <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-white font-bold text-sm mx-auto">
              AI
            </div>
          )}
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center ${
                isSidebarOpen ? 'px-3' : 'px-0 justify-center'
              } py-3 rounded-lg transition-all duration-200 ${
                isActive(item.path)
                  ? 'bg-blue-50 text-blue-600'
                  : 'text-gray-700 hover:bg-gray-100'
              }`}
            >
              <span className="text-xl">{item.icon}</span>
              {isSidebarOpen && (
                <span className="ml-3 font-medium text-sm">{item.label}</span>
              )}
            </Link>
          ))}
        </nav>

        {/* User Profile & Settings */}
        <div className="border-t border-gray-200 p-4">
          <div
            className={`flex items-center ${
              isSidebarOpen ? 'space-x-3' : 'justify-center'
            } mb-3`}
          >
            <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center text-white font-bold">
              {user.fullName.charAt(0)}
            </div>
            {isSidebarOpen && (
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {user.fullName}
                </p>
                <p className="text-xs text-gray-500 truncate">
                  Level {user.level} • {user.totalPoints} pts
                </p>
              </div>
            )}
          </div>

          {isSidebarOpen && (
            <div className="space-y-2">
              <Link
                to="/profile"
                className="flex items-center px-3 py-2 text-sm text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <span className="mr-2">⚙️</span>
                Settings
              </Link>
              <button
                onClick={onLogout}
                className="w-full flex items-center px-3 py-2 text-sm text-red-600 rounded-lg hover:bg-red-50 transition-colors"
              >
                <span className="mr-2">🚪</span>
                Logout
              </button>
            </div>
          )}
        </div>

        {/* Toggle Sidebar Button */}
        <button
          onClick={() => setIsSidebarOpen(!isSidebarOpen)}
          className="absolute -right-3 top-20 w-6 h-6 bg-white border border-gray-200 rounded-full flex items-center justify-center text-gray-600 hover:bg-gray-50 transition-colors shadow-sm"
        >
          <span className="text-xs">{isSidebarOpen ? '◀' : '▶'}</span>
        </button>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <div className="max-w-7xl mx-auto p-6">{children}</div>
      </main>
    </div>
  );
};

export default Layout;
