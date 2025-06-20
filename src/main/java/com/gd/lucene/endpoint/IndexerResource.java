package com.gd.lucene.endpoint;

import com.gd.lucene.endpoint.io.CreateIndexResponse;
import com.gd.lucene.endpoint.io.SearchResponses;
import com.gd.lucene.service.Indexer;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/index")
public class IndexerResource {

    @Inject
    Logger log;


    @Inject
    Indexer indexer;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createIndex() throws Exception {

        try {
            CreateIndexResponse createIndexResponse = indexer.initLoading();
            log.info("createIndexResponse: " + createIndexResponse);
            return Response.ok().entity(createIndexResponse).build();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return Response.serverError().entity(new SearchResponses(e.getMessage())).build();
        }
    }

}
