package com.edgareldy.springsecuritytutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edgareldy.springsecuritytutorial.entity.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * {@code @DataJpaTest} for {@link PermissionRepository}, backed by a real
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
class PermissionRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        permissionRepository.save(Permission.builder().resource("PRODUCT").action("WRITE").build());
    }

    @Test
    void existsByResourceIgnoreCaseAndActionIgnoreCaseMatchesRegardlessOfCase() {
        assertThat(permissionRepository.existsByResourceIgnoreCaseAndActionIgnoreCase("product", "write")).isTrue();
    }

    @Test
    void existsByResourceIgnoreCaseAndActionIgnoreCaseReturnsFalseWhenNoMatch() {
        assertThat(permissionRepository.existsByResourceIgnoreCaseAndActionIgnoreCase("USER", "DELETE")).isFalse();
    }
}
