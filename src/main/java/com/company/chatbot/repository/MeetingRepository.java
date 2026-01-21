package com.company.chatbot.repository;

import com.company.chatbot.entity.MeetingEntity;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {
    List<MeetingEntity> findByUserOrderByExtractedAtDesc(User user);
    boolean existsByUserAndTitleAndStartAtAndSourceEmailId(User user, String title, LocalDateTime startAt, String sourceEmailId);
    boolean existsByUserAndTitleAndSourceEmailId(User user, String title, String sourceEmailId);
}
