package at.technikum.paperlessrest.controller;

import at.technikum.paperlessrest.customExceptions.DocumentNotFoundException;
import at.technikum.paperlessrest.customExceptions.InvalidFileUploadException;
import at.technikum.paperlessrest.entities.Document;
import at.technikum.paperlessrest.rabbitMQ.RabbitMQProducer;
import at.technikum.paperlessrest.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final RabbitMQProducer rabbitMQProducer;

    public DocumentController(DocumentService documentService, RabbitMQProducer rabbitMQProducer) {
        this.documentService = documentService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @Operation(summary = "Uploads a document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document uploaded successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @CrossOrigin
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Document document = documentService.uploadDocument(file);
            rabbitMQProducer.sendToQueue("File has been uploaded " + file.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(document.getId());
        } catch (InvalidFileUploadException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed.");
        }
    }

    @Operation(summary = "Deletes a document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @CrossOrigin
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable UUID id) {
        try {
            boolean deleted = documentService.deleteDocumentById(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
            }
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting document");
        }
    }

    @Operation(summary = "Fetches a document by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document found", content = @Content(schema = @Schema(implementation = Document.class))),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @CrossOrigin
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable UUID id) {
        try {
            Optional<Document> document = documentService.getDocumentById(id);
            return ResponseEntity.status(HttpStatus.OK).body(document.get());
        } catch (InvalidFileUploadException | DocumentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving the document.");
        }
    }

    @Operation(summary = "Fetches all documents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents found or empty list", content = @Content(schema = @Schema(implementation = Document.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @CrossOrigin
    @GetMapping()
    public ResponseEntity<?> getAllDocuments() {
        try {
            List<Document> documents = documentService.getAllDocuments();
            return ResponseEntity.status(HttpStatus.OK).body(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving the document.");
        }
    }

}
