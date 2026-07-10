package edgareldy.springsecuritytutorial.repository;

import edgareldy.springsecuritytutorial.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Overrides the base paginated find to eagerly fetch each user's roles,
     * avoiding one extra select per row when the controller maps a page of
     * users to {@code UserResponse} (which reads {@code roles}).
     */
    @EntityGraph(attributePaths = "roles")
    @Override
    Page<User> findAll(Pageable pageable);

    /**
     * Eagerly fetches roles and their permissions, so a {@link User}
     * returned by this method has a fully usable
     * {@link User#getAuthorities()} even after the transaction/session
     * that loaded it has closed (e.g. once {@code UserDetailsServiceImpl}
     * hands it back to {@code JwtAuthFilter}).
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRoles_Id(Long roleId);
}
