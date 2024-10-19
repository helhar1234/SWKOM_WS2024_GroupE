package at.technikum.swkom_ws2024_groupe.customExceptions;

public class InvalidFileUploadException extends RuntimeException {
    public InvalidFileUploadException(String message) {
        super(message);
    }
}
