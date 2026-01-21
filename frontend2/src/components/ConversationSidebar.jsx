import React from 'react';
import { conversationAPI } from '../services/api';

export default function ConversationSidebar({
  user,
  conversations,
  activeConversationId,
  onSelectConversation,
  onNewConversation,
  onDeleteConversation
}) {
  const [searchQuery, setSearchQuery] = React.useState('');

  const filteredConversations = React.useMemo(() => {
    if (!searchQuery) return conversations;
    return conversations.filter(conv =>
      (conv.title?.toLowerCase() || '').includes(searchQuery.toLowerCase()) ||
      (conv.topic?.toLowerCase() || '').includes(searchQuery.toLowerCase())
    );
  }, [conversations, searchQuery]);

  const groupedConversations = React.useMemo(() => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const lastWeek = new Date(today);
    lastWeek.setDate(lastWeek.getDate() - 7);

    const groups = {
      today: [],
      yesterday: [],
      lastWeek: [],
      older: []
    };

    filteredConversations.forEach(conv => {
      const convDate = new Date(conv.startedAt);
      const convDay = new Date(convDate.getFullYear(), convDate.getMonth(), convDate.getDate());

      if (convDay.getTime() === today.getTime()) {
        groups.today.push(conv);
      } else if (convDay.getTime() === yesterday.getTime()) {
        groups.yesterday.push(conv);
      } else if (convDay >= lastWeek) {
        groups.lastWeek.push(conv);
      } else {
        groups.older.push(conv);
      }
    });

    return groups;
  }, [filteredConversations]);

  const handleDelete = async (e, conversationId) => {
    e.stopPropagation();
    if (!window.confirm('Delete this conversation?')) return;

    try {
      await conversationAPI.deleteConversation(conversationId);
      onDeleteConversation(conversationId);
    } catch (error) {
      console.error('Error deleting conversation:', error);
    }
  };

  const renderConversation = (conv) => {
    const isActive = conv.id === activeConversationId;
    const isOngoing = conv.status === 'ACTIVE';

    return (
      <div
        key={conv.id}
        onClick={() => onSelectConversation(conv.id)}
        className={`rounded-lg border px-4 py-3 cursor-pointer transition-colors ${
          isActive
            ? 'border-accent bg-mist'
            : 'border-border bg-white hover:bg-mist'
        }`}
      >
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <h3 className="text-base font-semibold text-ink truncate">
                {conv.title || conv.topic || 'New Conversation'}
              </h3>
              {isOngoing && (
                <span className="text-xs text-green-700 bg-green-100 px-2 py-0.5 rounded-full">
                  Active
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 mt-2">
              {conv.topic && (
                <span className="inline-block px-2 py-0.5 text-xs rounded bg-gray-200 text-gray-700">
                  {conv.topic}
                </span>
              )}
              <span className="text-sm text-gray-600">
                {conv.messageCount || 0} messages
              </span>
            </div>
          </div>
          <button
            onClick={(e) => handleDelete(e, conv.id)}
            className="text-sm font-semibold text-red-700 hover:text-red-800"
            aria-label="Delete conversation"
          >
            Delete
          </button>
        </div>
      </div>
    );
  };

  return (
    <div className="w-80 bg-white border-r border-border flex flex-col h-full">
      <div className="p-4 border-b border-border">
        <h2 className="text-lg font-semibold text-ink">Conversations</h2>
        <p className="text-sm text-gray-600 mt-1">
          {user?.fullName ? `Signed in as ${user.fullName}` : 'Start a new chat to begin.'}
        </p>
        <button onClick={onNewConversation} className="btn-primary w-full mt-4">
          New Conversation
        </button>
      </div>

      <div className="p-4 border-b border-border">
        <label className="label" htmlFor="conversation-search">Search conversations</label>
        <input
          id="conversation-search"
          type="text"
          placeholder="Search by title or topic"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="input"
        />
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-5">
        {groupedConversations.today.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-gray-600 uppercase mb-2">Today</h3>
            <div className="space-y-2">
              {groupedConversations.today.map(renderConversation)}
            </div>
          </div>
        )}

        {groupedConversations.yesterday.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-gray-600 uppercase mb-2">Yesterday</h3>
            <div className="space-y-2">
              {groupedConversations.yesterday.map(renderConversation)}
            </div>
          </div>
        )}

        {groupedConversations.lastWeek.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-gray-600 uppercase mb-2">Last 7 Days</h3>
            <div className="space-y-2">
              {groupedConversations.lastWeek.map(renderConversation)}
            </div>
          </div>
        )}

        {groupedConversations.older.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-gray-600 uppercase mb-2">Older</h3>
            <div className="space-y-2">
              {groupedConversations.older.map(renderConversation)}
            </div>
          </div>
        )}

        {conversations.length === 0 && (
          <div className="text-center py-8 text-gray-600 text-base">
            No conversations yet.
            <div className="mt-2">Select "New Conversation" to begin.</div>
          </div>
        )}
      </div>
    </div>
  );
}
