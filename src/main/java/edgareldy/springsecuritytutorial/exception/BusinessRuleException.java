package edgareldy.springsecuritytutorial.exception;

/**
 * Thrown by service implementations when an operation violates a domain
 * rule (e.g. an ADMIN trying to lock their own account).
 * Caught by {@link GlobalExceptionHandler} and translated into a 422 response.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-security-tutorial
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
