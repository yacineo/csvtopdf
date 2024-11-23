package dz.dgmf.dgmf.controller;

import dz.dgmf.dgmf.utils.CsvUtils;
import dz.dgmf.dgmf.utils.PdfGenerator;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class CsvToPdfController {

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf() {
        try {

            List<String[]> csvData = CsvUtils.readCsv("C:/dev/workspaces/java/dgmf/test.csv");
            byte[] pdfBytes =
                    PdfGenerator.generatePdfWithHeader(csvData);


            // 3. Return PDF as response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("output.pdf").build());

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
}