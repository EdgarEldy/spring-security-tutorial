package edgareldy.springsecuritytutorial.exception;

/**
 * Thrown by service implementations when a requested entity does not exist
 * (e.g. looking up a User by an id that is not in the database).
 * Caught by {@link GlobalExceptionHandler} and translated into a 404 response.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-security-tutorial
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
