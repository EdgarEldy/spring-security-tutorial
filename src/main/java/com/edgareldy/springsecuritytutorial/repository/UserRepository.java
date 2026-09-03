package com.edgareldy.springsecuritytutorial.repository;

import com.edgareldy.springsecuritytutorial.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link User}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Overrides the base single-entity find to eagerly fetch the user's
     * roles and each role's permissions, needed by
     * {@code UserServiceImpl.toDetailResponse} (used by {@code findById},
     * {@code assignRole}, and {@code removeRole}) so those lazy
     * associations are still accessible once the read completes, without
     * relying on {@code spring.jpa.open-in-view} to keep the session open.
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Override
    Optional<User> findById(Long id);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRoles_Id(Long roleId);
}
