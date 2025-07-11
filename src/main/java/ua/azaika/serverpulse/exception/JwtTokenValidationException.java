package ua.azaika.serverpulse.exception;

/**
 @author Andrii Zaika
 **/
public class JwtTokenValidationException extends RuntimeException {
    public JwtTokenValidationException(String message) {
        super(message);
    }
}
