package com.edgareldy.springsecuritytutorial.service;

import com.edgareldy.springsecuritytutorial.entity.User;

/**
 * Contract for account activation token generation and validation. No
 * controller exposes this directly: feature/auth's {@code AuthServiceImpl}
 * is the intended caller (registration generates a token, the activation
 * endpoint validates it).
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface ActivationTokenService {

    String generate(User user);

    User validate(String token);
}
