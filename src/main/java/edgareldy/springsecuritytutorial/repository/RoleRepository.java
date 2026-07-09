package edgareldy.springsecuritytutorial.repository;

import edgareldy.springsecuritytutorial.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Role}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Overrides the base find-all to eagerly fetch each role's permissions,
     * avoiding one extra select per row when the controller maps every role
     * to a {@code RoleResponse} (which reads {@code permissions}).
     */
    @EntityGraph(attributePaths = "permissions")
    @Override
    List<Role> findAll();

    Optional<Role> findByRoleNameIgnoreCase(String roleName);

    boolean existsByRoleNameIgnoreCase(String roleName);

    boolean existsByPermissions_Id(Long permissionId);
}
