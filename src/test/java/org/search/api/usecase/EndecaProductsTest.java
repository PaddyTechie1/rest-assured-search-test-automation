package org.search.api.usecase;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.Test;
import org.search.api.gateway.excel.util.ExcelUtility;
import org.search.api.usecase.util.Messages;
import org.search.api.usecase.util.ParamMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/*
1. Reads the input file from project/data directory
2. for each row (search terms), a request is created
    1. request to search-api (endeca) is fired
    2. response is capture into a list
3.
*/

public class EndecaProductsTest {

    private static final Logger ENDECA_LOGGER = LoggerFactory.getLogger(EndecaProductsTest.class);
    private static final String BASE_URI      = "https://internal.acc.cloud.jumbo.com/v0/search";
    private static final String PATH          = "/P/products";
    private static final int    ONE           = 1;

    @Test
    public void testEndecaSearchResponseWithIds() {
        //make sure the current search terms
        final XSSFSheet sheetToRead = getSheetToRead();
        if (sheetToRead == null) {
            ENDECA_LOGGER.warn(Messages.EMPTY_SHEET);
        } else {
            final int totalRowCount = ExcelUtility.getRowCount(sheetToRead);
            executeTestsAndWriteToFile(sheetToRead, totalRowCount);
        }
    }

    private XSSFSheet getSheetToRead() {
        try {
            return ExcelUtility.getSheetToRead(ParamMapping.ENDECA_INPUT_FILE_PATH, ParamMapping.ENDECA_INPUT_FILE_SHEET_NAME);
        } catch (IOException ioException) {
            ENDECA_LOGGER.error(ioException.getMessage(), ioException);
        }
        return null;
    }

    private void executeTestsAndWriteToFile(final XSSFSheet sheetToRead, final int totalRowCount) {
        if (totalRowCount == ONE) {
            ENDECA_LOGGER.warn(Messages.SHEET_WITH_HEADER);
        } else {
            final List<String> resultsToWrite = new LinkedList<>();
            for (int rowNum = 1; rowNum < totalRowCount; rowNum++) {
                executeTestsForEachSearchTerms(sheetToRead, rowNum, resultsToWrite);
            }
            if (!resultsToWrite.isEmpty()) {
                writeResultsToFile(resultsToWrite);
            }
        }
    }

    //for better performance, make sure to remove empty rows in the input file
    private void executeTestsForEachSearchTerms(final XSSFSheet sheetToRead, final int rowNum, final List<String> resultsToWrite) {
        final String searchTerms = (String) ExcelUtility.getCellData(sheetToRead, rowNum, 0);
        if (!StringUtils.isEmpty(searchTerms)) {
            final RequestSpecification requestSpec = getRequestSpec(
                searchTerms);
            final Response response = RestAssured.given().spec(requestSpec).get();
            extractProductResultsList(response.body(), searchTerms, resultsToWrite);
        }
    }

    //request building - non-bdd way
    private RequestSpecification getRequestSpec(final String searchTerms) {
        return new RequestSpecBuilder()
            .setBaseUri(BASE_URI)
            .setBasePath(PATH)
            .addQueryParam(ParamMapping.ENDECA_SEARCH_TERMS, searchTerms)
            .addQueryParam(ParamMapping.ENDECA_PAGE_SIZE, ParamMapping.PAGE_SIZE_DEFAULT)
            .build();
    }

    private void extractProductResultsList(final ResponseBody responseBody, final String searchTerms, final List<String> resultsToWrite) {
        final List<Object> productIds = responseBody.jsonPath().get(ParamMapping.ENDECA_RECORD_IDS);
        if (productIds == null || productIds.isEmpty()) {
            resultsToWrite.add(searchTerms);
            return;
        } else {
            writeItemsIntoList(searchTerms, resultsToWrite, productIds);
        }
    }

    private void writeItemsIntoList(final String searchTerms, final List<String> resultsToWrite, final List<Object> productIds) {
        for (final Object idLists : productIds) {
            if (idLists instanceof List) {
                final AtomicInteger counter = new AtomicInteger();
                ((List<?>) idLists).forEach(item -> {
                    if (item instanceof List) {
                        resultsToWrite.add(searchTerms + ParamMapping.COMMA + ((List) item).get(0).toString() + ParamMapping.COMMA + counter.incrementAndGet());
                    }
                });
            } else {
                ENDECA_LOGGER.warn(Messages.RESPONSE_EMPTY);
            }
        }
    }

    private void writeResultsToFile(final List<String> resultsToWrite) {
        try {
            final String outputFilePath = ParamMapping.RESULTS_OUTPUT_FILE_PATH_PREFIX +
                getClass().getSimpleName() + ParamMapping.UNDERSCORE + new Date().getTime() + ParamMapping.XLSX_EXTENSION;
            ExcelUtility.writeToFile(resultsToWrite, outputFilePath);
        } catch (final IOException exception) {
            ENDECA_LOGGER.error(exception.getMessage(), exception);
        }
    }
}
