package edgareldy.springsecuritytutorial.dto.permission;

/**
 * Representation of a
 * {@link edgareldy.springsecuritytutorial.entity.Permission} returned by the
 * API.
 * <p>
 * Created by edgar.muhamyangabo on 7/9/26
 * Author : edgar.muhamyangabo
 * Date : 7/9/26
 * Project : spring-security-tutorial
 */
public record PermissionResponse(
        Long id,
        String resource,
        String action
) {
}
