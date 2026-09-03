package com.edgareldy.springsecuritytutorial.service;

import com.edgareldy.springsecuritytutorial.dto.permission.PermissionRequest;
import com.edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import java.util.List;

/**
 * Contract for {@link com.edgareldy.springsecuritytutorial.entity.Permission}
 * business operations. Controllers and tests depend on this interface,
 * never on its implementation directly.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface PermissionService {

    List<PermissionResponse> findAll();

    PermissionResponse create(PermissionRequest request);

    void delete(Long id);
}
