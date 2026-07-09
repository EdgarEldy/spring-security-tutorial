package edgareldy.springsecuritytutorial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Declares the {@link PasswordEncoder} bean on its own, ahead of the full
 * {@code SecurityConfig} (filter chain, JWT filter, authentication rules)
 * that feature/auth will add. {@code UserServiceImpl.createUser} needs it to
 * hash passwords as soon as accounts can be created, well before login
 * exists.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
