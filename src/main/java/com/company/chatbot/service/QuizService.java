package com.company.chatbot.service;

import com.company.chatbot.entity.*;
import com.company.chatbot.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GamificationService gamificationService;
    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper;

    // Track conversations currently being processed to prevent duplicates
    private final Set<Long> processingConversations = ConcurrentHashMap.newKeySet();

    /**
     * Generate a quiz from a completed conversation
     */
    public Quiz generateQuizFromConversation(Long conversationId) {
        if (processingConversations.contains(conversationId)) {
            log.warn("Quiz generation already in progress for conversation {}", conversationId);
            throw new RuntimeException("Quiz generation already in progress for this conversation");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (quizRepository.findByConversation(conversation).isPresent()) {
            log.warn("Quiz already exists for conversation {}", conversationId);
            throw new RuntimeException("Quiz already exists for this conversation");
        }

        processingConversations.add(conversationId);
        log.info("Started quiz generation for conversation {}", conversationId);

        try {
            // Get all messages from conversation
            List<ChatMessage> messages = chatMessageRepository.findByConversationOrderByCreatedAtAsc(conversation);

            if (messages == null || messages.isEmpty()) {
                throw new RuntimeException("Cannot generate quiz from empty conversation");
            }

            // Build conversation context
            String conversationText = messages.stream()
                .map(msg -> "User: " + msg.getUserMessage())
                .reduce("", (a, b) -> a + "\n" + b);

            // Generate quiz questions
            List<QuizQuestionData> questionDataList = generateQuizQuestions(conversationText, conversation.getTopic());

            // Create quiz entity
            Quiz quiz = new Quiz();
            quiz.setConversation(conversation);
            quiz.setUser(conversation.getUser());
            quiz.setTopic(conversation.getTopic());
            quiz = quizRepository.save(quiz);

            // Save questions
            int questionOrder = 1;
            for (QuizQuestionData qd : questionDataList) {
                QuizQuestion q = new QuizQuestion();
                q.setQuiz(quiz);
                q.setQuestionText(qd.question);
                q.setOptionA(qd.optionA);
                q.setOptionB(qd.optionB);
                q.setOptionC(qd.optionC);
                q.setOptionD(qd.optionD);
                q.setCorrectAnswer(qd.correctAnswer);
                q.setExplanation(qd.explanation);
                q.setQuestionOrder(questionOrder++);
                quizQuestionRepository.save(q);
            }

            log.info("Generated quiz {} with {} questions for conversation {}",
                quiz.getId(), questionDataList.size(), conversationId);

            return quiz;
        } finally {
            processingConversations.remove(conversationId);
            log.debug("Removed conversation {} from processing set", conversationId);
        }
    }

    public Optional<Quiz> getQuizForConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return quizRepository.findByConversation(conversation);
    }

    public Quiz getQuiz(Long quizId) {
        return quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    public List<QuizQuestion> getQuizQuestions(Long quizId) {
        Quiz quiz = getQuiz(quizId);
        return quizQuestionRepository.findByQuizOrderByQuestionOrderAsc(quiz);
    }

    public QuizAttempt submitQuizAttempt(Long quizId, Long userId, Map<Long, String> answers) {
        Quiz quiz = getQuiz(quizId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        int correctCount = 0;
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizOrderByQuestionOrderAsc(quiz);

        for (QuizQuestion q : questions) {
            String answer = answers.get(q.getId());
            if (q.getCorrectAnswer().equals(answer)) {
                correctCount++;
            }
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setScore(correctCount);
        attempt.setTotalQuestions(questions.size());
        attempt = quizAttemptRepository.save(attempt);

        // Calculate points earned (10 points per correct answer)
        int pointsEarned = correctCount * 10;
        attempt.setPointsEarned(pointsEarned);
        quizAttemptRepository.save(attempt);

        // Award points to user
        user.setTotalPoints(user.getTotalPoints() + pointsEarned);
        userRepository.save(user);
        gamificationService.checkAndAwardAchievements(user);

        log.info("User {} scored {}/{} on quiz {}, earned {} points",
            userId, correctCount, questions.size(), quizId, pointsEarned);

        return attempt;
    }

    public QuizAttempt getQuizAttempt(Long attemptId) {
        return quizAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));
    }

    public List<QuizAttempt> getUserQuizAttempts(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return quizAttemptRepository.findByUserOrderByCompletedAtDesc(user);
    }

        // DTO for generating quiz questions
    private static class QuizQuestionData {
        String question;
        String optionA;
        String optionB;
        String optionC;
        String optionD;
        String correctAnswer;
        String explanation;
    }

    /**
     * Generate quiz questions using AI
     */
    private List<QuizQuestionData> generateQuizQuestions(String conversationText, String topic) {
        String topicName = (topic != null && !topic.isBlank()) ? topic : "this topic";
        String context = trimConversation(conversationText);

        if (context.isBlank()) {
            return defaultQuestions(topicName);
        }

        try {
            String prompt = buildQuizPrompt(context, topicName);
            String response = ollamaService.generateResponse(prompt, "", 500, false);
            List<QuizQuestionData> parsed = parseQuizResponse(response);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        } catch (Exception e) {
            log.warn("Quiz generation via LLM failed, using fallback", e);
        }

        return defaultQuestions(topicName);
    }

    private String trimConversation(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.trim();
        if (cleaned.length() <= 800) {
            return cleaned;
        }
        return cleaned.substring(cleaned.length() - 800);
    }

    private String buildQuizPrompt(String context, String topicName) {
        return "You are a training quiz generator. Create 3 multiple-choice questions based ONLY on the context below. "
            + "Return JSON only in this exact format: {\"questions\":[{\"question\":...,\"optionA\":...,\"optionB\":...,\"optionC\":...,\"optionD\":...,\"correctAnswer\":\"A\",\"explanation\":...}]} "
            + "Use topic: " + topicName + ". Context: " + context + ". "
            + "Use only user questions. Do not include line breaks inside JSON strings.";
    }

    private List<QuizQuestionData> parseQuizResponse(String response) {
        String json = extractJson(response);
        if (json != null) {
            json = json.replace("\r", " ").replace("\n", " ").trim();
        }
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode questionsNode = root.has("questions") ? root.get("questions") : root;
            List<LlmQuizQuestion> llmQuestions = objectMapper.convertValue(questionsNode, new TypeReference<>() {});

            List<QuizQuestionData> results = new ArrayList<>();
            for (LlmQuizQuestion q : llmQuestions) {
                if (q == null || q.question == null || q.optionA == null || q.optionB == null || q.optionC == null || q.optionD == null) {
                    continue;
                }
                String answer = normalizeAnswer(q.correctAnswer);
                if (answer == null) {
                    continue;
                }
                QuizQuestionData data = new QuizQuestionData();
                data.question = q.question;
                data.optionA = q.optionA;
                data.optionB = q.optionB;
                data.optionC = q.optionC;
                data.optionD = q.optionD;
                data.correctAnswer = answer;
                data.explanation = q.explanation != null ? q.explanation : "";
                results.add(data);
            }

            return results;
        } catch (Exception e) {
            log.warn("Failed to parse quiz JSON", e);
            return List.of();
        }
    }

    private String extractJson(String response) {
        if (response == null) {
            return null;
        }
        int first = response.indexOf('{');
        int last = response.lastIndexOf('}');
        if (first < 0 || last <= first) {
            return null;
        }
        return response.substring(first, last + 1);
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return null;
        }
        String trimmed = answer.trim().toUpperCase(Locale.ROOT);
        return switch (trimmed) {
            case "A", "B", "C", "D" -> trimmed;
            default -> null;
        };
    }

    private List<QuizQuestionData> defaultQuestions(String topicName) {
        List<QuizQuestionData> questions = new ArrayList<>();

        QuizQuestionData q1 = new QuizQuestionData();
        q1.question = "What was the main topic of this conversation?";
        q1.optionA = topicName;
        q1.optionB = "An unrelated topic";
        q1.optionC = "General company policies";
        q1.optionD = "Personal preferences";
        q1.correctAnswer = "A";
        q1.explanation = "We discussed " + topicName + " in this conversation.";
        questions.add(q1);

        return questions;
    }

    private static class LlmQuizQuestion {
        public String question;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;
        public String correctAnswer;
        public String explanation;
    }
}
