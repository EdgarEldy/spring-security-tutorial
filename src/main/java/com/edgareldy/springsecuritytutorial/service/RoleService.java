package com.edgareldy.springsecuritytutorial.service;

import com.edgareldy.springsecuritytutorial.dto.role.RoleRequest;
import com.edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import java.util.List;

/**
 * Contract for {@link com.edgareldy.springsecuritytutorial.entity.Role} business
 * operations, including permission assignment. Controllers and tests depend
 * on this interface, never on its implementation directly.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface RoleService {

    List<RoleResponse> findAll();

    RoleResponse create(RoleRequest request);

    RoleResponse update(Long id, RoleRequest request);

    void delete(Long id);

    RoleResponse assignPermission(Long roleId, Long permissionId);

    RoleResponse removePermission(Long roleId, Long permissionId);
}
