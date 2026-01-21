package com.company.chatbot.repository;

import com.company.chatbot.entity.ExtractionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractionJobRepository extends JpaRepository<ExtractionJob, Long> {
}
