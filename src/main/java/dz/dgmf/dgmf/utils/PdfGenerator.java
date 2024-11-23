package dz.dgmf.dgmf.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfGenerator {

    public static byte[] generatePdfWithHeader(List<String[]> csvData) throws Exception {
        try (PDDocument document = new PDDocument()) {

            // Calculate the number of pages needed
            int rowsPerPage = 30; // Example: 30 rows per page
            int totalPages = (int) Math.ceil((double) csvData.size() / rowsPerPage);

            for (int pageNumber = 1; pageNumber <= totalPages; pageNumber++) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    drawHeader(contentStream, pageNumber, totalPages);
                    drawTable(contentStream, csvData, pageNumber, totalPages, rowsPerPage);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private static void drawHeader(PDPageContentStream contentStream, int pageNumber, int totalPages) throws Exception {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);

        float startX = 50;
        float startY = 750;

        String[] headerLines = {
                "MINISTERE DES FINANCES",
                "TRESORERIE DE TRESORERIE D'ALGER",
                "                    MOUVEMENTS DE LA JOURNEE DU 15/11/2022"
        };

        for (String line : headerLines) {
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.showText(line);
            contentStream.endText();
            startY -= 20;
        }

        // Add page number at the top right
        PDFont font = PDType1Font.HELVETICA; // Use the font object for getStringWidth
        contentStream.setFont(font, 12);
        String pageText = String.format("Page %d / %d", pageNumber, totalPages);
        float pageTextWidth = font.getStringWidth(pageText) / 1000 * 12; // Adjust multiplier as needed
        contentStream.beginText();
        contentStream.newLineAtOffset(550 - pageTextWidth, 750); // Adjust 550 for x-position
        contentStream.showText(pageText);
        contentStream.endText();
    }

    private static void drawTable(PDPageContentStream contentStream, List<String[]> csvData, int pageNumber, int totalPages, int rowsPerPage) throws Exception {
        float margin = 50;
        float yStart = 680;
        float yPosition = yStart;
        float tableWidth = 530; // You might need to adjust this
        float rowHeight = 25;
        float cellMargin = 5;

        String[] headers = {"NUM_MVT", "COMPTE_LIGNE", "POSTE", "NUMERAIRE", "BANCAIRE", "C.C.P", "OPER_ORDRE", "TOTAL_DU_MVT"};

        float[] colWidths = calculateColumnWidths(contentStream, csvData, headers);

        // 1. Draw the top horizontal line
        //drawHorizontalLine(contentStream, margin, yPosition, tableWidth);
        yPosition -= rowHeight; // Move to the next row

        drawDashedHorizontalLine(contentStream, margin, yPosition, tableWidth);
        // 2. Draw header row WITHOUT borders
        yPosition = drawRowWithoutBorders(contentStream, yPosition, margin, tableWidth, rowHeight, cellMargin, headers, colWidths, true);

        // 3. Draw the line below the header AND add vertical separators
        drawDashedHorizontalLine(contentStream, margin, yPosition, tableWidth);
        yPosition -= 3;
        drawDashedHorizontalLine(contentStream, margin, yPosition, tableWidth);
        float xPosition = margin;
        for (int i = 0; i < colWidths.length - 1; i++) {
            xPosition += colWidths[i];
            drawDashedVerticalLine(contentStream, xPosition, yPosition, yPosition + rowHeight);
        }

        // 4. Draw data rows WITHOUT borders and with dashed lines between them AND columns
        int startIndex = (pageNumber - 1) * rowsPerPage;
        int endIndex = Math.min(startIndex + rowsPerPage, csvData.size());
        for (int i = startIndex; i < endIndex; i++) {
            String[] row = csvData.get(i);
            xPosition = margin;
            yPosition = drawRowWithoutBorders(contentStream, yPosition, margin, tableWidth, rowHeight, cellMargin, row, colWidths, false);
            drawDashedHorizontalLine(contentStream, margin, yPosition, tableWidth); // Dashed line after each data row

            // Draw dashed vertical lines after each cell
            for (int j = 0; j < colWidths.length - 1; j++) { // -1 to avoid drawing a line after the last column
                xPosition += colWidths[j];
                drawDashedVerticalLine(contentStream, xPosition, yPosition, yPosition + rowHeight);
            }
        }
    }

    // Function to draw a row WITHOUT borders
    private static float drawRowWithoutBorders(PDPageContentStream contentStream, float yPosition, float margin, float tableWidth,
                                               float rowHeight, float cellMargin, String[] rowData, float[] colWidths, boolean isHeader) throws Exception {
        float xPosition = margin;
        contentStream.setFont(isHeader ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, 8); // Reduced font size

        for (int i = 0; i < rowData.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xPosition + cellMargin, yPosition - rowHeight + cellMargin);
            contentStream.showText(rowData[i] != null ? rowData[i] : "");
            contentStream.endText();

            xPosition += colWidths[i];
        }
        return yPosition - rowHeight;
    }

    private static float[] calculateColumnWidths(PDPageContentStream contentStream, List<String[]> csvData, String[] headers) throws IOException {
        float[] colWidths = new float[headers.length];
        PDFont font = PDType1Font.HELVETICA; // Or the font you are using

        // Calculate width for header cells
        for (int i = 0; i < headers.length; i++) {
            float width = font.getStringWidth(headers[i]) / 1000 * 10; // Adjust the multiplier as needed
            colWidths[i] = Math.max(colWidths[i], width);
        }

        // Calculate width for data cells
        for (String[] row : csvData) {
            for (int i = 0; i < row.length; i++) {
                float width = font.getStringWidth(row[i] != null ? row[i] : "") / 1000 * 10; // Adjust the multiplier as needed
                colWidths[i] = Math.max(colWidths[i], width);
            }
        }

        // Add cell margin to each column width
        for (int i = 0; i < colWidths.length; i++) {
            colWidths[i] += 10; // Add some padding
        }

        return colWidths;
    }

    private static void drawHorizontalLine(PDPageContentStream contentStream, float xStart, float yPosition, float width) throws IOException {
        contentStream.moveTo(xStart, yPosition);
        contentStream.lineTo(xStart + width, yPosition);
        contentStream.stroke();
    }

    private static void drawDashedHorizontalLine(PDPageContentStream contentStream, float xStart, float yPosition, float width) throws IOException {
        float dashLength = 5; // Length of each dash
        float spaceLength = 3; // Length of space between dashes

        contentStream.setLineDashPattern(new float[]{dashLength, spaceLength}, 0); // Set dash pattern

        contentStream.moveTo(xStart, yPosition);
        contentStream.lineTo(xStart + width, yPosition);
        contentStream.stroke();

        contentStream.setLineDashPattern(new float[]{}, 0); // Reset dash pattern to solid line
    }

    private static void drawDashedVerticalLine(PDPageContentStream contentStream, float xPosition, float yStart, float yEnd) throws IOException {
        float dashLength = 5; // Length of each dash
        float spaceLength = 3; // Length of space between dashes

        contentStream.setLineDashPattern(new float[]{dashLength, spaceLength}, 0); // Set dash pattern

        contentStream.moveTo(xPosition, yStart);
        contentStream.lineTo(xPosition, yEnd);
        contentStream.stroke();

        contentStream.setLineDashPattern(new float[]{}, 0); // Reset dash pattern to solid line
    }
}
