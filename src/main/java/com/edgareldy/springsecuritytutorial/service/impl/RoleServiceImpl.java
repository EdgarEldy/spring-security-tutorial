package com.edgareldy.springsecuritytutorial.service.impl;

import com.edgareldy.springsecuritytutorial.dto.role.RoleRequest;
import com.edgareldy.springsecuritytutorial.dto.role.RoleResponse;
import com.edgareldy.springsecuritytutorial.entity.Permission;
import com.edgareldy.springsecuritytutorial.entity.Role;
import com.edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import com.edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import com.edgareldy.springsecuritytutorial.mapper.RoleMapper;
import com.edgareldy.springsecuritytutorial.repository.PermissionRepository;
import com.edgareldy.springsecuritytutorial.repository.RoleRepository;
import com.edgareldy.springsecuritytutorial.repository.UserRepository;
import com.edgareldy.springsecuritytutorial.service.RoleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link RoleService} implementation backed by
 * {@link RoleRepository}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(roleMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByRoleNameIgnoreCase(request.roleName())) {
            throw new BusinessRuleException("Role " + request.roleName() + " already exists");
        }
        Role role = roleMapper.toEntity(request);
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = getRoleOrThrow(id);
        if (!role.getRoleName().equalsIgnoreCase(request.roleName())
                && roleRepository.existsByRoleNameIgnoreCase(request.roleName())) {
            throw new BusinessRuleException("Role " + request.roleName() + " already exists");
        }
        role.setRoleName(request.roleName());
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = getRoleOrThrow(id);
        if (userRepository.existsByRoles_Id(id)) {
            throw new BusinessRuleException("Role " + id + " is assigned to at least one user and cannot be deleted");
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleResponse assignPermission(Long roleId, Long permissionId) {
        Role role = getRoleOrThrow(roleId);
        Permission permission = getPermissionOrThrow(permissionId);
        if (!role.getPermissions().add(permission)) {
            throw new BusinessRuleException("Permission " + permissionId + " is already assigned to role " + roleId);
        }
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse removePermission(Long roleId, Long permissionId) {
        Role role = getRoleOrThrow(roleId);
        Permission permission = getPermissionOrThrow(permissionId);
        if (!role.getPermissions().remove(permission)) {
            throw new BusinessRuleException("Permission " + permissionId + " is not assigned to role " + roleId);
        }
        return roleMapper.toResponse(roleRepository.save(role));
    }

    private Role getRoleOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id " + id));
    }

    private Permission getPermissionOrThrow(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id " + id));
    }
}
