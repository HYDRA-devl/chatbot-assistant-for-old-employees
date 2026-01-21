package com.company.chatbot.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTasksService {

    private static final String APPLICATION_NAME = "Employee Chatbot";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final GoogleAuthService googleAuthService;

    private volatile Tasks tasksClient;
    private final Object tasksClientLock = new Object();

    public List<Task> listTasks(String taskListId, Integer maxResults) {
        try {
            Tasks client = getOrInitTasksClient();
            String resolvedTaskListId = taskListId != null ? taskListId : getPrimaryTaskListId(client);
            Tasks.TasksOperations.List request = client.tasks().list(resolvedTaskListId)
                .setShowCompleted(true)
                .setShowDeleted(false)
                .setShowHidden(false);
            if (maxResults != null) {
                request.setMaxResults(maxResults);
            }
            return request.execute().getItems();
        } catch (Exception e) {
            log.error("Failed to fetch Google Tasks", e);
            throw new RuntimeException("Failed to fetch Google Tasks");
        }
    }

    public List<TaskList> listTaskLists() {
        try {
            Tasks client = getOrInitTasksClient();
            TaskLists lists = client.tasklists().list().execute();
            return lists.getItems();
        } catch (Exception e) {
            log.error("Failed to fetch Google Task lists", e);
            throw new RuntimeException("Failed to fetch Google Task lists");
        }
    }

    public int countCompletedTasksSince(Instant since) {
        try {
            Tasks client = getOrInitTasksClient();
            TaskLists lists = client.tasklists().list().execute();
            if (lists.getItems() == null) {
                return 0;
            }

            int completedCount = 0;
            for (TaskList list : lists.getItems()) {
                Tasks.TasksOperations.List request = client.tasks().list(list.getId())
                    .setShowCompleted(true)
                    .setShowDeleted(false)
                    .setShowHidden(false);
                List<Task> tasks = request.execute().getItems();
                if (tasks == null) {
                    tasks = Collections.emptyList();
                }
                for (Task task : tasks) {
                    if (!"completed".equalsIgnoreCase(task.getStatus())) {
                        continue;
                    }
                    String completedAt = task.getCompleted();
                    if (completedAt == null) {
                        continue;
                    }
                    DateTime completedDateTime = new DateTime(completedAt);
                    if (completedDateTime.getValue() >= since.toEpochMilli()) {
                        completedCount += 1;
                    }
                }
            }
            return completedCount;
        } catch (Exception e) {
            log.error("Failed to count completed Google Tasks", e);
            throw new RuntimeException("Failed to count completed Google Tasks");
        }
    }

    public void markTaskCompleted(String taskListId, String taskId) {
        try {
            Tasks client = getOrInitTasksClient();
            Task update = new Task();
            update.setStatus("completed");
            update.setCompleted(Instant.now().toString());
            client.tasks().patch(taskListId, taskId, update).execute();
        } catch (Exception e) {
            log.error("Failed to mark Google Task completed", e);
            throw new RuntimeException("Failed to mark Google Task completed");
        }
    }

    private String getPrimaryTaskListId(Tasks client) throws Exception {
        TaskLists lists = client.tasklists().list().execute();
        if (lists.getItems() == null || lists.getItems().isEmpty()) {
            throw new RuntimeException("No task lists available");
        }
        return lists.getItems().get(0).getId();
    }

    private Tasks getOrInitTasksClient() {
        if (tasksClient != null) {
            return tasksClient;
        }
        synchronized (tasksClientLock) {
            if (tasksClient != null) {
                return tasksClient;
            }
            try {
                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                tasksClient = new Tasks.Builder(httpTransport, JSON_FACTORY, googleAuthService.getCredential())
                    .setApplicationName(APPLICATION_NAME)
                    .build();
                return tasksClient;
            } catch (Exception e) {
                log.error("Failed to initialize Google Tasks client", e);
                throw new RuntimeException("Failed to initialize Google Tasks client");
            }
        }
    }
}
