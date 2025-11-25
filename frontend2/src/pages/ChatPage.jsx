import React from 'react';
import { chatAPI } from '../services/api';

export default function ChatPage() {
  const [messages, setMessages] = React.useState([]);
  const [input, setInput] = React.useState('');
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState('');

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  React.useEffect(() => {
    let mounted = true;
    async function fetchHistory() {
      try {
        const history = await chatAPI.getRecentChatHistory(user.id, 20);
        if (mounted) setMessages(history);
      } catch (e) {
        setError(e.message);
      }
    }
    if (user?.id) fetchHistory();
    return () => { mounted = false; };
  }, [user?.id]);

  const sendMessage = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;
    setError('');
    const userEntry = {
      id: `u-${Date.now()}`,
      userMessage: input,
      botResponse: '',
      createdAt: new Date().toISOString(),
      pointsEarned: 0,
    };
    setMessages((prev) => [...prev, userEntry]);
    setInput('');
    setLoading(true);
    try {
      const res = await chatAPI.sendMessage({ userId: user.id, message: userEntry.userMessage });
      const aiEntry = {
        id: `a-${Date.now()}`,
        userMessage: userEntry.userMessage,
        botResponse: res.response,
        createdAt: new Date().toISOString(),
        pointsEarned: res.pointsEarned ?? 0,
      };
      setMessages((prev) => [...prev.slice(0, -1), aiEntry]);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid grid-rows-[1fr_auto] h-[calc(100vh-140px)] card">
      <div className="card-body overflow-y-auto space-y-4">
        {messages.length === 0 && (
          <div className="text-sm text-gray-500">Start a conversation by typing below.</div>
        )}
        {messages.map((m) => (
          <div key={m.id} className="space-y-2">
            <div className="flex justify-end">
              <div className="max-w-prose rounded-lg bg-gray-900 text-white px-3 py-2">{m.userMessage}</div>
            </div>
            {m.botResponse && (
              <div className="flex justify-start">
                <div className="max-w-prose rounded-lg bg-gray-100 text-gray-900 px-3 py-2 border border-gray-200">
                  {m.botResponse}
                  {typeof m.pointsEarned === 'number' && (
                    <div className="mt-2 text-xs text-gray-500">Points earned: {m.pointsEarned}</div>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
      <div className="border-t border-gray-200 p-3">
        {error && <div className="text-sm text-red-600 mb-2">{error}</div>}
        <form onSubmit={sendMessage} className="flex gap-2">
          <input
            className="input"
            placeholder="Type your message…"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            disabled={loading}
          />
          <button className="btn-primary" disabled={loading}>{loading ? 'Sending…' : 'Send'}</button>
        </form>
        <div className="text-xs text-gray-500 mt-2">Responses may take 30–180s depending on model.</div>
      </div>
    </div>
  );
}

