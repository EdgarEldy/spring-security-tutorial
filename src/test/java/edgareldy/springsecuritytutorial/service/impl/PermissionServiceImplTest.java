package edgareldy.springsecuritytutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springsecuritytutorial.dto.permission.PermissionRequest;
import edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import edgareldy.springsecuritytutorial.entity.Permission;
import edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import edgareldy.springsecuritytutorial.mapper.PermissionMapper;
import edgareldy.springsecuritytutorial.repository.PermissionRepository;
import edgareldy.springsecuritytutorial.repository.RoleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PermissionServiceImpl}, with
 * {@link PermissionRepository}, {@link RoleRepository}, and
 * {@link PermissionMapper} mocked.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission permission;
    private PermissionResponse permissionResponse;

    @BeforeEach
    void setUp() {
        permission = Permission.builder().id(1L).resource("PRODUCT").action("WRITE").build();
        permissionResponse = new PermissionResponse(1L, "PRODUCT", "WRITE");
    }

    @Test
    void findAllReturnsMappedList() {
        when(permissionRepository.findAll()).thenReturn(List.of(permission));
        when(permissionMapper.toResponse(permission)).thenReturn(permissionResponse);

        assertThat(permissionService.findAll()).containsExactly(permissionResponse);
    }

    @Test
    void createSavesWhenResourceActionUnused() {
        PermissionRequest request = new PermissionRequest("PRODUCT", "WRITE");
        when(permissionRepository.existsByResourceIgnoreCaseAndActionIgnoreCase("PRODUCT", "WRITE")).thenReturn(false);
        when(permissionMapper.toEntity(request)).thenReturn(permission);
        when(permissionRepository.save(permission)).thenReturn(permission);
        when(permissionMapper.toResponse(permission)).thenReturn(permissionResponse);

        assertThat(permissionService.create(request)).isEqualTo(permissionResponse);
    }

    @Test
    void createThrowsWhenResourceActionAlreadyExists() {
        PermissionRequest request = new PermissionRequest("PRODUCT", "WRITE");
        when(permissionRepository.existsByResourceIgnoreCaseAndActionIgnoreCase("PRODUCT", "WRITE")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.create(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(permissionRepository, never()).save(any());
    }

    @Test
    void deleteRemovesPermissionWhenUnassigned() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(roleRepository.existsByPermissions_Id(1L)).thenReturn(false);

        permissionService.delete(1L);

        verify(permissionRepository).delete(permission);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteThrowsWhenAssignedToRole() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(roleRepository.existsByPermissions_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> permissionService.delete(1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(permissionRepository, never()).delete(any());
    }
}
