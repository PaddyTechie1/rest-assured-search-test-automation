package org.search.api.usecase.helper;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.search.api.usecase.util.ParameterConstants;

public final class EmpathyHelper {

    private EmpathyHelper() {
    }

    //request building - non-bdd way
    public static RequestSpecification getSearchRequestSpec(final String searchTerms) {
        return new RequestSpecBuilder()
            .setBaseUri(ParameterConstants.EMPATHY_BASE_URI)
            .setBasePath(ParameterConstants.EMPATHY_SEARCH_END_POINT)
            .addQueryParam(ParameterConstants.PAGE_SIZE_PARAM, ParameterConstants.PAGE_SIZE_VALUE)
            .addQueryParam(ParameterConstants.OFFSET_PARAM, ParameterConstants.ZERO)
            .addQueryParam(ParameterConstants.EMPATHY_QUERY_PARAM, searchTerms)
            .addQueryParam(ParameterConstants.EMPATHY_SCOPE_PARAM, ParameterConstants.EMPATHY_SCOPE_DESKTOP)
            .addQueryParam(ParameterConstants.EMPATHY_LANG_PARAM, ParameterConstants.EMPATHY_LANG_NL)
            .build();
    }
}
