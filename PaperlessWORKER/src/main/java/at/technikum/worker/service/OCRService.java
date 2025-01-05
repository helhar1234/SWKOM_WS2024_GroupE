package at.technikum.worker.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OCRService {

    private final Tesseract tesseract;

    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // Path to Tesseract data
        tesseract.setLanguage("eng");
    }

    public String extractText(File file) throws TesseractException {
        return tesseract.doOCR(file);
    }
}
