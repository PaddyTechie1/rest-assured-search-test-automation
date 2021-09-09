package org.search.api.gateway.excel.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.search.api.usecase.util.ParamMapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        final XSSFWorkbook workbook = new XSSFWorkbook(file.getPath());
        return workbook.getSheet(sheetName);
    }

    public static void writeToFile(final List<String> resultsToWrite, final String fileName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet();
            workbook.createSheet();
            int rowCount = 0;
            createHeaderRow(sheet, rowCount);
            rowCount++;
            for (final String item : resultsToWrite) {
                final Row row = sheet.createRow(rowCount++);
                writeToSheet(item, row);
            }
            final File outputFile = new File(fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
    }

    private static void createHeaderRow(final Sheet sheet, final int rowCount) {
        final Row headerRow = sheet.createRow(rowCount);
        headerRow.createCell(0).setCellValue(ParamMapping.OUTPUT_FILE_HEADER_COL_1);
        headerRow.createCell(1).setCellValue(ParamMapping.OUTPUT_FILE_HEADER_COL_2);
        headerRow.createCell(2).setCellValue(ParamMapping.OUTPUT_FILE_HEADER_COL_3);
    }

    private static void writeToSheet(final String itemToWrite, final Row row) {
        if (itemToWrite.contains(ParamMapping.COMMA)) {
            final String[] results = itemToWrite.split(ParamMapping.COMMA);
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
            default:
                //ignore
        }
    }

    private static void writeToCell(final Cell cell, final String item) {
        cell.setCellValue(item);
    }
}
