package at.technikum.worker.service;

import java.io.File;
import java.io.IOException;

public class OCRService {
    public static void processDocument(String documentPath) {
        try {
            // Schritt 1: PDF in Bild umwandeln mit Ghostscript
            ProcessBuilder ghostscript = new ProcessBuilder(
                    "gs", "-sDEVICE=pngalpha", "-o", "output-%d.png", "-r300", documentPath
            );
            ghostscript.start().waitFor();

            // Schritt 2: OCR auf jedes Bild anwenden mit Tesseract
            File dir = new File("./");
            for (File image : dir.listFiles((d, name) -> name.endsWith(".png"))) {
                ProcessBuilder tesseract = new ProcessBuilder(
                        "tesseract", image.getName(), image.getName().replace(".png", "")
                );
                tesseract.start().waitFor();
            }
            System.out.println("OCR completed for document: " + documentPath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
