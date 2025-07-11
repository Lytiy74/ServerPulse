package ua.azaika.serverpulse.exception;

/**
 @author Andrii Zaika
 **/
public class JwtTokenGenerationException extends RuntimeException {
    public JwtTokenGenerationException(String message) {
        super(message);
    }
}
