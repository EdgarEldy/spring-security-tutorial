package com.edgareldy.springsecuritytutorial.mapper;

import com.edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import com.edgareldy.springsecuritytutorial.dto.user.UpdateProfileRequest;
import com.edgareldy.springsecuritytutorial.dto.user.UserRequest;
import com.edgareldy.springsecuritytutorial.dto.user.UserResponse;
import com.edgareldy.springsecuritytutorial.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper converting between {@link User} and its DTOs. The
 * password is never mapped from an entity to a response, and never mapped
 * directly from a request to an entity: {@code UserServiceImpl} sets it
 * explicitly after encoding it, so the raw value never round-trips through
 * generated mapper code unencoded.
 * <p>
 * Two response methods exist for the same reason as
 * {@code RoleMapper.toResponse}/{@code CustomerMapper.toDetailResponse} in
 * the sibling project: {@link #toResponse(User)} always leaves {@code roles}
 * as an empty list (used by the paginated list and every write endpoint),
 * {@link #toDetailResponse(User, List)} takes the caller-supplied list of
 * roles (resolved by the service layer via {@code RoleMapper}) for the
 * single-user detail endpoint only.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(java.util.List.of())")
    UserResponse toResponse(User user);

    @Mapping(target = "roles", source = "roles")
    UserResponse toDetailResponse(User user, List<RoleResponse> roles);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateProfile(UpdateProfileRequest request, @MappingTarget User user);
}
