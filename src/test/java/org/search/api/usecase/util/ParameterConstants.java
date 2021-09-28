package org.search.api.usecase.util;

public final class ParameterConstants {

    //Common
    public static final String TEST_INPUT_FILE_PATH         = "data/test-input.xlsx";
    public static final String TEST_INPUT_FILE_SHEET_NAME   = "search-terms";
    public static final String XLSX_EXTENSION               = ".xlsx";
    public static final String TEST_OUTPUT_FILE_PATH_PREFIX = "data/";
    public static final String UNDERSCORE                   = "_";
    public static final String VERTICAL_BAR                 = "|";
    public static final String ESCAPE_CHARS                 = "\\";
    public static final String OUTPUT_FILE_HEADER_COL_1     = "search_terms";
    public static final String OUTPUT_FILE_HEADER_COL_2     = "product_id";
    public static final String OUTPUT_FILE_HEADER_COL_3     = "product_name";
    public static final String OUTPUT_FILE_HEADER_COL_4     = "position";
    public static final String LOG_FILE_PATH_PREFIX         = "logs/test";
    public static final String LOG_FILE_EXTENSION           = ".log";
    public static final String PAGE_SIZE_VALUE              = "5";
    public static final int    ONE                          = 1;
    public static final int    ZERO                         = 0;
    public static final String PAGE_SIZE_PARAM              = "rows";
    public static final String OFFSET_PARAM                 = "start";
    public static final String DEFAULT_POSITION             = "30";
    //ENDECA specifics
    public static final String ENDECA                       = "endeca";
    public static final String ENDECA_BASE_URI              = "https://search-api.internal.acc.cloud.jumbo.com/v0/search";
    public static final String ENDECA_PRODUCT_END_POINT     = "/P/products";
    public static final String ENDECA_SEARCH_TERM_PARAM     = "searchTerms";
    public static final String ENDECA_PAGE_SIZE_PARAM       = "pageSize";
    public static final String ENDECA_RECORDS               = "mainContent.records.attributes";
    public static final String ENDECA_RECORD_ID_FIELD       = "ID";
    public static final String ENDECA_RECORD_TITLE_FIELD    = "P_Title";
    //Empathy-specifics
    public static final String EMPATHY                      = "empathy";
    public static final String EMPATHY_BASE_URI             = "https://api.staging.empathy.co/search/v1/query/jumbo";
    public static final String EMPATHY_SEARCH_END_POINT     = "search";
    public static final String EMPATHY_QUERY_PARAM          = "query";
    public static final String EMPATHY_SCOPE_PARAM          = "scope";
    public static final String EMPATHY_SCOPE_DESKTOP        = "desktop";
    public static final String EMPATHY_LANG_PARAM           = "lang";
    public static final String EMPATHY_LANG_NL              = "nl";
    public static final String EMPATHY_RECORDS              = "catalog.content";
    public static final String EMPATHY_ID_FIELD             = "sku";
    public static final String EMPATHY_NAME_FIELD           = "name";
    //BRSM - bloomreach search & merchandise specifics
    public static final String BRSM                         = "brsm";
    public static final String BRSM_BASE_URI                = "https://core.dxpapi.com/api/v1";
    public static final String BRSM_SEARCH_CORE_PATH        = "/core/";
    public static final String BRSM_ACCOUNT_ID_PARAM        = "account_id";
    public static final String BRSM_ACCOUNT_ID              = "6614";
    public static final String BRSM_AUTH_KEY_PARAM          = "auth_key";
    public static final String BRSM_AUTH_KEY                = "z8c57nr1wricspbl";
    public static final String BRSM_DOMAIN_KEY_PARAM        = "domain_key";
    public static final String BRSM_DOMAIN_KEY              = "demo_jumbo_nl";
    public static final String BRSM_REQUEST_ID_PARAM        = "request_id";
    public static final String BRSM_REQUEST_ID              = "109895450840987";
    public static final String BRSM_BR_UID_2_PARAM          = "_br_uid_2";
    public static final String BRSM_BR_UID_2                = "uid%3D71518092079876%3Av%3D12.8%3Ats%3D1627296331894%3Ahc%3D44";
    public static final String BRSM_URL_PARAM               = "url";
    public static final String BRSM_URL                     = "www.jumbo.com";
    public static final String BRSM_REF_URL_PARAM           = "ref_url";
    public static final String BRSM_REQUEST_TYPE_PARAM      = "request_type";
    public static final String BRSM_REQUEST_TYPE_SEARCH     = "search";
    public static final String BRSM_QUERY_PARAM             = "q";
    public static final String BRSM_SEARCH_TYPE_PARAM       = "search_type";
    public static final String BRSM_SEARCH_TYPE_KEYWORD     = "keyword";
    public static final String BRSM_RECORDS                 = "response.docs";
    public static final String BRSM_FIELDS_PARAM            = "fl";
    public static final String BRSM_FIELDS_LIST             = "title,pid";
    public static final String BRSM_ID_FIELD                = "pid";
    public static final String BRSM_NAME_FIELD              = "title";

    private ParameterConstants() {
    }
}
