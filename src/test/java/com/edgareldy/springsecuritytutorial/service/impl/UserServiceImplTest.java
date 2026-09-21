package com.edgareldy.springsecuritytutorial.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edgareldy.springsecuritytutorial.dto.common.PageResponse;
import com.edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import com.edgareldy.springsecuritytutorial.dto.user.UpdateProfileRequest;
import com.edgareldy.springsecuritytutorial.dto.user.UserRequest;
import com.edgareldy.springsecuritytutorial.dto.user.UserResponse;
import com.edgareldy.springsecuritytutorial.entity.Role;
import com.edgareldy.springsecuritytutorial.entity.User;
import com.edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import com.edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import com.edgareldy.springsecuritytutorial.mapper.RoleMapper;
import com.edgareldy.springsecuritytutorial.mapper.UserMapper;
import com.edgareldy.springsecuritytutorial.repository.RoleRepository;
import com.edgareldy.springsecuritytutorial.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for {@link UserServiceImpl}, with {@link UserRepository},
 * {@link UserMapper}, and {@link PasswordEncoder} mocked.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).firstName("Ada").lastName("Lovelace")
                .email("ada@example.com").password("hashed").enabled(true).accountLocked(false).build();
        userResponse = new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, false, List.of());
    }

    @Test
    void _01_ShouldReturnPageResponse_WhenListingUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        PageResponse<UserResponse> result = userService.findAll(pageable);

        assertThat(result.content()).containsExactly(userResponse);
    }

    @Test
    void _02_ShouldThrowNotFound_WhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void _03_ShouldReturnDetailResponseWithRoles_WhenUserFound() {
        Role role = Role.builder().id(1L).roleName("ADMIN").build();
        user.getRoles().add(role);
        RoleResponse roleResponse = new RoleResponse(1L, "ADMIN", List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);
        when(userMapper.toDetailResponse(user, List.of(roleResponse))).thenReturn(userResponse);

        UserResponse result = userService.findById(1L);

        assertThat(result).isEqualTo(userResponse);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void _04_ShouldReturnResponse_WhenEmailExists() {
        when(userRepository.findByEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        assertThat(userService.findByEmail("ada@example.com")).isEqualTo(userResponse);
    }

    @Test
    void _05_ShouldThrowNotFound_WhenEmailMissing() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void _06_ShouldHashPasswordAndDisableAccount_WhenCreatingUser() {
        UserRequest request = new UserRequest("Ada", "Lovelace", "ada@example.com", "s3cret!!");
        User mappedEntity = User.builder().firstName("Ada").lastName("Lovelace").email("ada@example.com").build();
        when(userRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(mappedEntity);
        when(passwordEncoder.encode("s3cret!!")).thenReturn("hashed");
        when(userRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(userMapper.toResponse(mappedEntity)).thenReturn(userResponse);

        assertThat(userService.createUser(request)).isEqualTo(userResponse);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getPassword()).isEqualTo("hashed");
        assertThat(savedUser.getValue().isEnabled()).isFalse();
        assertThat(savedUser.getValue().isAccountLocked()).isFalse();
    }

    @Test
    void _07_ShouldThrowBusinessRule_WhenEmailAlreadyUsed() {
        UserRequest request = new UserRequest("Ada", "Lovelace", "ada@example.com", "s3cret!!");
        when(userRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void _08_ShouldApplyRequest_WhenUpdatingProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest("Ada", "Byron");
        UserResponse updated = new UserResponse(1L, "Ada", "Byron", "ada@example.com", true, false, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(updated);

        assertThat(userService.updateProfile(1L, request)).isEqualTo(updated);

        verify(userMapper).updateProfile(request, user);
    }

    @Test
    void _09_ShouldThrowNotFound_WhenUpdatingMissingUser() {
        UpdateProfileRequest request = new UpdateProfileRequest("Ada", "Byron");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void _10_ShouldRemoveUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void _11_ShouldThrowNotFound_WhenDeletingMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void _12_ShouldLockAccount_WhenTargetDiffersFromCaller() {
        UserResponse locked = new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, true, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(locked);

        assertThat(userService.lock(1L, 2L)).isEqualTo(locked);

        assertThat(user.isAccountLocked()).isTrue();
    }

    @Test
    void _13_ShouldThrowBusinessRule_WhenAdminLocksThemselves() {
        assertThatThrownBy(() -> userService.lock(1L, 1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void _14_ShouldThrowNotFound_WhenLockingMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.lock(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void _15_ShouldUnlockAccount_WhenUserIsLocked() {
        user.setAccountLocked(true);
        UserResponse unlocked = new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, false, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(unlocked);

        assertThat(userService.unlock(1L)).isEqualTo(unlocked);

        assertThat(user.isAccountLocked()).isFalse();
    }

    @Test
    void _16_ShouldThrowNotFound_WhenUnlockingMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.unlock(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void _17_ShouldAddRole_WhenRoleNotAlreadyAssigned() {
        Role role = Role.builder().id(1L).roleName("ADMIN").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(roleMapper.toResponse(role)).thenReturn(new RoleResponse(1L, "ADMIN", List.of()));
        when(userMapper.toDetailResponse(eq(user), any())).thenReturn(userResponse);

        userService.assignRole(1L, 1L);

        assertThat(user.getRoles()).contains(role);
    }

    @Test
    void _18_ShouldThrowBusinessRule_WhenRoleAlreadyAssigned() {
        Role role = Role.builder().id(1L).roleName("ADMIN").build();
        user.getRoles().add(role);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> userService.assignRole(1L, 1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void _19_ShouldThrowNotFound_WhenAssigningMissingRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRole(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void _20_ShouldThrowNotFound_WhenAssigningRoleToMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRole(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roleRepository, never()).findById(any());
    }

    @Test
    void _21_ShouldRemoveRole_WhenRoleIsAssigned() {
        Role role = Role.builder().id(1L).roleName("ADMIN").build();
        user.getRoles().add(role);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDetailResponse(eq(user), any())).thenReturn(userResponse);

        userService.removeRole(1L, 1L);

        assertThat(user.getRoles()).doesNotContain(role);
    }

    @Test
    void _22_ShouldThrowBusinessRule_WhenRoleNotAssigned() {
        Role role = Role.builder().id(1L).roleName("ADMIN").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> userService.removeRole(1L, 1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void _23_ShouldThrowNotFound_WhenRemovingRoleFromMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeRole(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roleRepository, never()).findById(any());
    }

    @Test
    void _24_ShouldThrowNotFound_WhenRemovingMissingRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeRole(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void enableAccountEnablesUser() {
        user.setEnabled(false);
        UserResponse enabled = new UserResponse(1L, "Ada", "Lovelace", "ada@example.com", true, false, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(enabled);

        assertThat(userService.enableAccount(1L)).isEqualTo(enabled);

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void enableAccountThrowsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.enableAccount(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePasswordHashesAndSavesNewPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword1")).thenReturn("new-hashed");
        when(userRepository.save(user)).thenReturn(user);

        userService.updatePassword(1L, "newPassword1");

        assertThat(user.getPassword()).isEqualTo("new-hashed");
    }

    @Test
    void updatePasswordThrowsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePassword(99L, "newPassword1"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(passwordEncoder, never()).encode(any());
    }
}
