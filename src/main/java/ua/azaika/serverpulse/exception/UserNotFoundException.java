package ua.azaika.serverpulse.exception;

/**
 @author Andrii Zaika
 **/
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
