package com.company.chatbot.repository;

import com.company.chatbot.entity.CompletedTask;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedTaskRepository extends JpaRepository<CompletedTask, Long> {
    boolean existsByUserAndTaskId(User user, String taskId);
    List<CompletedTask> findByUser(User user);
}
