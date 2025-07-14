package com.gd.lucene.api;

import com.gd.lucene.api.exchange.SearchResponse;
import com.gd.lucene.api.exchange.SearchResponses;
import com.gd.lucene.model.DocumentUpdateRequestBody;
import com.gd.lucene.service.DocumentService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.hibernate.validator.constraints.UUID;
import org.jboss.logging.Logger;

@Path("/doc")
public class DocumentResource {

    @Inject
    private Logger logger;


    @Inject
    private DocumentService documentService;

    @Operation(summary = "", description = "")
    @PUT
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("uuid") @NotNull @io.smallrye.common.constraint.NotNull @NotEmpty @NotBlank @UUID String uuid, @RequestBody DocumentUpdateRequestBody documentUpdateRequestBody) throws Exception {

        try {
            SearchResponse searchResponse = documentService.update(uuid, documentUpdateRequestBody);
            logger.info("searchResponse: " + searchResponse);
            return Response.ok().entity(searchResponse).build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity(new SearchResponses(e.getMessage())).build();
        }
    }


    @DELETE
    @Path("/{uuid}")
    public Response delete(@PathParam("uuid") @NotNull @io.smallrye.common.constraint.NotNull @NotEmpty @NotBlank @UUID String uuid) {
        try {
            documentService.delete(uuid);
            return Response.noContent().build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity(new SearchResponses(e.getMessage())).build();
        }
    }

}
