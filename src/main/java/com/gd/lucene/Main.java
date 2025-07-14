package com.gd.lucene;


//import com.gd.lucene.service.Indexer;
//import com.gd.lucene.service.LuceneIndexService;

import com.gd.lucene.api.exchange.LoadToIndexResult;
import com.gd.lucene.service.IndexService;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@QuarkusMain
public class Main {

    @Inject
    private Logger logger;


    @Inject
    private IndexService indexService;


    public static void main(String... args) {
        Quarkus.run(args);
    }

    void onStart(@Observes StartupEvent ev) {
        try {
            System.out.println("Loading...");
            LoadToIndexResult loadToIndexResult = indexService.loadDataIntoIndex();
            System.out.println("loadToIndexResult: " + loadToIndexResult);

        } catch (Exception e) {
            logger.error("Error loadIntoIndex : " + e.getMessage());
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        logger.info("The application is stopping...");
    }


    @Shutdown
    void shutdown() {
        logger.info("The application is shutdown...");

    }

}