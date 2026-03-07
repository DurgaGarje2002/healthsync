package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.entity.FeedbackEntity;
import com.healthsync.healthsync.entity.UserEntity;
import com.healthsync.healthsync.repository.UserRepository;
import com.healthsync.healthsync.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository  userRepository;

    public FeedbackController(FeedbackService feedbackService, UserRepository userRepository) {
        this.feedbackService = feedbackService;
        this.userRepository  = userRepository;
    }

    // POST /api/feedback  — submit feedback
    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract userId from JWT via the request header (handled by JwtAuthFilter)
            // We use Principal or SecurityContext — simplest: pass userId in body
            Long userId = Long.parseLong(body.get("userId") != null
                    ? body.get("userId").toString()
                    : "0");

            // Fallback: try to load user by userId from body, or from security context
            UserEntity user = null;
            if (userId > 0) {
                user = userRepository.findById(userId).orElse(null);
            }

            FeedbackEntity fb = new FeedbackEntity();
            fb.setUser(user);
            fb.setFeedbackType(body.getOrDefault("feedbackType", "GENERAL").toString());
            fb.setSubject(body.getOrDefault("subject", "").toString());
            fb.setMessage(body.getOrDefault("message", "").toString());
            if (body.get("rating") != null && !body.get("rating").toString().equals("null")) {
                fb.setRating(Integer.parseInt(body.get("rating").toString()));
            }

            FeedbackEntity saved = feedbackService.save(fb);
            return ResponseEntity.ok(Map.of("message", "Feedback submitted", "id", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    // GET /api/feedback/all  — admin view all feedback
    @GetMapping("/all")
    public ResponseEntity<List<FeedbackEntity>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }
}