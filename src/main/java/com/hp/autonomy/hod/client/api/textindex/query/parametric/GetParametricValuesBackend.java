/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.hod.client.api.textindex.query.parametric;

import com.hp.autonomy.hod.client.api.authentication.AuthenticationToken;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;

import java.util.Collection;
import java.util.Map;

interface GetParametricValuesBackend {
    String URL = "/2/api/sync/textindex/query/parametricvalues/v2";

    /**
     * Get parametric values for the fieldNames using the given token
     *
     * @param token      The token to use to authenticate the request
     * @param fieldNames A collection of field names to return values for
     * @param indexes    The indexes to get values for
     * @param params     Additional parameters to be sent as part of the request
     * @return A list of field names with their parametric values
     */
    @GET(URL)
    Response getParametricValues(
            @Query("token") AuthenticationToken<?, ?> token,
            @Query("field_names") Collection<String> fieldNames,
            @Query("indexes") Collection<ResourceName> indexes,
            @QueryMap Map<String, Object> params
    ) throws HodErrorException;
}
