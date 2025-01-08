package at.technikum.worker.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class OCRService {

    private final Tesseract tesseract;

    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // Path to Tesseract data
        tesseract.setLanguage("eng");
        System.out.println("Tesseract OCR initialized with data path: /usr/share/tesseract-ocr/4.00/tessdata");
    }

    public String extractText(File file) throws TesseractException {
        log.info("Starting OCR extraction for file: {}", file.getName());
        String text = tesseract.doOCR(file);
        log.info("OCR extraction completed for file: {}", file.getName());
        return text;
    }
}
