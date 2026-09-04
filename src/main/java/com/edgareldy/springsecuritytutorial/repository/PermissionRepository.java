package com.edgareldy.springsecuritytutorial.repository;

import com.edgareldy.springsecuritytutorial.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Permission}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByResourceIgnoreCaseAndActionIgnoreCase(String resource, String action);
}
