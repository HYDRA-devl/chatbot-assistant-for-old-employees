import React from 'react';
import { googleTasksAPI, googleCalendarAPI, activityAPI } from '../services/api';

export default function TasksMeetings() {
  const [tasks, setTasks] = React.useState([]);
  const [taskLists, setTaskLists] = React.useState([]);
  const [selectedListId, setSelectedListId] = React.useState('');
  const [meetings, setMeetings] = React.useState([]);
  const [calendars, setCalendars] = React.useState([]);
  const [selectedCalendarId, setSelectedCalendarId] = React.useState('');
  const [completedTaskIds, setCompletedTaskIds] = React.useState(new Set());
  const [completedMeetingIds, setCompletedMeetingIds] = React.useState(new Set());
  const [statusMessage, setStatusMessage] = React.useState('');
  const [error, setError] = React.useState('');
  const [loading, setLoading] = React.useState(false);

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  const formatEventDate = (value) => {
    if (!value || typeof value !== 'string') return null;
    if (!value.includes('T')) return value;
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) return value;
    return parsed.toLocaleString();
  };

  const loadCompletionStatus = React.useCallback(async () => {
    if (!user?.id) return;
    try {
      const data = await activityAPI.getCompleted(user.id);
      setCompletedTaskIds(new Set(data.completedTaskIds || []));
      setCompletedMeetingIds(new Set(data.completedMeetingIds || []));
    } catch (e) {
      console.error('Failed to fetch completion status', e);
    }
  }, [user?.id]);

  const loadLists = React.useCallback(async () => {
    if (!user?.id) return;
    setLoading(true);
    setError('');
    try {
      const [listsData, tasksData, calendarsData, eventsData] = await Promise.all([
        googleTasksAPI.getTaskLists(),
        googleTasksAPI.getTasks(selectedListId || undefined, 20),
        googleCalendarAPI.getCalendars(),
        googleCalendarAPI.getEvents(selectedCalendarId || undefined, 20)
      ]);
      const listItems = Array.isArray(listsData) ? listsData : [];
      const calendarItems = Array.isArray(calendarsData) ? calendarsData : [];
      setTaskLists(listItems);
      setTasks(Array.isArray(tasksData) ? tasksData : []);
      setCalendars(calendarItems);
      setMeetings(Array.isArray(eventsData) ? eventsData : []);
      if (!selectedListId && listItems.length > 0) {
        setSelectedListId(listItems[0].id);
      }
      if (!selectedCalendarId && calendarItems.length > 0) {
        setSelectedCalendarId(calendarItems[0].id);
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [user?.id, selectedListId, selectedCalendarId]);

  React.useEffect(() => {
    loadLists();
    loadCompletionStatus();
  }, [loadLists, loadCompletionStatus]);

  const fetchCalendarEvents = async (calendarId) => {
    setError('');
    try {
      const data = await googleCalendarAPI.getEvents(calendarId, 50);
      setMeetings(Array.isArray(data) ? data : []);
      if (calendarId) {
        setSelectedCalendarId(calendarId);
      }
    } catch (e) {
      setError(e.message);
    }
  };

  const fetchGoogleTasks = async (listId) => {
    setError('');
    try {
      const data = await googleTasksAPI.getTasks(listId, 50);
      setTasks(Array.isArray(data) ? data : []);
      if (listId) {
        setSelectedListId(listId);
      }
    } catch (e) {
      setError(e.message);
    }
  };

  const markTaskCompleted = async (task) => {
    if (!user?.id || !task?.id) return;
    setStatusMessage('');
    setError('');
    try {
      const result = await activityAPI.completeTask({
        userId: user.id,
        taskId: task.id,
        taskListId: selectedListId || null,
        title: task.title || ''
      });
      setCompletedTaskIds((prev) => new Set([...prev, task.id]));
      setStatusMessage(`Task marked completed. +${result.pointsEarned} points.`);
      await fetchGoogleTasks(selectedListId || undefined);
    } catch (e) {
      setError(e.message);
    }
  };

  const markMeetingCompleted = async (meeting) => {
    if (!user?.id || !meeting?.id) return;
    setStatusMessage('');
    setError('');
    try {
      const result = await activityAPI.completeMeeting({
        userId: user.id,
        eventId: meeting.id,
        summary: meeting.summary || ''
      });
      setCompletedMeetingIds((prev) => new Set([...prev, meeting.id]));
      setStatusMessage(`Meeting marked completed. +${result.pointsEarned} points.`);
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div className="h-full overflow-y-auto p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Tasks and Meetings</h2>
            <p className="text-gray-600 mt-1">Update task and meeting status to earn points</p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <button className="btn-primary" onClick={() => fetchGoogleTasks(selectedListId || undefined)}>
              Fetch Google Tasks
            </button>
            <button
              className="btn-secondary"
              onClick={() => fetchCalendarEvents(selectedCalendarId || undefined)}
            >
              Fetch Calendar Meetings
            </button>
            <button
              className="btn-secondary"
              onClick={loadLists}
              disabled={loading}
            >
              {loading ? 'Refreshing...' : 'Refresh'}
            </button>
          </div>
        </div>

        {statusMessage && (
          <div className="text-base text-ink bg-mist border border-border rounded-lg px-4 py-3">
            {statusMessage}
          </div>
        )}

        {error && (
          <div className="text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
            {error}
          </div>
        )}

        <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="card">
            <div className="card-header flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <span>Tasks</span>
              {taskLists.length > 0 && (
                <select
                  className="border border-gray-300 rounded-md text-sm px-3 py-2 max-w-full sm:max-w-[16rem] w-full sm:w-auto truncate"
                  value={selectedListId}
                  onChange={(e) => fetchGoogleTasks(e.target.value)}
                >
                  {taskLists.map((list) => (
                    <option key={list.id} value={list.id}>{list.title}</option>
                  ))}
                </select>
              )}
            </div>
            <div className="card-body">
              {tasks.length === 0 ? (
                <div className="text-gray-500">No tasks found yet.</div>
              ) : (
                <div className="space-y-4">
                  {tasks.map((task) => {
                    const isCompleted = completedTaskIds.has(task.id) || task.status === 'completed';
                    return (
                      <div key={task.id} className="border border-gray-200 rounded-lg p-3">
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <div className="font-semibold text-gray-900">{task.title}</div>
                            <div className="text-xs text-gray-500 mt-1">Status: {task.status || 'needsAction'}</div>
                          </div>
                          <button
                            className="btn-secondary"
                            onClick={() => markTaskCompleted(task)}
                            disabled={isCompleted}
                          >
                            {isCompleted ? 'Completed' : 'Mark completed'}
                          </button>
                        </div>
                        {task.notes && (
                          <div className="text-sm text-gray-700 mt-2">{task.notes}</div>
                        )}
                        {task.due && (
                          <div className="text-xs text-gray-500 mt-2">Due: {new Date(task.due).toLocaleString()}</div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-header flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <span>Meetings</span>
              {calendars.length > 0 && (
                <select
                  className="border border-gray-300 rounded-md text-sm px-3 py-2 max-w-full sm:max-w-[16rem] w-full sm:w-auto truncate"
                  value={selectedCalendarId}
                  onChange={(e) => fetchCalendarEvents(e.target.value)}
                >
                  {calendars.map((cal) => (
                    <option key={cal.id} value={cal.id}>{cal.summary || cal.id}</option>
                  ))}
                </select>
              )}
            </div>
            <div className="card-body">
              {meetings.length === 0 ? (
                <div className="text-gray-500">No meetings found yet.</div>
              ) : (
                <div className="space-y-4">
                  {meetings.map((meeting) => {
                    const startLabel = formatEventDate(meeting.start);
                    const endLabel = formatEventDate(meeting.end);
                    const meetingLink = meeting.meetingLink || meeting.htmlLink;
                    const isCompleted = completedMeetingIds.has(meeting.id);

                    return (
                      <div key={meeting.id} className="border border-gray-200 rounded-lg p-3">
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <div className="font-semibold text-gray-900">{meeting.summary}</div>
                            {startLabel && (
                              <div className="text-xs text-gray-500 mt-1">Start: {startLabel}</div>
                            )}
                            {endLabel && (
                              <div className="text-xs text-gray-500">End: {endLabel}</div>
                            )}
                          </div>
                          <button
                            className="btn-secondary"
                            onClick={() => markMeetingCompleted(meeting)}
                            disabled={isCompleted}
                          >
                            {isCompleted ? 'Completed' : 'Mark completed'}
                          </button>
                        </div>
                        {meeting.location && (
                          <div className="text-sm text-gray-700 mt-2">Location: {meeting.location}</div>
                        )}
                        {meetingLink && (
                          <div className="text-sm text-gray-700 mt-2">
                            <a
                              href={meetingLink}
                              target="_blank"
                              rel="noreferrer"
                              className="text-blue-600 hover:text-blue-700 underline"
                            >
                              {meeting.meetingLink ? 'Join meeting' : 'Open event'}
                            </a>
                          </div>
                        )}
                        {meeting.attendees && meeting.attendees.length > 0 && (
                          <div className="text-sm text-gray-700">Attendees: {meeting.attendees.join(", ")}</div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
