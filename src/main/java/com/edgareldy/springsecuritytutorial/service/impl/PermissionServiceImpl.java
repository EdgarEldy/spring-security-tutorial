package com.edgareldy.springsecuritytutorial.service.impl;

import com.edgareldy.springsecuritytutorial.dto.permission.PermissionRequest;
import com.edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import com.edgareldy.springsecuritytutorial.entity.Permission;
import com.edgareldy.springsecuritytutorial.exception.BusinessRuleException;
import com.edgareldy.springsecuritytutorial.exception.ResourceNotFoundException;
import com.edgareldy.springsecuritytutorial.mapper.PermissionMapper;
import com.edgareldy.springsecuritytutorial.repository.PermissionRepository;
import com.edgareldy.springsecuritytutorial.repository.RoleRepository;
import com.edgareldy.springsecuritytutorial.service.PermissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link PermissionService} implementation backed by
 * {@link PermissionRepository}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll().stream().map(permissionMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        if (permissionRepository.existsByResourceIgnoreCaseAndActionIgnoreCase(request.resource(), request.action())) {
            throw new BusinessRuleException(
                    "Permission " + request.resource() + ":" + request.action() + " already exists");
        }
        Permission permission = permissionMapper.toEntity(request);
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id " + id));
        if (roleRepository.existsByPermissions_Id(id)) {
            throw new BusinessRuleException("Permission " + id + " is assigned to at least one role and cannot be deleted");
        }
        permissionRepository.delete(permission);
    }
}
