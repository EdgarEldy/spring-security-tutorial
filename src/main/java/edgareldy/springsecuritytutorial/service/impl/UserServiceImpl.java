package edgareldy.springsecuritytutorial.service.impl;

import edgareldy.springsecuritytutorial.dto.common.PageResponse;
import edgareldy.springsecuritytutorial.dto.user.UpdateProfileRequest;
import edgareldy.springsecuritytutorial.dto.user.UserRequest;
import edgareldy.springsecuritytutorial.dto.user.UserResponse;
import edgareldy.springsecuritytutorial.entity.User;
import edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import edgareldy.springsecuritytutorial.mapper.UserMapper;
import edgareldy.springsecuritytutorial.repository.UserRepository;
import edgareldy.springsecuritytutorial.service.UserService;
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
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<UserResponse> findAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    @Override
    public UserResponse findById(Long id) {
        return userMapper.toResponse(getUserOrThrow(id));
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

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }
}
