package com.edgareldy.springsecuritytutorial.service.impl;

import com.edgareldy.springsecuritytutorial.entity.BlacklistedToken;
import com.edgareldy.springsecuritytutorial.entity.User;
import com.edgareldy.springsecuritytutorial.repository.BlacklistedTokenRepository;
import com.edgareldy.springsecuritytutorial.service.BlacklistedTokenService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link BlacklistedTokenService} implementation backed by
 * {@link BlacklistedTokenRepository}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Service
@RequiredArgsConstructor
public class BlacklistedTokenServiceImpl implements BlacklistedTokenService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    @Transactional
    public void blacklist(User user, String rawJwt, String jti, Instant expiresAt) {
        Instant now = Instant.now();
        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .user(user)
                .token(rawJwt)
                .jti(jti)
                .blacklistedAt(now)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();
        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return blacklistedTokenRepository.existsByJti(jti);
    }
}
