import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';

export default function Layout({ user, onLogout }) {
  return (
    <div className="h-screen w-full grid grid-cols-[260px_1fr]">
      <aside className="relative border-r border-gray-200 bg-white">
        <div className="px-4 py-5 border-b border-gray-200">
          <div className="text-base font-semibold text-gray-900">Learning Platform</div>
          <div className="text-sm text-gray-500">Enterprise Dashboard</div>
        </div>
        <nav className="p-3 space-y-1">
          <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>Dashboard</NavLink>
          <NavLink to="/chat" className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>Chat</NavLink>
          <NavLink to="/achievements" className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>Achievements</NavLink>
          <NavLink to="/leaderboard" className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>Leaderboard</NavLink>
          <NavLink to="/profile" className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>Profile</NavLink>
        </nav>
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200">
          <div className="text-sm text-gray-600 mb-2">{user?.fullName || user?.username}</div>
          <button className="w-full btn-primary" onClick={onLogout}>Logout</button>
        </div>
      </aside>
      <main className="overflow-y-auto bg-gray-50">
        <header className="sticky top-0 z-10 bg-white border-b border-gray-200">
          <div className="container-pro py-4">
            <h1 className="text-xl font-semibold text-gray-900">Employee Learning Platform</h1>
          </div>
        </header>
        <div className="container-pro py-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
