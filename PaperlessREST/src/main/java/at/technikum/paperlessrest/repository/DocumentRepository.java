package at.technikum.paperlessrest.repository;


import at.technikum.paperlessrest.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {
}
