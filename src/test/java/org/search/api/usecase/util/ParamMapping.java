package org.search.api.usecase.util;

public final class ParamMapping {

    private ParamMapping() {
    }

    public static final String ENDECA_SEARCH_TERMS             = "searchTerms";
    public static final String ENDECA_PAGE_SIZE                = "pageSize";
    public static final String PAGE_SIZE_DEFAULT               = "25";
    public static final String ENDECA_RECORD_IDS               = "mainContent.records.attributes.ID";
    public static final String ENDECA_INPUT_FILE_PATH          = "data/test-input.xlsx";
    public static final String ENDECA_INPUT_FILE_SHEET_NAME    = "search-terms";
    public static final String XLSX_EXTENSION                  = ".xlsx";
    public static final String RESULTS_OUTPUT_FILE_PATH_PREFIX = "data/";
    public static final String UNDERSCORE                      = "_";
    public static final String COMMA                           = ",";
    public static final String OUTPUT_FILE_HEADER_COL_1        = "search_terms";
    public static final String OUTPUT_FILE_HEADER_COL_2        = "product_id";
    public static final String OUTPUT_FILE_HEADER_COL_3        = "position";
}
