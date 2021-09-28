package org.search.api.usecase;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.RateLimiter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.Assert;
import org.junit.Test;
import org.search.api.gateway.excel.util.ExcelUtility;
import org.search.api.usecase.helper.BRSMHelper;
import org.search.api.usecase.helper.LoggerHelper;
import org.search.api.usecase.helper.SearchAndBrowseHelper;
import org.search.api.usecase.util.MessageConstants;
import org.search.api.usecase.util.ParameterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/*
- input to the test is an excel with search terms (column 1) and a pipe separated product (column 2) in a row
- default page size of 24 is set with the query
- output is a xlsx file containing the 5 products position in the response.
    - if the product is present in the response, the respective position from the response will be assigned
    - if the product is not present in the response, a default value of 30 is assigned
*/

@SuppressWarnings("UnstableApiUsage")
public class BRSMPrecisionTest {

    private static final Logger BRSM_LOGGER = LoggerFactory.getLogger(BRSMPrecisionTest.class);

    @Test
    public void searchPrecisionTestForLimitedProducts() {
        //make sure the current search terms
        final XSSFSheet sheetToRead = SearchAndBrowseHelper.getSheetToRead(BRSM_LOGGER);
        if (sheetToRead == null) {
            BRSM_LOGGER.warn(MessageConstants.EMPTY_SHEET);
        } else {
            final int totalRowCount = ExcelUtility.getRowCount(sheetToRead);
            executeTestsAndWriteToFile(sheetToRead, totalRowCount);
            Assert.assertTrue(totalRowCount > 1);
        }
    }

    private void executeTestsAndWriteToFile(final XSSFSheet sheetToRead, final int totalRowCount) {
        if (totalRowCount == ParameterConstants.ONE) {
            BRSM_LOGGER.warn(MessageConstants.SHEET_WITH_HEADER);
        } else {
            final List<String> resultsToWrite = getResultsToWrite(sheetToRead, totalRowCount);
            if (!resultsToWrite.isEmpty()) {
                SearchAndBrowseHelper.writeResultsToFile(getClass().getSimpleName(), BRSM_LOGGER, resultsToWrite);
            }
        }
    }

    private List<String> getResultsToWrite(final XSSFSheet sheetToRead, final int totalRowCount) {
        final List<String> resultsToWrite = new LinkedList<>();
        final RateLimiter rateLimiter = RateLimiter.create(2.0);
        final String fileToWrite = LoggerHelper.getFileToWrite(ParameterConstants.BRSM);
        LoggerHelper.log(BRSM_LOGGER, fileToWrite, MessageConstants.START_TIME + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        for (int rowNum = 1; rowNum < totalRowCount; rowNum++) {
            rateLimiter.acquire(1);
            executeTestsForEachSearchTerms(sheetToRead, rowNum, resultsToWrite, fileToWrite);
        }
        LoggerHelper.log(BRSM_LOGGER, fileToWrite, MessageConstants.END_TIME + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        return resultsToWrite;
    }

    //for better performance, make sure to remove empty rows in the input file
    private void executeTestsForEachSearchTerms(final XSSFSheet sheetToRead, final int rowNum, final List<String> resultsToWrite, final String fileName) {
        final String searchTerms = (String) ExcelUtility.getCellData(sheetToRead, rowNum, 0);
        final List<String> productsForPrecisionTest = SearchAndBrowseHelper.getProductsForPrecisionTest(sheetToRead, rowNum);

        LoggerHelper.log(BRSM_LOGGER, fileName, MessageConstants.SEARCH_API_REQUEST_START + searchTerms);

        if (!Strings.isNullOrEmpty(searchTerms)) {
            final RequestSpecification requestSpec = BRSMHelper.getSearchRequestSpec(searchTerms);
            final long startTime = System.currentTimeMillis();
            final Response response = RestAssured.given().spec(requestSpec).get();
            final long endTime = System.currentTimeMillis();
            LoggerHelper.log(BRSM_LOGGER, fileName, MessageConstants.API_RESPONSE_TIME + (endTime - startTime));
            SearchAndBrowseHelper.extractProductPrecisionResults(response.body(), searchTerms, resultsToWrite, ParameterConstants.BRSM_RECORDS,
                ParameterConstants.BRSM_ID_FIELD, ParameterConstants.BRSM_NAME_FIELD, productsForPrecisionTest);
            LoggerHelper.log(BRSM_LOGGER, fileName, MessageConstants.SEARCH_API_REQUEST_FINISH + searchTerms);
        } else {
            LoggerHelper.log(BRSM_LOGGER, fileName, MessageConstants.EMPTY_ROW_PROCESSING);
        }
    }
}
