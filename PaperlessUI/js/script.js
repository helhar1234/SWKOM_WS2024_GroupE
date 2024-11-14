document.addEventListener("DOMContentLoaded", () => {
    document.querySelector("button").addEventListener("click", showAllFiles);
});


async function showAllFiles() {
    console.log("showAllFiles function called");  // Debug-Ausgabe

    try {
        console.log("Sending fetch request...");  // Debug-Ausgabe
        const response = await fetch('http://paperlessrest:8081/api/documents', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        console.log("Fetch request sent, processing response...");  // Debug-Ausgabe

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await response.json();
        console.log("Data received:", data);
        document.getElementById("fileList").textContent = JSON.stringify(data, null, 2);
    } catch (error) {
        console.error('Error fetching documents:', error);
        document.getElementById("fileList").textContent = 'Error fetching documents: ' + error.message;
    }
}
