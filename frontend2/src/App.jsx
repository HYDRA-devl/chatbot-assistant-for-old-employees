import React from 'react';
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import ChatPage from './pages/ChatPage.jsx';
import Profile from './pages/Profile.jsx';
import Achievements from './pages/Achievements.jsx';
import Leaderboard from './pages/Leaderboard.jsx';
import QuizPage from './pages/QuizPage.jsx';
import TasksMeetings from './pages/TasksMeetings.jsx';
import GmailInbox from './pages/GmailInbox.jsx';

function useAuth() {
  const [user, setUser] = React.useState(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  });

  const updateUser = (u) => {
    if (u) {
      localStorage.setItem('currentUser', JSON.stringify(u));
    } else {
      localStorage.removeItem('currentUser');
    }
    setUser(u);
  };

  return { user, setUser: updateUser };
}

function Protected({ children }) {
  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  if (!user) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  const auth = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    auth.setUser(null);
    navigate('/login');
  };

  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login setUser={auth.setUser} />} />

      <Route
        path="/"
        element={
          <Protected>
            <Layout user={auth.user} onLogout={handleLogout} />
          </Protected>
        }
      >
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="chat" element={<ChatPage />} />
        <Route path="quiz/:quizId" element={<QuizPage />} />
        <Route path="profile" element={<Profile />} />
        <Route path="achievements" element={<Achievements />} />
        <Route path="leaderboard" element={<Leaderboard />} />
        <Route path="tasks" element={<TasksMeetings />} />
        <Route path="gmail" element={<GmailInbox />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

