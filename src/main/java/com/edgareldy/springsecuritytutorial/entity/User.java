package com.edgareldy.springsecuritytutorial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JPA entity mapping the {@code users} table, and the Spring Security
 * {@link UserDetails} implementation authenticated against once
 * feature/auth wires a {@code UserDetailsService} around it.
 * <p>
 * {@link #getAuthorities()} derives one {@code ROLE_<roleName>} authority
 * per assigned {@link Role} and one {@code PERMISSION_<RESOURCE>:<ACTION>}
 * authority per permission granted through those roles, so
 * {@code hasRole(...)} and {@code CustomPermissionEvaluator}-backed
 * {@code hasPermission(...)} expressions both work directly off this
 * collection. Role names and permission resource/action are upper-cased
 * here, since neither {@code RoleRequest}/{@code PermissionRequest} nor the
 * database enforce a canonical case, and {@code hasRole(...)}/
 * {@link com.edgareldy.springsecuritytutorial.security.CustomPermissionEvaluator}
 * both compare against upper-case authority strings.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(
                    "ROLE_" + role.getRoleName().toUpperCase(Locale.ROOT)));
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(
                        "PERMISSION_" + permission.getResource().toUpperCase(Locale.ROOT)
                                + ":" + permission.getAction().toUpperCase(Locale.ROOT)));
            }
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
