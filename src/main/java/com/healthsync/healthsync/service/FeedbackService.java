package com.healthsync.healthsync.service;

import com.healthsync.healthsync.entity.FeedbackEntity;
import com.healthsync.healthsync.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }
    @Transactional
    public FeedbackEntity save(FeedbackEntity feedback) {
        return feedbackRepository.save(feedback);
    }
    @Transactional(readOnly = true)
    public List<FeedbackEntity> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }
}