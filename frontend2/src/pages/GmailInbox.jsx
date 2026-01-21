import React from 'react';
import { gmailAPI } from '../services/api';

export default function GmailInbox() {
  const [emails, setEmails] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [connecting, setConnecting] = React.useState(false);
  const [error, setError] = React.useState('');
  const [status, setStatus] = React.useState('');

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  const handleConnect = async () => {
    setConnecting(true);
    setError('');
    setStatus('');
    try {
      const message = await gmailAPI.connect();
      setStatus(message || 'Gmail connected.');
    } catch (e) {
      setError(e.message);
    } finally {
      setConnecting(false);
    }
  };

  const handleFetch = async () => {
    if (!user?.id) {
      setError('No user found. Please log in again.');
      return;
    }
    setLoading(true);
    setError('');
    setStatus('');
    try {
      const data = await gmailAPI.fetchEmails(user.id, 10);
      setEmails(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="h-full overflow-y-auto p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Gmail Inbox</h2>
            <p className="text-gray-600 mt-1">Connect your Gmail and fetch recent emails</p>
          </div>
          <div className="flex items-center gap-2">
            <button
              className="btn-primary"
              onClick={handleConnect}
              disabled={connecting}
            >
              {connecting ? 'Connecting...' : 'Connect Gmail'}
            </button>
            <button
              className="inline-flex items-center justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-60"
              onClick={handleFetch}
              disabled={loading}
            >
              {loading ? 'Loading...' : 'Fetch Emails'}
            </button>
          </div>
        </div>

        {status && (
          <div className="text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg px-4 py-3">
            {status}
          </div>
        )}

        {error && (
          <div className="text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
            {error}
          </div>
        )}

        <section className="card">
          <div className="card-header">Recent Emails</div>
          <div className="card-body">
            {emails.length === 0 ? (
              <div className="text-gray-500">No emails fetched yet.</div>
            ) : (
              <div className="divide-y divide-gray-100">
                {emails.map((email) => {
                  const receivedAt = email.receivedAt
                    ? new Date(email.receivedAt).toLocaleString()
                    : 'Unknown date';
                  const snippet = email.body ? email.body.slice(0, 160) : '';
                  return (
                    <div key={email.id || email.gmailMessageId} className="py-4">
                      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1">
                        <div className="text-sm font-semibold text-gray-900">{email.subject || '(No subject)'}</div>
                        <div className="text-xs text-gray-500">{receivedAt}</div>
                      </div>
                      <div className="text-xs text-gray-600 mt-1">From: {email.sender || 'Unknown sender'}</div>
                      {snippet && (
                        <div className="text-sm text-gray-700 mt-2">{snippet}{email.body && email.body.length > 160 ? '...' : ''}</div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
