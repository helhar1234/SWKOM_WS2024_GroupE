package at.technikum.paperlessrest.repository;

import at.technikum.paperlessrest.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByFilenameContainingIgnoreCase(String query);
    List<Document> findByIdContainingIgnoreCase(String query);
}
