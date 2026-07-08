package edgareldy.springsecuritytutorial.exception;

/**
 * Thrown when an activation, password reset, or JWT token is missing,
 * expired, or already used. Caught by {@link GlobalExceptionHandler} and
 * translated into a 400 response.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-security-tutorial
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
