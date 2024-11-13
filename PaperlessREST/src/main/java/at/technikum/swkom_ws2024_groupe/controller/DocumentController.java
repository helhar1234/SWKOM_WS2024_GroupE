package at.technikum.swkom_ws2024_groupe.controller;

import at.technikum.swkom_ws2024_groupe.customExceptions.DocumentNotFoundException;
import at.technikum.swkom_ws2024_groupe.customExceptions.InvalidFileUploadException;
import at.technikum.swkom_ws2024_groupe.entities.Document;
import at.technikum.swkom_ws2024_groupe.service.DocumentService;
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

//todo: change status numbers to fixed status vatiables (HttpStatus.)
//todo: no content in body for response?

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Uploads a document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document uploaded successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
            }

            Document document = documentService.uploadDocument(file.getOriginalFilename(), file.getContentType(), file.getSize());
            return ResponseEntity.status(201).body("File uploaded successfully with id: " + document.getId());

        } catch (InvalidFileUploadException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File upload failed due to server error.");
        }
    }

    @Operation(summary = "Fetches a document by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document found", content = @Content(schema = @Schema(implementation = Document.class))),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable UUID id) {
        try {
            Optional<Document> document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document.get());
        } catch (InvalidFileUploadException | DocumentNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while retrieving the document.");
        }
    }

    @Operation(summary = "Fetches all documents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents found", content = @Content(schema = @Schema(implementation = Document.class))),
            @ApiResponse(responseCode = "404", description = "No documents found")
    })
    @GetMapping()
    public ResponseEntity<?> getAllDocuments() {
        try {
            List<Document> documents = documentService.getAllDocuments();
            return ResponseEntity.ok(documents);
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while retrieving the documents.");
        }
    }
}
