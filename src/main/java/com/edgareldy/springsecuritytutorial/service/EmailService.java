package com.edgareldy.springsecuritytutorial.service;

import com.edgareldy.springsecuritytutorial.entity.User;

/**
 * Contract for sending account-related emails. Called by feature/auth's
 * {@code AuthServiceImpl} after generating an activation or password reset
 * token; implementations run asynchronously so a slow SMTP call never
 * blocks the request thread that triggered it.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface EmailService {

    void sendActivationEmail(User user, String token);

    void sendPasswordResetEmail(User user, String token);
}
