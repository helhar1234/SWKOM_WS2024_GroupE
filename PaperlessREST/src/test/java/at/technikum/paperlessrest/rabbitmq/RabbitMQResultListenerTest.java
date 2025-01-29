package at.technikum.paperlessrest.rabbitmq;

import at.technikum.paperlessrest.dto.DocumentDTO;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.repository.DocumentRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMQResultListenerTest {

    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final RabbitMQResultListener rabbitMQResultListener = new RabbitMQResultListener(documentRepository);

    @Test
    void handleOcrResult_success() throws JSONException {
        // Arrange
        String documentId = "123e4567-e89b-12d3-a456-426614174000";
        DocumentDTO document = DocumentDTO.builder()
                .id(documentId)
                .filename("test.pdf")
                .ocrJobDone(false)
                .build();

        String message = new JSONObject()
                .put("documentId", documentId)
                .toString();

        when(documentRepository.findById(documentId)).thenReturn(java.util.Optional.of(new Document(document)));
        when(documentRepository.save(any(Document.class))).thenReturn(new Document(document));

        // Act
        rabbitMQResultListener.handleOcrResult(message);

        // Assert
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());

        Document savedDocument = documentCaptor.getValue();
        assertTrue(savedDocument.isOcrJobDone());
        verify(documentRepository).findById(documentId);
    }

    @Test
    void handleOcrResult_documentNotFound() throws JSONException {
        // Arrange
        String documentId = "nonexistent-id";
        String message = new JSONObject()
                .put("documentId", documentId)
                .toString();

        when(documentRepository.findById(documentId)).thenReturn(java.util.Optional.empty());

        // Act
        rabbitMQResultListener.handleOcrResult(message);

        // Assert
        verify(documentRepository).findById(documentId);
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void handleOcrResult_invalidJsonMessage() {
        // Arrange
        String invalidMessage = "{invalid-json}";

        // Act
        rabbitMQResultListener.handleOcrResult(invalidMessage);

        // Assert
        verify(documentRepository, never()).findById(anyString());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void handleOcrResult_missingDocumentId() throws JSONException {
        // Arrange
        String message = new JSONObject()
                .put("invalidKey", "value")
                .toString();

        // Act
        rabbitMQResultListener.handleOcrResult(message);

        // Assert
        verify(documentRepository, never()).findById(anyString());
        verify(documentRepository, never()).save(any(Document.class));
    }
}
