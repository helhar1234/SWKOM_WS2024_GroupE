package at.technikum.paperlessrest.integration;

import at.technikum.paperlessrest.dto.DocumentDTO;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class DocumentRepositoryIntegrationTest {

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeAll
    static void setLogPathForTests() {
        System.setProperty("LOG_PATH", "./logs/test-logs");
    }
/*
    @Test
    void testSaveAndFindById() {
        // Arrange
        DocumentDTO document = DocumentDTO.builder()
                .id("1")
                .filename("test.pdf")
                .filesize(123L)
                .build();
        documentRepository.save(document);

        // Act
        Optional<Document> result = documentRepository.findById("1");

        // Assert
        assertTrue(result.isPresent());
    }*/
}
