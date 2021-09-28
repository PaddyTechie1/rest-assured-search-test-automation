package org.search.api.usecase.util;

public final class MessageConstants {

    public static final String EMPTY_SHEET               = "Input sheet is empty, cannot proceed with the test";
    public static final String SHEET_WITH_HEADER         = "Input sheet is present only with header, please add the relevant content to proceed with the test";
    public static final String START_TIME                = "startTime : ";
    public static final String END_TIME                  = "EndTime : ";
    public static final String SEARCH_API_REQUEST_START  = "Start:: Search request for ==> ";
    public static final String SEARCH_API_REQUEST_FINISH = "End:: Search request for ==> ";
    public static final String API_RESPONSE_TIME         = "Api response time ==> ";
    public static final String EMPTY_ROW_PROCESSING      = "processing empty row from the input file. hence no request fired to search vendor";

    private MessageConstants() {
    }
}
