package com.gd.lucene;


import com.gd.lucene.endpoint.io.CreateIndexResponse;
import com.gd.lucene.service.Indexer;
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
    Logger log;

    @Inject
    Indexer indexer;

    public static void main(String... args) {
        Quarkus.run(args);
    }

    void onStart(@Observes StartupEvent ev) {
        try {
            CreateIndexResponse createIndexResponse = indexer.initLoading();
            System.out.println("Loaded: " + createIndexResponse);

//            uniqueCategories.forEach(uniqueCategory -> System.out.println("\t->" + uniqueCategory + "<-") );

        } catch (Exception e) {
            log.error("Error loadIntoIndex : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("The application is stopping...");
    }


    @Shutdown
    void shutdown() {
        log.info("The application is shutdown...");

    }

}