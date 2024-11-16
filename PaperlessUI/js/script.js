document.addEventListener("DOMContentLoaded", () => {
  const content = document.getElementById("content");
  const links = document.querySelectorAll(".nav-link");

  const loadContent = async (path) => {
    try {
      const response = await fetch(path);
      if (!response.ok) {
        throw new Error(`Error loading page: ${path}`);
      }
      const html = await response.text();
      content.innerHTML = html;

      // Trigger additional logic based on current page
      loadData(window.location.pathname);
    } catch (error) {
      content.innerHTML = `<div class="alert alert-danger">Error loading page: ${error.message}</div>`;
    }
  };

  const setActiveLink = (route) => {
    links.forEach((link) => {
      link.classList.toggle("active", link.getAttribute("href") === route);
    });
  };

  links.forEach((link) => {
    link.addEventListener("click", (e) => {
      e.preventDefault();
      const route = link.getAttribute("href");
      window.history.pushState(null, "", route);
      loadContent(`pages${route}.html`);
      setActiveLink(route);
    });
  });

  const currentPath = window.location.pathname;
  if (currentPath === "/" || currentPath === "/home") {
    loadContent("pages/home.html");
    setActiveLink("/home");
  } else {
    loadContent(`pages${currentPath}.html`);
    setActiveLink(currentPath);
  }

  window.addEventListener("popstate", () => {
    const newPath = window.location.pathname;
    loadContent(
      newPath === "/home" ? "pages/home.html" : `pages${newPath}.html`
    );
    setActiveLink(newPath);
  });
});

// Funktion, um Dokumente zu laden und die Tabelle zu füllen
const loadDocuments = async () => {
  try {
    const response = await fetch("http://localhost:8081/api/documents");
    if (!response.ok) {
      throw new Error(`Fehler: ${response.statusText}`);
    }

    const documents = await response.json(); // Antwort als JSON
    console.log("API Response:", documents); // Ausgabe in der Konsole

    const tableBody = document.getElementById("documents-table-body");
    if (tableBody) {
      tableBody.innerHTML = ""; // Leere Tabelle, falls bereits Einträge vorhanden

      if (documents.length === 0) {
        tableBody.innerHTML = `<tr>
                        <td colspan="7" class="text-center">No Documents</td>
                    </tr>`;
      } else {
        documents.forEach((doc) => {
          const row = `
                        <tr>
                            <td>${doc.id || "-"}</td>
                            <td>${doc.filename || "-"}</td>
                            <td>${doc.filesize || "-"}</td>
                            <td>${doc.filetype || "-"}</td>
                            <td>${doc.uploadDate || "-"}</td>
                            <td>
                                <a href="data:${doc.filetype};base64,${doc.file}" download="${doc.filename}" class="btn btn-success">
                                    Download
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-download" viewBox="0 0 16 16">
                                        <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5"/>
                                        <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708z"/>
                                    </svg>
                                </a>
                            </td>
                            <td>
                                <button class="btn btn-danger" onclick="deleteDocument('${doc.id}')"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                                <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z"/>
                                <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z"/>
                                </svg></button>
                            </td>
                        </tr>`;
          tableBody.innerHTML += row;
        });
      }
    }
  } catch (error) {
    console.error("Fehler beim Laden der Dokumente:", error.message);

    const tableBody = document.getElementById("documents-table-body");
    if (tableBody) {
      tableBody.innerHTML = `<tr>
                    <td colspan="7" class="text-center text-danger">Error loading documents: ${error.message}</td>
                </tr>`;
    }
  }
};

// Function to delete a document
const deleteDocument = async (id) => {
  console.log(id);
  try {
    const response = await fetch(`http://localhost:8081/api/documents/${id}`, {
      method: "DELETE",
    });

    if (response.ok) {
      console.log("Document deleted successfully!");
      loadDocuments();
    } else {
      const errorMessage = await response.text();
      console.log(`Failed to delete document: ${errorMessage}`);
    }
  } catch (error) {
    console.log(`Error deleting document: ${error.message}`);
  }
};

// Funktion, um datenbasiert zu laden
const loadData = (newPath) => {
  if (newPath === "/show-documents") {
    console.log("Loading All Documents");
    loadDocuments();
  } else if (newPath === "/add-document") {
    console.log("Loading Add Document");
    initializeUploadFunctionality();
  }
};

// Upload-Dokument-Logik
const initializeUploadFunctionality = () => {
  const uploadButton = document.getElementById("uploadButton");
  if (!uploadButton) {
    console.error("Upload button not found!");
    return;
  }

  uploadButton.addEventListener("click", () => {
    const fileInput = document.getElementById("fileInput");
    if (!fileInput || fileInput.files.length === 0) {
      document.getElementById(
        "upload-status"
      ).innerHTML = `<div class="alert alert-warning">Please select a file to upload.</div>`;
      return;
    }

    if (
      file.type !== "application/pdf" &&
      !file.name.toLowerCase().endsWith(".pdf")
    ) {
      uploadStatus.innerHTML = `<div class="alert alert-danger">Invalid file type. Only PDF files are allowed.</div>`;
      return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    uploadDocument(formData);
  });
};

const uploadDocument = async (formData) => {
  try {
    const response = await fetch("http://localhost:8081/api/documents", {
      method: "POST",
      body: formData,
    });

    const serverResponse = await response.text();
    const uploadStatus = document.getElementById("upload-status");

    if (response.ok) {
      uploadStatus.innerHTML = `<div class="alert alert-success">Document uploaded successfully!</div>`;
    } else {
      uploadStatus.innerHTML = `<div class="alert alert-danger">Error: ${serverResponse}</div>`;
    }
  } catch (error) {
    document.getElementById(
      "upload-status"
    ).innerHTML = `<div class="alert alert-danger">
        File upload failed due to server error: ${error.message}
      </div>`;
  }
};
