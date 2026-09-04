package com.edgareldy.springsecuritytutorial.mapper;

import com.edgareldy.springsecuritytutorial.dto.permission.PermissionRequest;
import com.edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import com.edgareldy.springsecuritytutorial.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between {@link Permission} and its DTOs.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);

    @Mapping(target = "id", ignore = true)
    Permission toEntity(PermissionRequest request);
}
