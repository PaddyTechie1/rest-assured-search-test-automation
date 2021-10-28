package org.search.api.usecase;

import com.google.common.util.concurrent.RateLimiter;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.Assert;
import org.junit.Test;
import org.search.api.gateway.excel.util.ExcelUtility;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/*
- input to the test is an excel with search terms (column 1) and a pipe separated product (column 2) in a row
- default page size of 24 is set with the query
- output is a xlsx file containing the 5 products position in the response.
    - if the product is present in the response, the respective position from the response will be assigned
    - if the product is not present in the response, a default value of 30 is assigned
*/

@SuppressWarnings("UnstableApiUsage")
public class EndecaPrecisionTest {

    private static final Logger ENDECA_LOGGER = LoggerFactory.getLogger(EndecaPrecisionTest.class);

    // just in case the request and response log to be enabled. uncomment the below code
   /* @Before
    public void setUp() throws FileNotFoundException {
        PrintStream fileOutPutStream = new PrintStream(new File("logs/results.log"));
        config = config().logConfig(new LogConfig().defaultStream(fileOutPutStream));
    }*/

    @Test
    public void searchPrecisionTestForLimitedProducts() {
        //make sure the current search terms
        final XSSFSheet sheetToRead = SearchAndBrowseHelper.getSheetToRead(ENDECA_LOGGER);
        if (sheetToRead == null) {
            ENDECA_LOGGER.warn(MessageConstants.EMPTY_SHEET);
        } else {
            final int totalRowCount = ExcelUtility.getRowCount(sheetToRead);
            executeTestsAndWriteToFile(sheetToRead, totalRowCount);
            Assert.assertTrue(totalRowCount > 1);
        }
    }

    private void executeTestsAndWriteToFile(final XSSFSheet sheetToRead, final int totalRowCount) {
        if (totalRowCount == ParameterConstants.ONE) {
            ENDECA_LOGGER.warn(MessageConstants.SHEET_WITH_HEADER);
        } else {
            final List<String> resultsToWrite = getResultsToWrite(sheetToRead, totalRowCount);
            if (!resultsToWrite.isEmpty()) {
                SearchAndBrowseHelper.writeResultsToFile(getClass().getSimpleName(), ENDECA_LOGGER, resultsToWrite);
            }
        }
    }

    private List<String> getResultsToWrite(final XSSFSheet sheetToRead, final int totalRowCount) {
        final List<String> resultsToWrite = new LinkedList<>();
        final RateLimiter rateLimiter = RateLimiter.create(3.0);
        final String fileToWrite = LoggerHelper.getFileToWrite(ParameterConstants.ENDECA);
        LoggerHelper.log(ENDECA_LOGGER, fileToWrite, MessageConstants.START_TIME + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        for (int rowNum = 1; rowNum < totalRowCount; rowNum++) {
            rateLimiter.acquire(1);
            executeTestsForEachSearchTerms(sheetToRead, rowNum, resultsToWrite, fileToWrite);
        }
        LoggerHelper.log(ENDECA_LOGGER, fileToWrite, MessageConstants.END_TIME + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        return resultsToWrite;
    }

    //for better performance, make sure to remove empty rows in the input file
    private void executeTestsForEachSearchTerms(final XSSFSheet sheetToRead, final int rowNum, final List<String> resultsToWrite, final String fileName) {
        final String searchTerms = (String) ExcelUtility.getCellData(sheetToRead, rowNum, 0);
        final List<String> productsForPrecisionTest = SearchAndBrowseHelper.getProductsForPrecisionTest(sheetToRead, rowNum);
        LoggerHelper.log(ENDECA_LOGGER, fileName, MessageConstants.SEARCH_API_REQUEST_START + searchTerms);

        if (!StringUtils.isEmpty(searchTerms)) {
            final RequestSpecification requestSpec = getRequestSpec(searchTerms);
            final long startTime = System.currentTimeMillis();
            final Response response = RestAssured.given().spec(requestSpec).get();
            final long endTime = System.currentTimeMillis();
            LoggerHelper.log(ENDECA_LOGGER, fileName, MessageConstants.API_RESPONSE_TIME + (endTime - startTime));
            //uncomment if logging to be done about request & response.
            //requestSpec.log().all()
            extractProductPrecisionResults(response.body(), searchTerms, resultsToWrite, productsForPrecisionTest);
            LoggerHelper.log(ENDECA_LOGGER, fileName, MessageConstants.SEARCH_API_REQUEST_FINISH + searchTerms);
        } else {
            LoggerHelper.log(ENDECA_LOGGER, fileName, MessageConstants.EMPTY_ROW_PROCESSING);
        }
    }

    //request building - non-bdd way
    private RequestSpecification getRequestSpec(final String searchTerms) {
        return new RequestSpecBuilder()
            .setBaseUri(ParameterConstants.ENDECA_BASE_URI)
            .setBasePath(ParameterConstants.ENDECA_PRODUCT_END_POINT)
            .addQueryParam(ParameterConstants.ENDECA_SEARCH_TERM_PARAM, searchTerms)
            .addQueryParam(ParameterConstants.ENDECA_PAGE_SIZE_PARAM, ParameterConstants.PAGE_SIZE_VALUE)
            .build();
    }

    private void extractProductPrecisionResults(final ResponseBody<?> responseBody, final String searchTerms, final List<String> resultsToWrite
        , final List<String> productsToCheck) {
        final List<List<Map<String, Object>>> products = responseBody.jsonPath().getList(ParameterConstants.ENDECA_RECORDS);
        if (products == null || products.isEmpty()) {
            resultsToWrite.add(searchTerms);
            return;
        } else {
            buildResultsToWrite(searchTerms, resultsToWrite, productsToCheck, ParameterConstants.ENDECA_RECORD_ID_FIELD, products.get(0));
        }
    }

    private static void buildResultsToWrite(final String searchTerms, final List<String> resultsToWrite, final List<String> productsToCheck,
        final String idField, final List<Map<String, Object>> products) {
        final List<String> resultsForSearchTerm = new LinkedList<>();
        writeMatchingRecordsToList(searchTerms, resultsForSearchTerm, products, productsToCheck, idField);
        if (productsToCheck.size() != resultsForSearchTerm.size()) {
            final List<String> productsNotInResponse = SearchAndBrowseHelper.getMissingProductsFromResponse(productsToCheck, resultsForSearchTerm);
            SearchAndBrowseHelper.buildMissingProductIntoResults(searchTerms, resultsForSearchTerm, productsNotInResponse);
        }
        resultsToWrite.addAll(resultsForSearchTerm);
    }

    private static void writeMatchingRecordsToList(final String searchTerms, final List<String> resultsToWrite, final List<Map<String, Object>> products,
        final List<String> productsToCheck, final String idField) {
        final AtomicInteger counter = new AtomicInteger();
        products.forEach(
            product -> {
                final int position = counter.incrementAndGet();
                if (productsToCheck.contains(((List<?>) product.get(idField)).get(0))) {
                    final StringBuilder resultBuilder = new StringBuilder();
                    resultBuilder.append(searchTerms);
                    buildNameAndId(product, resultBuilder, idField);
                    resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(position);
                    resultsToWrite.add(resultBuilder.toString());
                }
            });
    }

    private static void buildNameAndId(final Map<String, Object> product, final StringBuilder resultBuilder, final String idField) {
        if (product.containsKey(idField)) {
            resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(((List<?>) product.get(idField)).get(0));
        }

        if (product.containsKey(ParameterConstants.ENDECA_RECORD_TITLE_FIELD)) {
            resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(((List<?>) product.get(ParameterConstants.ENDECA_RECORD_TITLE_FIELD)).get(0));
        }
    }
}
