document.addEventListener("DOMContentLoaded", () => {
    const uploadButton = document.getElementById("uploadButton");
    const fileInput = document.getElementById("fileInput");
    const loader = document.getElementById("loader");
    const searchButton = document.getElementById("search-button");
    const searchInput = document.getElementById("search-input");


    // Handle file upload
    uploadButton.addEventListener("click", () => {
        fileInput.click();
    });

    fileInput.addEventListener("change", () => {
        if (fileInput.files.length > 0) {
            const file = fileInput.files[0];
            if (file.type === "application/pdf") {
                const formData = new FormData();
                formData.append("file", file);

                loader.style.display = "flex"; // Show loader
                uploadDocument(formData);
            } else {
                alert("Only PDF files are allowed.");
            }
        }
    });

    const uploadDocument = async (formData) => {
        try {
            const response = await fetch("http://localhost:8081/api/documents", {
                method: "POST",
                body: formData,
            });

            if (response.ok) {
                alert("Document uploaded successfully!");
                loadDocuments(); // Reload documents after upload
            } else {
                alert("Failed to upload document.");
            }
        } catch (error) {
            console.error("Error uploading document:", error);
        } finally {
            loader.style.display = "none"; // Hide loader
        }
    };

    // Load all documents and render the table
    const loadDocuments = async () => {
        try {
            const response = await fetch("http://localhost:8081/api/documents");
            if (!response.ok) throw new Error("Failed to fetch documents.");

            const documents = await response.json();
            const tableBody = document.getElementById("documents-table-body");
            tableBody.innerHTML = "";

            documents.forEach(({ document, file }) => {
                const downloadUrl = `http://localhost:8081/api/documents/${document.id}/download`;

                const row = `
                    <tr>
                        <td>${document.id}</td>
                        <td>${document.filename}</td>
                        <td>${(document.filesize / 1024).toFixed(2)} KB</td>
                        <td>${document.filetype}</td>
                        <td>${new Date(document.uploadDate).toLocaleString()}</td>
                        <td>
                            <div class="action-buttons">
                                <a href="${downloadUrl}" class="btn btn-sm btn-outline-success" download="${document.filename}">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-download" viewBox="0 0 16 16">
                                        <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5"/>
                                        <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708z"/>
                                    </svg>
                                </a>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteDocument('${document.id}')">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z"/>
                                        <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z"/>
                                    </svg>
                                </button>
                            </div>
                        </td>
                    </tr>`;
                tableBody.innerHTML += row;
            });
        } catch (error) {
            console.error("Error loading documents:", error);
        }
    };

    // Delete a document
    window.deleteDocument = async (id) => {
        try {
            const response = await fetch(`http://localhost:8081/api/documents/${id}`, { method: "DELETE" });
            if (response.ok) {
                loadDocuments(); // Reload documents after deletion
            } else {
                alert("Failed to delete document.");
            }
        } catch (error) {
            console.error("Error deleting document:", error);
        }
    };

    // Search documents
    const searchDocuments = async (query) => {
        try {
            const response = await fetch(`http://localhost:8081/api/documents/search?query=${encodeURIComponent(query)}`);
            if (!response.ok) throw new Error("Failed to search documents.");

            const documents = await response.json();
            const tableBody = document.getElementById("documents-table-body");
            tableBody.innerHTML = "";

            documents.forEach(({ document, file }) => {
                const downloadUrl = `http://localhost:8081/api/documents/${document.id}/download`;

                const row = `
                    <tr>
                        <td>${document.id}</td>
                        <td>${document.filename}</td>
                        <td>${(document.filesize / 1024).toFixed(2)} KB</td>
                        <td>${document.filetype}</td>
                        <td>${new Date(document.uploadDate).toLocaleString()}</td>
                        <td>
                            <div class="action-buttons">
                                <a href="${downloadUrl}" class="btn btn-sm btn-outline-success" download="${document.filename}">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-download" viewBox="0 0 16 16">
                                        <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5"/>
                                        <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708z"/>
                                    </svg>
                                </a>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteDocument('${document.id}')">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z"/>
                                        <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z"/>
                                    </svg>
                                </button>
                            </div>
                        </td>
                    </tr>`;
                tableBody.innerHTML += row;
            });
        } catch (error) {
            console.error("Error searching documents:", error);
        }
    };

    // Handle search button click
    searchButton.addEventListener("click", () => {
        const query = searchInput.value.trim();
        if (query) {
            searchDocuments(query);
        } else {
            alert("Please enter a search query.");
        }
    });

    // Load documents on page load
    loadDocuments();

});
