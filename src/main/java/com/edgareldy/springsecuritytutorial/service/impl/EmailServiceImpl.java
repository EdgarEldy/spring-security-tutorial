package com.edgareldy.springsecuritytutorial.service.impl;

import com.edgareldy.springsecuritytutorial.entity.User;
import com.edgareldy.springsecuritytutorial.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Default {@link EmailService} implementation using {@link JavaMailSender}.
 * Each method runs on the {@code emailTaskExecutor} pool declared in
 * {@code AsyncConfig}, never on the caller's thread.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String baseUrl;

    public EmailServiceImpl(JavaMailSender mailSender, @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendActivationEmail(User user, String token) {
        String link = baseUrl + "/api/auth/activate-account?token=" + token;
        send(user.getEmail(), "Activate your account",
                "Hello " + user.getFirstName() + ",\n\nActivate your account by visiting:\n" + link);
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(User user, String token) {
        String link = baseUrl + "/api/auth/reset-password?token=" + token;
        send(user.getEmail(), "Reset your password",
                "Hello " + user.getFirstName() + ",\n\nReset your password by visiting:\n" + link);
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        log.info("Sent email to {} with subject '{}'", to, subject);
    }
}
