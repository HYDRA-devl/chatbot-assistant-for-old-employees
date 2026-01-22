package com.company.chatbot.repository;

import com.company.chatbot.entity.EmailEntity;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailEntity, Long> {
    Optional<EmailEntity> findByUserAndGmailMessageId(User user, String gmailMessageId);
    Optional<EmailEntity> findByIdAndUserId(Long id, Long userId);
    List<EmailEntity> findByUserOrderByReceivedAtDesc(User user);
}
