package org.search.api.usecase.helper;

import com.google.common.base.Strings;
import io.restassured.response.ResponseBody;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.search.api.gateway.excel.util.ExcelUtility;
import org.search.api.usecase.util.ParameterConstants;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class SearchAndBrowseHelper {

    private SearchAndBrowseHelper() {
    }

    public static XSSFSheet getSheetToRead(final Logger logger) {
        try {
            return ExcelUtility.getSheetToRead(ParameterConstants.TEST_INPUT_FILE_PATH, ParameterConstants.TEST_INPUT_FILE_SHEET_NAME);
        } catch (IOException ioException) {
            logger.error(ioException.getMessage(), ioException);
        }
        return null;
    }

    public static void writeResultsToFile(final String className, final Logger logger, final List<String> resultsToWrite) {
        try {
            final String outputFilePath = ParameterConstants.TEST_OUTPUT_FILE_PATH_PREFIX +
                className + ParameterConstants.UNDERSCORE + new Date().getTime() + ParameterConstants.XLSX_EXTENSION;
            ExcelUtility.writeToFile(resultsToWrite, outputFilePath);
        } catch (final IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
    }

    //writes searchTerms, productId, productName & position from the response.
    public static void extractProductResultsList(final ResponseBody<?> responseBody, final String searchTerms,
        final List<String> resultsToWrite, final String responsePatterForId, final String idField, final String nameField) {
        final List<Map<String, String>> products = responseBody.jsonPath().getList(responsePatterForId);
        if (products == null || products.isEmpty()) {
            resultsToWrite.add(searchTerms);
            return;
        } else {
            writeRecordsIntoList(searchTerms, resultsToWrite, idField, nameField, products);
        }
    }

    private static void writeRecordsIntoList(final String searchTerms, final List<String> resultsToWrite, final String idField, final String nameField,
        final List<Map<String, String>> products) {
        final AtomicInteger counter = new AtomicInteger();
        products.forEach(
            product -> {
                final StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append(searchTerms);
                buildNameAndId(product, resultBuilder, idField, nameField);
                resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(counter.incrementAndGet());
                resultsToWrite.add(resultBuilder.toString());
            });
    }

    private static void buildNameAndId(final Map<String, String> product, final StringBuilder resultBuilder, final String idField, final String nameField) {
        if (product.containsKey(idField)) {
            resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(product.get(idField));
        }

        if (product.containsKey(nameField)) {
            resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(product.get(nameField));
        }
    }

    //writes searchTerms, productId, productName & position from the response.
    public static void extractProductPrecisionResults(final ResponseBody<?> responseBody, final String searchTerms, final List<String> resultsToWrite,
        final String responsePatterForId, final String idField, final String nameField, final List<String> productsToCheck) {
        final List<Map<String, String>> products = responseBody.jsonPath().getList(responsePatterForId);
        if (products == null || products.isEmpty()) {
            resultsToWrite.add(searchTerms);
            return;
        } else {
            buildResultsToWrite(searchTerms, resultsToWrite, idField, nameField, productsToCheck, products);
        }
    }

    private static void buildResultsToWrite(final String searchTerms, final List<String> resultsToWrite, final String idField, final String nameField,
        final List<String> productsToCheck, final List<Map<String, String>> products) {
        final List<String> resultsForSearchTerm = new LinkedList<>();
        writeMatchingRecordsToList(searchTerms, resultsForSearchTerm, idField, nameField, products, productsToCheck);
        if (productsToCheck.size() != resultsForSearchTerm.size()) {
            final List<String> productsNotInResponse = getMissingProductsFromResponse(productsToCheck, resultsForSearchTerm);
            buildMissingProductIntoResults(searchTerms, resultsForSearchTerm, productsNotInResponse);
        }
        resultsToWrite.addAll(resultsForSearchTerm);
    }

    private static List<String> getMissingProductsFromResponse(final List<String> productsToCheck, final List<String> resultsForSearchTerm) {
        final List<String> productIdsFromResponse = new ArrayList<>(resultsForSearchTerm.size());
        resultsForSearchTerm.forEach(result -> {
            final String[] results = result.split(ParameterConstants.ESCAPE_CHARS + ParameterConstants.VERTICAL_BAR);
            productIdsFromResponse.add(results[1]);
        });
        return productsToCheck
            .stream()
            .filter(item -> !productIdsFromResponse.contains(item))
            .collect(Collectors.toList());
    }

    private static void buildMissingProductIntoResults(final String searchTerms, final List<String> resultsForSearchTerm, final List<String> productsNotInResponse) {
        productsNotInResponse.forEach(productNotInResponse ->
            resultsForSearchTerm.add(searchTerms + ParameterConstants.VERTICAL_BAR + productNotInResponse + ParameterConstants.VERTICAL_BAR
                + ParameterConstants.VERTICAL_BAR + ParameterConstants.DEFAULT_POSITION));
    }

    private static void writeMatchingRecordsToList(final String searchTerms, final List<String> resultsToWrite, final String idField, final String nameField,
        final List<Map<String, String>> products, final List<String> productsToCheck) {
        final AtomicInteger counter = new AtomicInteger();
        products.forEach(
            product -> {
                final int position = counter.incrementAndGet();
                if (productsToCheck.contains(product.get(idField))) {
                    final StringBuilder resultBuilder = new StringBuilder();
                    resultBuilder.append(searchTerms);
                    buildNameAndId(product, resultBuilder, idField, nameField);
                    resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(position);
                    resultsToWrite.add(resultBuilder.toString());
                }
            });
    }

    public static List<String> getProductsForPrecisionTest(final XSSFSheet sheetToRead, final int rowNum) {
        final String productIdsWithPosition = (String) ExcelUtility.getCellData(sheetToRead, rowNum, 1);
        return Strings.isNullOrEmpty(productIdsWithPosition) ? Collections.emptyList() :
            Arrays.asList(productIdsWithPosition.split(ParameterConstants.ESCAPE_CHARS + ParameterConstants.VERTICAL_BAR));
    }

    //enable to write searchTerms, productId & position
    /*public static void extractProductIdsFromResponse(final ResponseBody responseBody, final String searchTerms, final List<String> resultsToWrite, final String responsePatterForId) {
        final List<String> productIds = responseBody.jsonPath().get(responsePatterForId);
        if (productIds == null || productIds.isEmpty()) {
            resultsToWrite.add(searchTerms);
            return;
        } else {
            writeItemsIntoList(searchTerms, resultsToWrite, productIds);
        }
    }

    private static void writeProductIdsIntoList(final String searchTerms, final List<String> resultsToWrite, final List<String> productIds) {
        final AtomicInteger counter = new AtomicInteger();
        productIds.forEach(productId -> resultsToWrite.add(searchTerms + ParameterConstants.COMMA + productId + ParameterConstants.COMMA + counter.incrementAndGet()));
    }*/
}
