package edgareldy.springsecuritytutorial.security;

import edgareldy.springsecuritytutorial.entity.User;
import edgareldy.springsecuritytutorial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads a {@link User} by email (the JWT subject and Spring Security
 * username) for both the login {@code AuthenticationManager} and
 * {@code JwtAuthFilter}. {@link User} already implements
 * {@link UserDetails} directly, so it is returned as-is: no separate
 * adapter type is needed.
 * <p>
 * Created by edgar.muhamyangabo on 7/10/26
 * Author : edgar.muhamyangabo
 * Date : 7/10/26
 * Project : spring-security-tutorial
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email " + email));
    }
}
