package edgareldy.springsecuritytutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springsecuritytutorial.dto.role.RoleRequest;
import edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import edgareldy.springsecuritytutorial.entity.Permission;
import edgareldy.springsecuritytutorial.entity.Role;
import edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import edgareldy.springsecuritytutorial.mapper.RoleMapper;
import edgareldy.springsecuritytutorial.repository.PermissionRepository;
import edgareldy.springsecuritytutorial.repository.RoleRepository;
import edgareldy.springsecuritytutorial.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RoleServiceImpl}, with {@link RoleRepository},
 * {@link PermissionRepository}, {@link UserRepository}, and
 * {@link RoleMapper} mocked.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;
    private Permission permission;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        role = Role.builder().id(1L).roleName("ADMIN").permissions(new HashSet<>()).build();
        permission = Permission.builder().id(1L).resource("PRODUCT").action("WRITE").build();
        roleResponse = new RoleResponse(1L, "ADMIN", List.of());
    }

    @Test
    void findAllReturnsMappedList() {
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        assertThat(roleService.findAll()).containsExactly(roleResponse);
    }

    @Test
    void createSavesWhenRoleNameUnused() {
        RoleRequest request = new RoleRequest("ADMIN");
        when(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).thenReturn(false);
        when(roleMapper.toEntity(request)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        assertThat(roleService.create(request)).isEqualTo(roleResponse);
    }

    @Test
    void createThrowsWhenRoleNameAlreadyExists() {
        RoleRequest request = new RoleRequest("ADMIN");
        when(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> roleService.create(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(roleRepository, never()).save(any());
    }

    @Test
    void updateAppliesNewRoleNameWhenUnused() {
        RoleRequest request = new RoleRequest("MODERATOR");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByRoleNameIgnoreCase("MODERATOR")).thenReturn(false);
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(new RoleResponse(1L, "MODERATOR", List.of()));

        assertThat(roleService.update(1L, request).roleName()).isEqualTo("MODERATOR");
    }

    @Test
    void updateSkipsUniquenessCheckWhenNameUnchanged() {
        RoleRequest request = new RoleRequest("ADMIN");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        assertThat(roleService.update(1L, request)).isEqualTo(roleResponse);

        verify(roleRepository, never()).existsByRoleNameIgnoreCase(any());
    }

    @Test
    void updateThrowsWhenNewNameAlreadyUsedByAnotherRole() {
        RoleRequest request = new RoleRequest("MODERATOR");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByRoleNameIgnoreCase("MODERATOR")).thenReturn(true);

        assertThatThrownBy(() -> roleService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class);

        verify(roleRepository, never()).save(any());
    }

    @Test
    void deleteRemovesRoleWhenUnassigned() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.existsByRoles_Id(1L)).thenReturn(false);

        roleService.delete(1L);

        verify(roleRepository).delete(role);
    }

    @Test
    void deleteThrowsWhenAssignedToUser() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.existsByRoles_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> roleService.delete(1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(roleRepository, never()).delete(any());
    }

    @Test
    void assignPermissionAddsPermissionWhenNotAlreadyAssigned() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        roleService.assignPermission(1L, 1L);

        assertThat(role.getPermissions()).contains(permission);
    }

    @Test
    void assignPermissionThrowsWhenAlreadyAssigned() {
        role.getPermissions().add(permission);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        assertThatThrownBy(() -> roleService.assignPermission(1L, 1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(roleRepository, never()).save(any());
    }

    @Test
    void assignPermissionThrowsWhenPermissionMissing() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignPermission(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removePermissionRemovesWhenAssigned() {
        role.getPermissions().add(permission);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        roleService.removePermission(1L, 1L);

        assertThat(role.getPermissions()).doesNotContain(permission);
    }

    @Test
    void removePermissionThrowsWhenNotAssigned() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        assertThatThrownBy(() -> roleService.removePermission(1L, 1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(roleRepository, never()).save(any());
    }
}
