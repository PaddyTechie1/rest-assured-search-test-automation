package org.search.api.gateway.excel.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.search.api.usecase.util.ParameterConstants;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class ExcelUtility {

    private ExcelUtility() {
    }

    public static int getRowCount(final XSSFSheet sheet) {
        return sheet.getPhysicalNumberOfRows();
    }

    public static Object getCellData(final XSSFSheet sheet, final int rowNum, final int colNum) {
        final DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(sheet.getRow(rowNum).getCell(colNum));
    }

    //for better performance, make sure to remove empty rows in the input file
    public static XSSFSheet getSheetToRead(final String filePath, final String sheetName) throws IOException {
        final File file = new File(filePath);
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getPath())) {
            return workbook.getSheet(sheetName);
        }
    }

    public static void writeToFile(final List<String> resultsToWrite, final String fileName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet();
            workbook.createSheet();
            int rowCount = 0;
            createHeaderRow(sheet, rowCount);
            for (final String item : resultsToWrite) {
                final Row row = sheet.createRow(++rowCount);
                writeToSheet(item, row);
            }
            try (OutputStream fos = Files.newOutputStream(Paths.get(fileName))) {
                workbook.write(fos);
            }
        }
    }

    private static void createHeaderRow(final Sheet sheet, final int rowCount) {
        final Row headerRow = sheet.createRow(rowCount);
        headerRow.createCell(0).setCellValue(ParameterConstants.OUTPUT_FILE_HEADER_COL_1);
        headerRow.createCell(1).setCellValue(ParameterConstants.OUTPUT_FILE_HEADER_COL_2);
        headerRow.createCell(2).setCellValue(ParameterConstants.OUTPUT_FILE_HEADER_COL_3);
        headerRow.createCell(3).setCellValue(ParameterConstants.OUTPUT_FILE_HEADER_COL_4);
    }

    private static void writeToSheet(final String itemToWrite, final Row row) {
        if (itemToWrite.contains(ParameterConstants.VERTICAL_BAR)) {
            final String[] results = itemToWrite.split(ParameterConstants.ESCAPE_CHARS + ParameterConstants.VERTICAL_BAR);
            for (int index = 0; index < results.length; index++) {
                writeToCell(row, results, index);
            }
        } else {
            row.createCell(0).setCellValue(itemToWrite);
        }
    }

    private static void writeToCell(final Row row, final String[] results, final int index) {
        switch (index) {
            case 0:
                writeToCell(row.createCell(index), results[index]);
            case 1:
                writeToCell(row.createCell(index), results[index]);
            case 2:
                writeToCell(row.createCell(index), results[index]);
            case 3:
                writeToCell(row.createCell(index), results[index]);
            default:
                //ignore
        }
    }

    private static void writeToCell(final Cell cell, final String item) {
        cell.setCellValue(item);
    }
}
