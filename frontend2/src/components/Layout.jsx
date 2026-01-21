import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';

export default function Layout({ user, onLogout }) {
  const location = useLocation();
  const [isSidebarOpen, setIsSidebarOpen] = React.useState(true);

  React.useEffect(() => {
    if (location.pathname.startsWith('/chat')) {
      setIsSidebarOpen(false);
    }
  }, [location.pathname]);

  return (
    <div className="min-h-screen bg-sand text-ink flex flex-col">
      <header className="bg-white border-b border-border px-6 py-4 flex items-center">
        <div className="flex-1 flex items-center">
          <button
            className="btn-secondary"
            onClick={() => setIsSidebarOpen((prev) => !prev)}
            aria-expanded={isSidebarOpen}
            aria-controls="primary-navigation"
          >
            {isSidebarOpen ? 'Hide menu' : 'Show menu'}
          </button>
        </div>

        <div className="flex-1 flex items-center justify-center">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-accent text-white flex items-center justify-center font-semibold">
              EL
            </div>
            <div className="text-center">
              <h1 className="text-2xl font-semibold text-ink">Employee Learning Platform</h1>
              <p className="text-base text-gray-600">Simple tools for learning, email, and tasks</p>
            </div>
          </div>
        </div>

        <div className="flex-1 flex items-center justify-end gap-3">
          <div className="text-right">
            <div className="text-base font-semibold text-ink">{user?.fullName || user?.username}</div>
            <div className="text-sm text-gray-600">Signed in</div>
          </div>
          <NavLink to="/profile" className="btn-secondary">Profile</NavLink>
          <button className="btn-secondary" onClick={onLogout}>Log out</button>
        </div>
      </header>

      <div className="flex flex-1 min-h-0 overflow-hidden">
        {isSidebarOpen ? (
          <aside className="w-72 bg-white border-r border-border p-4 flex flex-col gap-4 flex-none overflow-hidden">
            <nav id="primary-navigation" aria-label="Primary" className="space-y-2">
              <NavLink
                to="/dashboard"
                className={({ isActive }) => `${isActive ? 'nav-link nav-link-active' : 'nav-link'}`}
              >
                Dashboard
              </NavLink>
              <NavLink
                to="/chat"
                className={({ isActive }) => `${isActive ? 'nav-link nav-link-active' : 'nav-link'}`}
              >
                AI Chat
              </NavLink>
              <NavLink
                to="/gmail"
                className={({ isActive }) => `${isActive ? 'nav-link nav-link-active' : 'nav-link'}`}
              >
                Email (Gmail)
              </NavLink>
              <NavLink
                to="/tasks"
                className={({ isActive }) => `${isActive ? 'nav-link nav-link-active' : 'nav-link'}`}
              >
                Tasks and Meetings
              </NavLink>
              <NavLink
                to="/achievements"
                className={({ isActive }) => `${isActive ? 'nav-link nav-link-active' : 'nav-link'}`}
              >
                Learning Progress
              </NavLink>
              <NavLink
                to="/leaderboard"
                className={({ isActive }) => `${isActive ? 'nav-link nav-link-active' : 'nav-link'}`}
              >
                Leaderboard
              </NavLink>
            </nav>

            <div className="mt-2 p-4 border border-border rounded-xl bg-mist">
              <h2 className="text-lg font-semibold text-ink">Need help now?</h2>
              <p className="text-sm text-gray-600 mt-1">
                Open the AI chat for step-by-step guidance.
              </p>
              <NavLink to="/chat" className="btn-primary w-full mt-3">
                Open AI Chat
              </NavLink>
            </div>
          </aside>
        ) : null}

        <main id="main-content" className="flex-1 min-h-0 overflow-y-auto p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
