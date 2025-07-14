package com.gd.lucene.api;

import com.gd.lucene.api.exchange.LoadToIndexResult;
import com.gd.lucene.api.exchange.SearchResponses;
import com.gd.lucene.service.IndexService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/index")
public class IndexResource {

    @Inject
    Logger log;


    @Inject
    IndexService indexService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createIndex() throws Exception {

        try {
            LoadToIndexResult loadToIndexResult = indexService.loadDataIntoIndex();
            log.info("createIndexResponse: " + loadToIndexResult);
            return Response.ok().entity(loadToIndexResult).build();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return Response.serverError().entity(new SearchResponses(e.getMessage())).build();
        }
    }

}
