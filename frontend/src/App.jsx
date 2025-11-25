import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ChatPage from './pages/ChatPage';
import Profile from './pages/Profile';
import Achievements from './pages/Achievements';
import Leaderboard from './pages/Leaderboard';

function App() {
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    // Check if user is stored in localStorage
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      setCurrentUser(JSON.parse(storedUser));
    }
  }, []);

  const handleLogin = (user) => {
    setCurrentUser(user);
    localStorage.setItem('currentUser', JSON.stringify(user));
  };

  const handleLogout = () => {
    setCurrentUser(null);
    localStorage.removeItem('currentUser');
  };

  return (
    <Router>
      <Routes>
        <Route 
          path="/login" 
          element={
            currentUser ? <Navigate to="/dashboard" /> : <Login onLogin={handleLogin} />
          } 
        />
        <Route 
          path="/dashboard" 
          element={
            currentUser ? (
              <Dashboard user={currentUser} onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route 
          path="/chat" 
          element={
            currentUser ? (
              <ChatPage user={currentUser} onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route 
          path="/profile" 
          element={
            currentUser ? (
              <Profile user={currentUser} onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route 
          path="/achievements" 
          element={
            currentUser ? (
              <Achievements user={currentUser} onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route 
          path="/leaderboard" 
          element={
            currentUser ? (
              <Leaderboard user={currentUser} onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;
