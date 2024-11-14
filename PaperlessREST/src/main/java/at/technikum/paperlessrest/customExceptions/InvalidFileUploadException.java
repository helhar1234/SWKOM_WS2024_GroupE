package at.technikum.paperlessrest.customExceptions;

public class InvalidFileUploadException extends RuntimeException {
    public InvalidFileUploadException(String message) {
        super(message);
    }
}
