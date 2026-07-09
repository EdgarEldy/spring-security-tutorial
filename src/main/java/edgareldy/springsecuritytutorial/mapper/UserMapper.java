package edgareldy.springsecuritytutorial.mapper;

import edgareldy.springsecuritytutorial.dto.user.UpdateProfileRequest;
import edgareldy.springsecuritytutorial.dto.user.UserRequest;
import edgareldy.springsecuritytutorial.dto.user.UserResponse;
import edgareldy.springsecuritytutorial.entity.Role;
import edgareldy.springsecuritytutorial.entity.User;
import java.util.List;
import java.util.Set;
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
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

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

    default List<String> mapRoleNames(Set<Role> roles) {
        return roles.stream().map(Role::getRoleName).sorted().toList();
    }
}
