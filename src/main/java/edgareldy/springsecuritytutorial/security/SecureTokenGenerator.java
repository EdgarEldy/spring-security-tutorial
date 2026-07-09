package edgareldy.springsecuritytutorial.security;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * Generates cryptographically secure random tokens shared by
 * {@code ActivationTokenService} and {@code PasswordResetTokenService}, per
 * this project's rule that activation/reset/JWT tokens are never generated
 * with a non-cryptographic random source.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Component
public class SecureTokenGenerator {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
