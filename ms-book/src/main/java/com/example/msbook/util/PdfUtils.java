/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.util;

import com.example.msbook.entity.Libro;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PdfUtils {

    private PdfUtils() {
    }

    public static ByteArrayOutputStream generatePdfStream(List<Libro> books) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

        for (Libro book : books) {
            Paragraph paragraph = new Paragraph(book.getTitulo(), boldFont);
            document.add(paragraph);
        }

        document.add(new Paragraph("\n"));
        document.close();

        return outputStream;
    }
}