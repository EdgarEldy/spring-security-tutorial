package edgareldy.springsecuritytutorial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edgareldy.springsecuritytutorial.entity.Permission;
import edgareldy.springsecuritytutorial.entity.Role;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * {@code @DataJpaTest} for {@link RoleRepository}, backed by a real
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
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Permission permission;

    @BeforeEach
    void setUp() {
        permission = permissionRepository.save(Permission.builder().resource("PRODUCT").action("WRITE").build());
        roleRepository.save(Role.builder()
                .roleName("ADMIN")
                .permissions(new HashSet<>(Set.of(permission)))
                .build());
    }

    @Test
    void findByRoleNameIgnoreCaseReturnsMatchingRole() {
        assertThat(roleRepository.findByRoleNameIgnoreCase("admin"))
                .isPresent()
                .get()
                .extracting(Role::getRoleName)
                .isEqualTo("ADMIN");
    }

    @Test
    void existsByRoleNameIgnoreCaseReflectsCurrentData() {
        assertThat(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).isTrue();
        assertThat(roleRepository.existsByRoleNameIgnoreCase("unknown")).isFalse();
    }

    @Test
    void existsByPermissionsIdReflectsAssignment() {
        assertThat(roleRepository.existsByPermissions_Id(permission.getId())).isTrue();
    }

    @Test
    void existsByPermissionsIdReturnsFalseForUnassignedPermission() {
        Permission other = permissionRepository.save(Permission.builder().resource("USER").action("DELETE").build());

        assertThat(roleRepository.existsByPermissions_Id(other.getId())).isFalse();
    }
}
