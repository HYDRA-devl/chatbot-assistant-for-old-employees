import React from 'react';
import { useNavigate } from 'react-router-dom';
import { conversationAPI, quizAPI } from '../services/api';
import ConversationSidebar from '../components/ConversationSidebar';

export default function ChatPage() {
  const navigate = useNavigate();
  const [conversations, setConversations] = React.useState([]);
  const [activeConversation, setActiveConversation] = React.useState(null);
  const [messages, setMessages] = React.useState([]);
  const [input, setInput] = React.useState('');
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState('');
  const [generatingQuiz, setGeneratingQuiz] = React.useState(false);
  const messagesEndRef = React.useRef(null);

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  React.useEffect(() => {
    if (user?.id) {
      loadConversations();
    }
  }, [user?.id]);

  React.useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadConversations = async () => {
    try {
      const convs = await conversationAPI.getUserConversations(user.id);
      setConversations(convs);

      const active = convs.find(c => c.status === 'ACTIVE');
      if (active) {
        selectConversation(active.id);
      }
    } catch (e) {
      console.error('Error loading conversations:', e);
    }
  };

  const selectConversation = async (conversationId) => {
    try {
      const conv = await conversationAPI.getConversation(conversationId);
      setActiveConversation(conv);

      const history = await conversationAPI.getConversationMessages(conversationId);
      setMessages(history || []);
    } catch (e) {
      console.error('Error loading conversation:', e);
      setError(e.message);
    }
  };

  const createNewConversation = async () => {
    try {
      const newConv = await conversationAPI.createConversation(user.id);
      setConversations(prev => [newConv, ...prev]);
      setActiveConversation(newConv);
      setMessages([]);
      setInput('');
      setError('');
    } catch (e) {
      console.error('Error creating conversation:', e);
      setError(e.message);
    }
  };

  const goToQuizForConversation = async (conversationId) => {
    try {
      const quiz = await quizAPI.getQuizByConversation(conversationId);
      navigate(`/quiz/${quiz.id}`);
      return true;
    } catch (e) {
      return false;
    }
  };

  const endConversation = async () => {
    if (!activeConversation) return;

    if (!window.confirm('Finish this conversation and create a quiz?')) {
      return;
    }

    setGeneratingQuiz(true);
    try {
      const endedConv = await conversationAPI.endConversation(activeConversation.id);

      setConversations(prev =>
        prev.map(c => c.id === endedConv.id ? endedConv : c)
      );

      setActiveConversation(endedConv);

      try {
        const quiz = await quizAPI.generateQuiz(endedConv.id);
        navigate(`/quiz/${quiz.id}`);
      } catch (e) {
        const found = await goToQuizForConversation(endedConv.id);
        if (!found) {
          throw e;
        }
      }
    } catch (e) {
      console.error('Error ending conversation or generating quiz: ', e);
      setError(e.message || 'Failed to generate quiz. Please try again.');
    } finally {
      setGeneratingQuiz(false);
    }
  };

  const deleteConversation = (conversationId) => {
    setConversations(prev => prev.filter(c => c.id !== conversationId));

    if (activeConversation?.id === conversationId) {
      setActiveConversation(null);
      setMessages([]);
    }
  };

  const sendMessage = async (e) => {
    e.preventDefault();
    if (!input.trim() || loading) return;

    let conv = activeConversation;
    if (!conv) {
      try {
        conv = await conversationAPI.createConversation(user.id);
        setActiveConversation(conv);
        setConversations(prev => [conv, ...prev]);
      } catch (e) {
        setError(e.message);
        return;
      }
    }

    setError('');
    const userMsg = input;
    setInput('');
    setLoading(true);

    const userEntry = {
      id: `u-${Date.now()}`,
      userMessage: userMsg,
      botResponse: '',
      createdAt: new Date().toISOString(),
      pointsEarned: 0,
      streaming: false,
    };
    setMessages((prev) => [...prev, userEntry]);

    const botEntryId = `a-${Date.now()}`;
    const emptyBotEntry = {
      id: botEntryId,
      userMessage: '',
      botResponse: '',
      createdAt: new Date().toISOString(),
      pointsEarned: 0,
      streaming: true,
    };
    setMessages((prev) => [...prev, emptyBotEntry]);

    try {
      const eventSource = new EventSource(
        `http://localhost:8081/api/chat/stream/conversation?userId=${user.id}&conversationId=${conv.id}&message=${encodeURIComponent(userMsg)}`
      );

      eventSource.addEventListener('token', (event) => {
        const data = JSON.parse(event.data);
        const token = data.token;
        setMessages((prev) =>
          prev.map((m) =>
            m.id === botEntryId
              ? { ...m, botResponse: m.botResponse + token }
              : m
          )
        );
      });

      eventSource.addEventListener('complete', (event) => {
        const data = JSON.parse(event.data);
        setMessages((prev) =>
          prev.map((m) =>
            m.id === botEntryId
              ? { ...m, pointsEarned: data.pointsEarned, streaming: false }
              : m
          )
        );

        const updatedUser = { ...user, totalPoints: user.totalPoints + data.pointsEarned };
        localStorage.setItem('currentUser', JSON.stringify(updatedUser));

        setActiveConversation(prev => ({
          ...prev,
          messageCount: (prev.messageCount || 0) + 1
        }));

        eventSource.close();
        setLoading(false);
      });

      eventSource.addEventListener('error', (event) => {
        console.error('EventSource error:', event);
        setMessages((prev) =>
          prev.map((m) =>
            m.id === botEntryId
              ? {
                  ...m,
                  botResponse: m.botResponse || 'Error: Could not complete request.',
                  streaming: false,
                }
              : m
          )
        );
        eventSource.close();
        setLoading(false);
      });
    } catch (e) {
      setError(e.message);
      setLoading(false);
    }
  };

  return (
    <div className="flex h-full bg-white border border-border rounded-xl overflow-hidden">
      <ConversationSidebar
        user={user}
        conversations={conversations}
        activeConversationId={activeConversation?.id}
        onSelectConversation={selectConversation}
        onNewConversation={createNewConversation}
        onDeleteConversation={deleteConversation}
      />

      <div className="flex-1 flex flex-col bg-white">
        {activeConversation ? (
          <>
            <div className="bg-white border-b border-border px-6 py-4">
              <div className="flex items-center justify-between gap-4">
                <div className="text-lg font-semibold text-ink">
                  {activeConversation.title || activeConversation.topic || 'New Conversation'}
                </div>
                <div className="flex items-center gap-3">
                  {activeConversation.status === 'ACTIVE' && (
                    <button
                      onClick={endConversation}
                      disabled={generatingQuiz}
                      className="btn-primary"
                    >
                      {generatingQuiz ? 'Creating quiz...' : 'Finish & create quiz'}
                    </button>
                  )}
                  {activeConversation.status === 'COMPLETED' && (
                    <button
                      onClick={async () => {
                        const found = await goToQuizForConversation(activeConversation.id);
                        if (!found) {
                          alert('Quiz not found yet. Try again in a moment.');
                        }
                      }}
                      className="btn-secondary"
                    >
                      View quiz
                    </button>
                  )}
                </div>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto p-6 space-y-5" aria-live="polite">
              {messages.length === 0 && (
                <div className="text-center py-8 text-gray-600 text-lg">
                  Type your first message below.
                </div>
              )}

              {messages.map((m) => (
                <div key={m.id} className="space-y-3">
                  {m.userMessage && (
                    <div className="flex justify-end">
                      <div className="max-w-2xl rounded-xl bg-gray-100 text-ink px-5 py-4 text-base border border-border">
                        {m.userMessage}
                      </div>
                    </div>
                  )}
                  {(m.botResponse || m.streaming) && (
                    <div className="flex justify-start">
                      <div className="max-w-2xl rounded-xl bg-white text-ink px-5 py-4 border border-border shadow-subtle">
                        {m.botResponse || (m.streaming ? '' : '')}
                        {m.streaming && (
                          <span className="inline-block w-0.5 h-4 ml-1 bg-ink animate-pulse"></span>
                        )}
                        {typeof m.pointsEarned === 'number' && m.pointsEarned > 0 && (
                          <div className="mt-3 text-sm text-green-700 font-semibold">
                            +{m.pointsEarned} points earned
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              ))}
              <div ref={messagesEndRef} />
            </div>

            <div className="bg-white border-t border-border p-6">
              {error && <div className="text-base text-red-700 mb-3">{error}</div>}
              <form onSubmit={sendMessage} className="flex flex-col gap-3">
                <label className="label" htmlFor="chat-message">Your message</label>
                <div className="flex flex-col lg:flex-row gap-3">
                  <input
                    id="chat-message"
                    className="input flex-1"
                    placeholder="Type your message..."
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    disabled={loading || activeConversation.status !== 'ACTIVE'}
                    aria-label="Message input"
                  />
                  <button
                    className="btn-primary"
                    disabled={loading || !input.trim() || activeConversation.status !== 'ACTIVE'}
                  >
                    {loading ? 'Sending...' : 'Send message'}
                  </button>
                </div>
              </form>
              {activeConversation.status !== 'ACTIVE' && (
                <div className="text-base text-amber-700 mt-3">
                  This conversation has ended. Start a new conversation to continue.
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center">
            <div className="text-center max-w-lg px-6">
              <h2 className="text-2xl font-semibold text-ink mb-3">
                Start a new conversation
              </h2>
              <p className="text-base text-gray-600 mb-6">
                Ask a question to begin.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
