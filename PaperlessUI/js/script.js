document.addEventListener("DOMContentLoaded", () => {
  const uploadButton = document.getElementById("uploadButton");
  const fileInput = document.getElementById("fileInput");
  const loader = document.getElementById("loader");
  const searchButton = document.getElementById("search-button");
  const searchInput = document.getElementById("search-input");
  const tableBody = document.getElementById("documents-table-body");

  // Handle file upload
  uploadButton.addEventListener("click", () => fileInput.click());

  fileInput.addEventListener("change", () => {
    if (fileInput.files.length > 0) {
      const file = fileInput.files[0];
      if (file.type === "application/pdf") {
        const formData = new FormData();
        formData.append("file", file);

        loader.style.display = "flex";
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
        loadDocuments();
      } else {
        alert("Failed to upload document.");
      }
    } catch (error) {
      console.error("Error uploading document:", error);
    } finally {
      loader.style.display = "none";
    }
  };

  // Load all documents
  const loadDocuments = async () => {
    try {
      const response = await fetch("http://localhost:8081/api/documents");
      if (!response.ok) throw new Error("Failed to fetch documents.");
      const documents = await response.json();
      renderTable(documents);
    } catch (error) {
      console.error("Error loading documents:", error);
    }
  };

  // Search function for documents
  const searchDocuments = async (query) => {
    try {
      const response = await fetch(
        `http://localhost:8081/api/documents/search?query=${encodeURIComponent(
          query
        )}`
      );
      if (!response.ok) throw new Error("Failed to search documents.");

      const documents = await response.json();
      renderTable(documents);
    } catch (error) {
      console.error("Error searching documents:", error);
    }
  };

  // Event listener for search button
  searchButton.addEventListener("click", () => {
    const query = searchInput.value.trim();
    if (query) {
      searchDocuments(query);
    } else {
      alert("Please enter a search query.");
    }
  });

  // Function to render documents table
  const renderTable = (documents) => {
    //console.log(documents);
    tableBody.innerHTML = "";
    documents.forEach((document) => {
      tableBody.innerHTML += createTableRow(document);
    });
  };
  

  // Create table row with OCR status
  const createTableRow = (document) => {
    const downloadUrl = `http://localhost:8081/api/documents/${document.id}/download`;

    // Improved SVG icons for OCR status
    const ocrStatus = document.ocrJobDone
      ? `<svg xmlns="http://www.w3.org/2000/svg" width="25" height="25" fill="green" class="bi bi-check" viewBox="0 0 16 16">
             <path d="M10.97 4.97a.75.75 0 0 1 1.07 1.05l-3.99 4.99a.75.75 0 0 1-1.08.02L4.324 8.384a.75.75 0 1 1 1.06-1.06l2.094 2.093 3.473-4.425z"/>
           </svg>`
      : `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="orange" class="bi bi-hourglass-split" viewBox="0 0 16 16">
             <path d="M2.5 15a.5.5 0 1 1 0-1h1v-1a4.5 4.5 0 0 1 2.557-4.06c.29-.139.443-.377.443-.59v-.7c0-.213-.154-.451-.443-.59A4.5 4.5 0 0 1 3.5 3V2h-1a.5.5 0 0 1 0-1h11a.5.5 0 0 1 0 1h-1v1a4.5 4.5 0 0 1-2.557 4.06c-.29.139-.443.377-.443.59v.7c0 .213.154.451.443.59A4.5 4.5 0 0 1 12.5 13v1h1a.5.5 0 0 1 0 1zm2-13v1c0 .537.12 1.045.337 1.5h6.326c.216-.455.337-.963.337-1.5V2z"/>
           </svg>`;

    return `
        <tr>
          <td>${document.id}</td>
          <td>${document.filename}</td>
          <td>${(document.filesize / 1024).toFixed(2)} KB</td>
          <td>${document.filetype}</td>
          <td>${new Date(document.uploadDate).toLocaleString()}</td>
          <td>${ocrStatus}</td>
          <td>
            <div class="action-buttons">
              <a href="${downloadUrl}" class="btn btn-sm btn-outline-success" download="${
      document.filename
    }">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-download" viewBox="0 0 16 16">
                  <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5"/>
                  <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708z"/>
                </svg>
              </a>
              <button class="btn btn-sm btn-outline-danger" onclick="deleteDocument('${
                document.id
              }')">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z"/>
                                        <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z"/>
                                    </svg>
              </button>
            </div>
          </td>
        </tr>`;
  };

  // Delete document
  window.deleteDocument = async (id) => {
    try {
      await fetch(`http://localhost:8081/api/documents/${id}`, {
        method: "DELETE",
      });
      loadDocuments();
    } catch (error) {
      console.error("Error deleting document:", error);
    }
  };

  // Load documents on page load
  loadDocuments();
});
