package com.company.chatbot.repository;

import com.company.chatbot.entity.CompletedMeeting;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedMeetingRepository extends JpaRepository<CompletedMeeting, Long> {
    boolean existsByUserAndEventId(User user, String eventId);
    List<CompletedMeeting> findByUser(User user);
}
