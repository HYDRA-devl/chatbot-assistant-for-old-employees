package com.company.chatbot.repository;

import com.company.chatbot.entity.TaskEntity;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByUserOrderByExtractedAtDesc(User user);
    boolean existsByUserAndTitleAndSourceEmailId(User user, String title, String sourceEmailId);
    boolean existsByUserAndTitleAndDueAtAndSourceEmailId(User user, String title, LocalDateTime dueAt, String sourceEmailId);
}
