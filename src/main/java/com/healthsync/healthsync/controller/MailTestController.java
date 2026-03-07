package com.healthsync.healthsync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailTestController {
	
	 @Autowired
	    private JavaMailSender mailSender;

	    @GetMapping("/test-mail")
	    public String sendMail() {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo("durgagarje14@gmail.com");
	        message.setSubject("HealthSync Test");
	        message.setText("Email working successfully!");

	        mailSender.send(message);
	        return "Mail Sent!";
	    }

}
