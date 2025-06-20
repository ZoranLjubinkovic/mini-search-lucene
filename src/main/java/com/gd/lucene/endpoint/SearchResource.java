package com.gd.lucene.endpoint;

import com.gd.lucene.endpoint.io.SearchResponses;
import com.gd.lucene.endpoint.io.SortTypes;
import com.gd.lucene.service.LuceneService;
import jakarta.inject.Inject;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.hibernate.validator.constraints.UUID;
import org.jboss.logging.Logger;

import static com.gd.lucene.service.NDJsonFileReader.fieldBrand;
import static com.gd.lucene.service.NDJsonFileReader.fieldId;
import static com.gd.lucene.service.NDJsonFileReader.fieldTitle;

@Path("/search")
public class SearchResource {

    @Inject
    Logger log;

    @Inject
    LuceneService luceneService;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByMultiFields(
            @QueryParam("q") @NotNull @io.smallrye.common.constraint.NotNull @NotBlank @NotEmpty
            @Parameter(name = "q", description = "User queris",
                    examples = {
                            @ExampleObject(name = "Bosch", value = "Bosch"),
                            @ExampleObject(name = "LG", value = "LG"),
                            @ExampleObject(name = "LG - Lucene syntax", value = "brand:LG"),
                            @ExampleObject(name = "Water Heaters", value = "Water Heaters"),
                            @ExampleObject(name = "Water Heaters - Lucene syntax", value = """
                                    category:"Water Heaters"
                                    """),
                    })

            String userQuery,
            @QueryParam("sort") @NotNull @DefaultValue("SORT_NOTHING")
            SortTypes sort
    ) throws Exception {

        log.info("Search query: " + userQuery + ", sort: " + sort);
        try {
            SearchResponses searchResponses = luceneService.search(userQuery, sort);
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
    @Path("/title/{title}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByTitle(@PathParam("title") @NotNull @io.smallrye.common.constraint.NotNull @NotBlank @NotEmpty String title) throws Exception {

        log.info("Search query: title: " + title);
        try {
            Query query = new TermQuery(new Term(fieldTitle, title.toLowerCase().trim()));
            SearchResponses searchResponses = luceneService.search(query);
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
    @Path("/id/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchById(@PathParam("uuid") @NotNull @io.smallrye.common.constraint.NotNull @NotBlank @NotEmpty @UUID String uuid) throws Exception {

        log.info("Search query: id: " + uuid);
        try {
            Query query = new TermQuery(new Term(fieldId, uuid.trim()));
            SearchResponses searchResponses = luceneService.search(query);
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
    @Path("/brand/{brand}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchByBrand(@PathParam("brand") @NotNull @io.smallrye.common.constraint.NotNull @NotBlank @NotEmpty String brand) throws Exception {

        log.info("Search query: brand: " + brand);
        try {
            Query query = new TermQuery(new Term(fieldBrand, brand.toLowerCase().trim()));
            SearchResponses searchResponses = luceneService.search(query);
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
    public Response searchAll() throws Exception {
        try {
            Query query = new MatchAllDocsQuery();
            SearchResponses searchResponses = luceneService.search(query);
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
