package com.edgareldy.springsecuritytutorial.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link User#getAuthorities()}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
class UserTest {

    @Test
    void _01_ShouldReturnEmptyAuthorities_WhenUserHasNoRoles() {
        User user = User.builder().roles(new HashSet<>()).build();

        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void _02_ShouldDeriveRoleAndPermissionAuthorities_WhenUserHasRoles() {
        Permission permission = Permission.builder().resource("USER").action("CREATE").build();
        Role role = Role.builder().roleName("ADMIN").permissions(Set.of(permission)).build();
        User user = User.builder().roles(Set.of(role)).build();

        assertThat(user.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "PERMISSION_USER:CREATE");
    }

    @Test
    void _03_ShouldUpperCaseAuthorities_WhenStoredCaseDiffers() {
        Permission permission = Permission.builder().resource("user").action("create").build();
        Role role = Role.builder().roleName("admin").permissions(Set.of(permission)).build();
        User user = User.builder().roles(Set.of(role)).build();

        assertThat(user.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "PERMISSION_USER:CREATE");
    }
}
