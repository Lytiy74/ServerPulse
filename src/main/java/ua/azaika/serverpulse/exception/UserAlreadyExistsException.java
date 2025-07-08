package ua.azaika.serverpulse.exception;

/**
 @author Andrii Zaika
 **/
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
