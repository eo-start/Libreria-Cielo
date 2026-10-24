/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.util;

import com.example.msbook.entity.Libro;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class UserExcelExporter {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Libro> books;

    public UserExcelExporter(List<Libro> books) {
        this.books = books;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Users");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        createCell(row, 0, "ID", style);
        createCell(row, 1, "Nombre", style);
    }

    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnIndex);
        Cell cell = row.createCell(columnIndex);

        if (value instanceof Integer integerValue) {
            cell.setCellValue(integerValue);
        } else if (value instanceof Boolean booleanValue) {
            cell.setCellValue(booleanValue);
        } else {
            cell.setCellValue(String.valueOf(value));
        }

        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Libro book : books) {
            Row row = sheet.createRow(rowCount++);

            createCell(row, 0, book.getId(), style);
            createCell(row, 1, book.getTitulo(), style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}