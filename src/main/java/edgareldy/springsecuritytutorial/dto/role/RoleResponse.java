package edgareldy.springsecuritytutorial.dto.role;

import edgareldy.springsecuritytutorial.dto.permission.PermissionResponse;
import java.util.List;

/**
 * Representation of a {@link edgareldy.springsecuritytutorial.entity.Role}
 * returned by the API, including its currently assigned permissions.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record RoleResponse(
        Long id,
        String roleName,
        List<PermissionResponse> permissions
) {
}
