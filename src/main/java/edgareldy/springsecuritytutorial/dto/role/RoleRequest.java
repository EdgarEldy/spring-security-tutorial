package edgareldy.springsecuritytutorial.dto.role;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload accepted by {@code POST}/{@code PUT} {@code /api/roles}. Permission
 * assignment goes through the dedicated
 * {@code /api/roles/{roleId}/permissions/{permissionId}} endpoints, not this
 * request.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record RoleRequest(

        @NotBlank(message = "roleName must not be blank")
        String roleName
) {
}
