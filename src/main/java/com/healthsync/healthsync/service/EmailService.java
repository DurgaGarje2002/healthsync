package com.healthsync.healthsync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetEmail(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("HealthSync Password Reset");

        message.setText(
            "Hello,\n\n" +
            "Click the link below to reset your password:\n\n" +
            resetLink +
            "\n\nIf you didn't request this, ignore this email.\n\n" +
            "HealthSync Team"
        );

        mailSender.send(message);
    }
}