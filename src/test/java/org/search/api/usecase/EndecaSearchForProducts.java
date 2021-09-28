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
1. Reads the input file from project/data directory
2. for each row (search terms), a request is created
    1. request to search-api (endeca) is fired
    2. writes some information to the log file for later reference
    3. response is captured into a list
    4. measures the response time in milliseconds
3. writes the content from list into a file (excel)
*/

@SuppressWarnings("UnstableApiUsage")
public class EndecaSearchForProducts {

    private static final Logger ENDECA_LOGGER = LoggerFactory.getLogger(EndecaSearchForProducts.class);

    // just in case the request and response log to be enabled. uncomment the below code
   /* @Before
    public void setUp() throws FileNotFoundException {
        PrintStream fileOutPutStream = new PrintStream(new File("logs/results.log"));
        config = config().logConfig(new LogConfig().defaultStream(fileOutPutStream));
    }*/

    @Test
    public void searchForProductsWithValidInput() {
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
            final List<String> resultsToWrite = new LinkedList<>();
            final RateLimiter rateLimiter = RateLimiter.create(2.0);
            final String fileToWrite = LoggerHelper.getFileToWrite(ParameterConstants.ENDECA);
            LoggerHelper.log(ENDECA_LOGGER, fileToWrite, MessageConstants.START_TIME + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            for (int rowNum = 1; rowNum < totalRowCount; rowNum++) {
                rateLimiter.acquire(1);
                executeTestsForEachSearchTerms(sheetToRead, rowNum, resultsToWrite, fileToWrite);
            }
            LoggerHelper.log(ENDECA_LOGGER, fileToWrite, MessageConstants.END_TIME + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            if (!resultsToWrite.isEmpty()) {
                SearchAndBrowseHelper.writeResultsToFile(getClass().getSimpleName(), ENDECA_LOGGER, resultsToWrite);
            }
        }
    }

    //for better performance, make sure to remove empty rows in the input file
    private void executeTestsForEachSearchTerms(final XSSFSheet sheetToRead, final int rowNum, final List<String> resultsToWrite, final String fileName) {
        final String searchTerms = (String) ExcelUtility.getCellData(sheetToRead, rowNum, 0);
        LoggerHelper.log(ENDECA_LOGGER, fileName, MessageConstants.SEARCH_API_REQUEST_START + searchTerms);

        if (!StringUtils.isEmpty(searchTerms)) {
            final RequestSpecification requestSpec = getRequestSpec(searchTerms);
            final long startTime = System.currentTimeMillis();
            final Response response = RestAssured.given().spec(requestSpec).get();
            final long endTime = System.currentTimeMillis();
            LoggerHelper.log(ENDECA_LOGGER, fileName, MessageConstants.API_RESPONSE_TIME + (endTime - startTime));
            //uncomment if logging to be done about request & response.
            //requestSpec.log().all()
            extractProductResultsList(response.body(), searchTerms, resultsToWrite);
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

    private void extractProductResultsList(final ResponseBody<?> responseBody, final String searchTerms, final List<String> resultsToWrite) {
        final List<List<Map<String, Object>>> products = responseBody.jsonPath().getList(ParameterConstants.ENDECA_RECORDS);
        if (products == null || products.isEmpty()) {
            resultsToWrite.add(searchTerms);
            return;
        } else {
            writeRecordsIntoList(searchTerms, resultsToWrite, products.get(0));
        }
    }

    private static void writeRecordsIntoList(final String searchTerms, final List<String> resultsToWrite, final List<Map<String, Object>> products) {
        final AtomicInteger counter = new AtomicInteger();
        products.forEach(
            product -> {
                final StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append(searchTerms);
                buildNameAndId(product, resultBuilder);
                resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(counter.incrementAndGet());
                resultsToWrite.add(resultBuilder.toString());
            });
    }

    private static void buildNameAndId(final Map<String, Object> product, final StringBuilder resultBuilder) {
        if (product.containsKey(ParameterConstants.ENDECA_RECORD_ID_FIELD)) {
            resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(((List<?>) product.get(ParameterConstants.ENDECA_RECORD_ID_FIELD)).get(0));
        }

        if (product.containsKey(ParameterConstants.ENDECA_RECORD_TITLE_FIELD)) {
            resultBuilder.append(ParameterConstants.VERTICAL_BAR).append(((List<?>) product.get(ParameterConstants.ENDECA_RECORD_TITLE_FIELD)).get(0));
        }
    }
}
