package edgareldy.springsecuritytutorial.service;

import edgareldy.springsecuritytutorial.entity.User;

/**
 * Contract for password reset token generation and single-use validation.
 * No controller exposes this directly: feature/auth's
 * {@code AuthServiceImpl} orchestrates the forgot-password/reset-password
 * flow around it.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface PasswordResetTokenService {

    String generate(User user);

    User validateAndConsume(String token);
}
