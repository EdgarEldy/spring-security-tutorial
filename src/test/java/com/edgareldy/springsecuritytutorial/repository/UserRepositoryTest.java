package com.edgareldy.springsecuritytutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edgareldy.springsecuritytutorial.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * {@code @DataJpaTest} for {@link UserRepository}, backed by a real
 * PostgreSQL instance via Testcontainers.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RepositoryTestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder()
                .firstName("Ada").lastName("Lovelace").email("ada@example.com")
                .password("hashed").enabled(true).accountLocked(false).build());
        userRepository.save(User.builder()
                .firstName("Grace").lastName("Hopper").email("grace@example.com")
                .password("hashed").enabled(true).accountLocked(false).build());
    }

    @Test
    void _01_ShouldReturnMatchingUser_WhenFindingByEmail() {
        assertThat(userRepository.findByEmailIgnoreCase("ada@example.com"))
                .isPresent()
                .get()
                .extracting(User::getLastName)
                .isEqualTo("Lovelace");
    }

    @Test
    void _02_ShouldMatchEmail_WhenCaseDiffers() {
        assertThat(userRepository.findByEmailIgnoreCase("GRACE@EXAMPLE.COM"))
                .isPresent()
                .get()
                .extracting(User::getFirstName)
                .isEqualTo("Grace");
    }

    @Test
    void _03_ShouldReflectCurrentData_WhenCheckingEmailExists() {
        assertThat(userRepository.existsByEmailIgnoreCase("grace@example.com")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("unknown@example.com")).isFalse();
    }
}
