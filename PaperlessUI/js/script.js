async function showAllFiles() {
    try {
        const response = await fetch('http://localhost:8081/api/documents');  // Verwende den Alias hier

        if (response.ok) {
            const documents = await response.json();
            const fileListDiv = document.getElementById('fileList');
            fileListDiv.innerHTML = '';

            if (documents && documents.length > 0) {
                documents.forEach(doc => {
                    const fileItem = document.createElement('p');
                    fileItem.textContent = `Document ID: ${doc.id}, Name: ${doc.name}`;
                    fileListDiv.appendChild(fileItem);
                });
            } else {
                fileListDiv.innerHTML = '<p>No documents found.</p>';
            }
        } else if (response.status === 404) {
            document.getElementById('fileList').innerHTML = '<p>No documents found.</p>';
        } else {
            throw new Error('Failed to fetch documents');
        }
    } catch (error) {
        console.error('Error fetching documents:', error);
        document.getElementById('fileList').innerHTML = '<p>Error fetching documents. Please try again later.</p>';
    }
}
