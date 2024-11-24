package dz.dgmf.dgmf.controller;

import dz.dgmf.dgmf.utils.CsvUtils;
import dz.dgmf.dgmf.utils.PdfGenerator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class CsvToPdfController {

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam(value = "depense", required = false, defaultValue = "false") boolean depense,
            @RequestParam(value = "depenseRecette", required = false, defaultValue = "false") boolean depenseRecette,
            @RequestParam(value = "recette", required = false, defaultValue = "false") boolean recette

    ) {
        try {

            Resource resource = new ClassPathResource("data/test.csv");
            List<String[]> csvData = CsvUtils.readCsv(resource);
            byte[] pdfBytes = PdfGenerator.generatePdfWithHeader(csvData);


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