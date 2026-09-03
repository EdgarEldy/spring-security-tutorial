package com.edgareldy.springsecuritytutorial.mapper;

import com.edgareldy.springsecuritytutorial.dto.role.RoleRequest;
import com.edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import com.edgareldy.springsecuritytutorial.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between {@link Role} and its DTOs, delegating
 * permission mapping to {@link PermissionMapper}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Mapper(componentModel = "spring", uses = PermissionMapper.class)
public interface RoleMapper {

    RoleResponse toResponse(Role role);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleRequest request);
}
