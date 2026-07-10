package edgareldy.springsecuritytutorial.service;

import edgareldy.springsecuritytutorial.dto.common.PageResponse;
import edgareldy.springsecuritytutorial.dto.user.UpdateProfileRequest;
import edgareldy.springsecuritytutorial.dto.user.UserRequest;
import edgareldy.springsecuritytutorial.dto.user.UserResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contract for {@link edgareldy.springsecuritytutorial.entity.User} business
 * operations. Controllers and tests depend on this interface, never on its
 * implementation directly.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface UserService {

    PageResponse<UserResponse> findAll(Pageable pageable);

    UserResponse findById(Long id);

    UserResponse findByEmail(String email);

    UserResponse createUser(UserRequest request);

    UserResponse updateProfile(Long id, UpdateProfileRequest request);

    void delete(Long id);

    UserResponse lock(Long id, Long currentUserId);

    UserResponse unlock(Long id);

    UserResponse assignRole(Long userId, Long roleId);

    UserResponse removeRole(Long userId, Long roleId);

    UserResponse enableAccount(Long id);

    void updatePassword(Long id, String rawNewPassword);
}
