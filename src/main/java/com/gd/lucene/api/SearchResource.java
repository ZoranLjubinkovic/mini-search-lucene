package com.gd.lucene.api;

import com.gd.lucene.api.exchange.AnalyzerType;
import com.gd.lucene.api.exchange.SearchResponses;
import com.gd.lucene.api.exchange.SortType;
import com.gd.lucene.service.SearchService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.lucene.index.IndexNotFoundException;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.hibernate.validator.constraints.UUID;
import org.jboss.logging.Logger;

@Path("/search")
public class SearchResource {

    @Inject
    Logger log;

    @Inject
    SearchService searchService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByMultiFields(
            @QueryParam("q") @NotNull @io.smallrye.common.constraint.NotNull @NotBlank @NotEmpty
            @Parameter(name = "q", description = "User query",
                    examples = {
                            @ExampleObject(name = "The Bosch refrigerator", value = "The Bosch refrigerator"),
                            @ExampleObject(name = "Dishwasher", value = "Dishwasher"),
                            @ExampleObject(name = "Bosch", value = "Bosch"),
                            @ExampleObject(name = "refrigerator", value = "refrigerator"),
                            @ExampleObject(name = "Stainless Steel", value = "Stainless Steel"),
                            @ExampleObject(name = "\"Stainless Steel\"", value = "\"Stainless Steel\""),
                            @ExampleObject(name = "Black & Decker", value = "Black & Decker"),
                    })

            String userQuery,

            @QueryParam("maxResults") @NotNull @DefaultValue("100") @Min(1) @Max(100)
            int maxResults,

            @QueryParam("priceFrom") @Min(0) @Max(100_000)
            Double priceFrom,
            @QueryParam("priceTo") @Min(0) @Max(100_000)
            Double priceTo,

            @QueryParam("sort") @NotNull @DefaultValue("SORT_NOTHING")
            SortType sortType,

            @QueryParam("analyzerType") @NotNull @DefaultValue("ANALYZER_STEMMING")
            AnalyzerType analyzerType

    ) {
        try {
            SearchResponses searchResponses = searchService.searchMulti(userQuery, maxResults, sortType, priceFrom, priceTo, analyzerType);
            if (searchResponses.getFound() == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity(searchResponses).build();
            } else {
                return Response.ok().entity(searchResponses).build();
            }
        } catch (IndexNotFoundException e) {
            log.error("IndexNotFoundException: " + e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT).entity(new SearchResponses("No data in index: ( " + e.getMessage() + " )")).build();
        } catch (Throwable e) {
            return Response.serverError().entity(new SearchResponses(e.getMessage())).build();
        }
    }

    @GET
    @Path("/id/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchById(@PathParam("uuid") @NotNull @io.smallrye.common.constraint.NotNull @NotBlank @NotEmpty @UUID String uuid) throws Exception {
        try {
            SearchResponses searchResponses = searchService.searchById(uuid.trim());
            if (searchResponses.getFound() == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity(searchResponses).build();
            } else {
                return Response.ok().entity(searchResponses).build();
            }
        } catch (IndexNotFoundException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT).entity(new SearchResponses("No data in index: ( " + e.getMessage() + " )")).build();
        } catch (Throwable e) {
            return Response.serverError().entity(new SearchResponses(e.getMessage())).build();
        }
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchMatchAll() throws Exception {
        try {
            SearchResponses searchResponses = searchService.searchMatchAll(1_000);
            if (searchResponses.getFound() == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity(searchResponses).build();
            } else {
                return Response.ok().entity(searchResponses).build();
            }
        } catch (IndexNotFoundException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT).entity(new SearchResponses("No data in index: ( " + e.getMessage() + " )")).build();
        } catch (Throwable e) {
            return Response.serverError().entity(new SearchResponses(e.getMessage())).build();
        }
    }
}
