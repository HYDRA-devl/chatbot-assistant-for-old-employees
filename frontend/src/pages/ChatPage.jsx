import { useState, useEffect, useRef } from 'react';
import Layout from '../components/Layout';
import { chatAPI } from '../services/api';

const ChatPage = ({ user, onLogout }) => {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const textareaRef = useRef(null);

  useEffect(() => {
    loadChatHistory();
  }, [user.id]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    adjustTextareaHeight();
  }, [inputMessage]);

  const adjustTextareaHeight = () => {
    const textarea = textareaRef.current;
    if (textarea) {
      textarea.style.height = 'auto';
      textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px';
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadChatHistory = async () => {
    try {
      const response = await chatAPI.getChatHistory(user.id);
      const history = response.data.reverse();
      const formattedMessages = history.flatMap(chat => [
        { type: 'user', content: chat.userMessage, timestamp: chat.createdAt },
        { type: 'bot', content: chat.botResponse, timestamp: chat.createdAt, points: chat.pointsEarned }
      ]);
      setMessages(formattedMessages);
    } catch (error) {
      console.error('Error loading chat history:', error);
    }
  };

  const handleSend = async () => {
    if (!inputMessage.trim() || loading) return;

    const userMessage = inputMessage;
    setInputMessage('');
    setLoading(true);

    setMessages(prev => [...prev, { 
      type: 'user', 
      content: userMessage, 
      timestamp: new Date().toISOString() 
    }]);

    try {
      const response = await chatAPI.sendMessage({
        userId: user.id,
        message: userMessage
      });

      setMessages(prev => [...prev, {
        type: 'bot',
        content: response.data.response,
        timestamp: new Date().toISOString(),
        points: response.data.pointsEarned
      }]);

      const updatedUser = { ...user, totalPoints: user.totalPoints + response.data.pointsEarned };
      localStorage.setItem('currentUser', JSON.stringify(updatedUser));
    } catch (error) {
      console.error('Error sending message:', error);
      setMessages(prev => [...prev, {
        type: 'bot',
        content: 'Sorry, I encountered an error. Please try again.',
        timestamp: new Date().toISOString()
      }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <Layout user={user} onLogout={onLogout}>
      <div className="flex flex-col h-[calc(100vh-48px)] max-w-4xl mx-auto">
        {/* Messages Container */}
        <div className="flex-1 overflow-y-auto py-8 px-4">
          {messages.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-center space-y-6">
              <div className="w-20 h-20 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg">
                <span className="text-4xl">🤖</span>
              </div>
              <div className="space-y-2">
                <h2 className="text-2xl font-semibold text-gray-800">
                  How can I help you today?
                </h2>
                <p className="text-gray-600 max-w-md">
                  Ask me anything about programming, technology, frameworks, or any tech topic.
                </p>
              </div>
              
              {/* Suggested Prompts */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 max-w-2xl w-full mt-8">
                {[
                  'Explain React hooks',
                  'What is microservices architecture?',
                  'How does Git branching work?',
                  'Best practices for REST APIs'
                ].map((prompt, idx) => (
                  <button
                    key={idx}
                    onClick={() => setInputMessage(prompt)}
                    className="p-4 text-left bg-white border border-gray-200 rounded-xl hover:bg-gray-50 hover:border-gray-300 transition-all duration-200 group"
                  >
                    <div className="flex items-start space-x-3">
                      <span className="text-xl">💡</span>
                      <span className="text-sm text-gray-700 group-hover:text-gray-900">
                        {prompt}
                      </span>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          ) : (
            <div className="space-y-6">
              {messages.map((message, index) => (
                <div
                  key={index}
                  className={`flex ${message.type === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`flex ${message.type === 'user' ? 'flex-row-reverse' : 'flex-row'} items-start space-x-3 max-w-3xl`}>
                    {/* Avatar */}
                    <div className={`flex-shrink-0 w-8 h-8 rounded-lg flex items-center justify-center ${
                      message.type === 'user'
                        ? 'bg-gradient-to-br from-purple-500 to-pink-500 ml-3'
                        : 'bg-gradient-to-br from-blue-500 to-purple-600 mr-3'
                    }`}>
                      <span className="text-white text-sm font-semibold">
                        {message.type === 'user' ? user.fullName.charAt(0) : '🤖'}
                      </span>
                    </div>

                    {/* Message Content */}
                    <div className="flex-1">
                      <div className={`${
                        message.type === 'user'
                          ? 'bg-blue-600 text-white'
                          : 'bg-white border border-gray-200'
                      } rounded-2xl px-5 py-3 shadow-sm`}>
                        <p className={`text-sm leading-relaxed whitespace-pre-wrap ${
                          message.type === 'user' ? 'text-white' : 'text-gray-800'
                        }`}>
                          {message.content}
                        </p>
                        {message.points && (
                          <div className="mt-2 pt-2 border-t border-green-100 flex items-center space-x-1 text-green-600">
                            <span className="text-xs font-medium">+{message.points} points</span>
                            <span className="text-xs">🎯</span>
                          </div>
                        )}
                      </div>
                      <p className="text-xs text-gray-500 mt-1 px-1">
                        {new Date(message.timestamp).toLocaleTimeString([], { 
                          hour: '2-digit', 
                          minute: '2-digit' 
                        })}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
              
              {loading && (
                <div className="flex justify-start">
                  <div className="flex items-start space-x-3 max-w-3xl">
                    <div className="flex-shrink-0 w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center mr-3">
                      <span className="text-white text-sm font-semibold">🤖</span>
                    </div>
                    <div className="bg-white border border-gray-200 rounded-2xl px-5 py-4 shadow-sm">
                      <div className="flex space-x-2">
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* Input Area */}
        <div className="border-t border-gray-200 bg-white p-4">
          <div className="max-w-4xl mx-auto">
            <div className="relative bg-gray-50 border border-gray-200 rounded-2xl focus-within:border-blue-500 focus-within:ring-2 focus-within:ring-blue-100 transition-all">
              <textarea
                ref={textareaRef}
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Message AI assistant..."
                className="w-full bg-transparent border-none focus:outline-none focus:ring-0 resize-none p-4 pr-12 text-gray-800 placeholder-gray-400 max-h-[200px]"
                rows="1"
                disabled={loading}
              />
              <button
                onClick={handleSend}
                disabled={loading || !inputMessage.trim()}
                className="absolute right-3 bottom-3 w-8 h-8 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white rounded-lg flex items-center justify-center transition-colors shadow-sm"
              >
                {loading ? (
                  <span className="text-xs">⏳</span>
                ) : (
                  <span className="text-sm">↑</span>
                )}
              </button>
            </div>
            <p className="text-xs text-gray-500 mt-2 text-center">
              Press Enter to send, Shift+Enter for new line
            </p>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default ChatPage;
