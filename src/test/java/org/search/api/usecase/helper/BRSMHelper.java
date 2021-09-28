package org.search.api.usecase.helper;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.search.api.usecase.util.ParameterConstants;

public final class BRSMHelper {

    private BRSMHelper() {

    }

    //request building - non-bdd way
    public static RequestSpecification getSearchRequestSpec(final String searchTerms) {
        return new RequestSpecBuilder()
            .setBaseUri(ParameterConstants.BRSM_BASE_URI)
            .setBasePath(ParameterConstants.BRSM_SEARCH_CORE_PATH)
            .addQueryParam(ParameterConstants.PAGE_SIZE_PARAM, ParameterConstants.PAGE_SIZE_VALUE)
            .addQueryParam(ParameterConstants.OFFSET_PARAM, ParameterConstants.ZERO)
            .addQueryParam(ParameterConstants.BRSM_ACCOUNT_ID_PARAM, ParameterConstants.BRSM_ACCOUNT_ID)
            .addQueryParam(ParameterConstants.BRSM_AUTH_KEY_PARAM, ParameterConstants.BRSM_AUTH_KEY)
            .addQueryParam(ParameterConstants.BRSM_DOMAIN_KEY_PARAM, ParameterConstants.BRSM_DOMAIN_KEY)
            .addQueryParam(ParameterConstants.BRSM_REQUEST_ID_PARAM, ParameterConstants.BRSM_REQUEST_ID)
            .addQueryParam(ParameterConstants.BRSM_BR_UID_2_PARAM, ParameterConstants.BRSM_BR_UID_2)
            .addQueryParam(ParameterConstants.BRSM_URL_PARAM, ParameterConstants.BRSM_URL)
            .addQueryParam(ParameterConstants.BRSM_REF_URL_PARAM, ParameterConstants.BRSM_URL)
            .addQueryParam(ParameterConstants.BRSM_REQUEST_TYPE_PARAM, ParameterConstants.BRSM_REQUEST_TYPE_SEARCH)
            .addQueryParam(ParameterConstants.BRSM_QUERY_PARAM, searchTerms)
            .addQueryParam(ParameterConstants.BRSM_SEARCH_TYPE_PARAM, ParameterConstants.BRSM_SEARCH_TYPE_KEYWORD)
            .addQueryParam(ParameterConstants.BRSM_FIELDS_PARAM, ParameterConstants.BRSM_FIELDS_LIST)
            .build();
    }
}
