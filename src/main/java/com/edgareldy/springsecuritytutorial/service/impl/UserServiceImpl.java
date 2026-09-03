package com.edgareldy.springsecuritytutorial.service.impl;

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
import com.edgareldy.springsecuritytutorial.service.UserService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link UserService} implementation backed by
 * {@link UserRepository}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<UserResponse> findAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    @Override
    public UserResponse findById(Long id) {
        return toDetailResponse(getUserOrThrow(id));
    }

    @Override
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException("Email " + request.email() + " is already in use");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(false);
        user.setAccountLocked(false);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = getUserOrThrow(id);
        userMapper.updateProfile(request, user);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.delete(getUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse lock(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessRuleException("An admin cannot lock their own account");
        }
        User user = getUserOrThrow(id);
        user.setAccountLocked(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse unlock(Long id) {
        User user = getUserOrThrow(id);
        user.setAccountLocked(false);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse assignRole(Long userId, Long roleId) {
        User user = getUserOrThrow(userId);
        Role role = getRoleOrThrow(roleId);
        if (!user.getRoles().add(role)) {
            throw new BusinessRuleException("Role " + roleId + " is already assigned to user " + userId);
        }
        return toDetailResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse removeRole(Long userId, Long roleId) {
        User user = getUserOrThrow(userId);
        Role role = getRoleOrThrow(roleId);
        if (!user.getRoles().remove(role)) {
            throw new BusinessRuleException("Role " + roleId + " is not assigned to user " + userId);
        }
        return toDetailResponse(userRepository.save(user));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private Role getRoleOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id " + id));
    }

    private UserResponse toDetailResponse(User user) {
        List<RoleResponse> roles = user.getRoles().stream()
                .map(roleMapper::toResponse)
                .sorted(Comparator.comparing(RoleResponse::roleName))
                .toList();
        return userMapper.toDetailResponse(user, roles);
    }
}
