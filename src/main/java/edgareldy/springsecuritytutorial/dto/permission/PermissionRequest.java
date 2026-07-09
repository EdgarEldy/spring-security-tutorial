package edgareldy.springsecuritytutorial.dto.permission;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload accepted by {@code POST /api/permissions}.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record PermissionRequest(

        @NotBlank(message = "resource must not be blank")
        String resource,

        @NotBlank(message = "action must not be blank")
        String action
) {
}
