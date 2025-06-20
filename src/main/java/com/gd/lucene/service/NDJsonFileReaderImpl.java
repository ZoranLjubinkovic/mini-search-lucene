package com.gd.lucene.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gd.lucene.model.HomeAppliance;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

@ApplicationScoped
public class NDJsonFileReaderImpl implements NDJsonFileReader {

    static final String fileName = "conversational_demo_demoproducts_flattened.json";

    final List<HomeAppliance> homeAppliances = new LinkedList<>();

    static final JsonMapper mapper = new JsonMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public int streamDataToIndexer(Indexer indexer) throws Exception {

        int counter = 0;
        try (InputStream inputStream = NDJsonFileReaderImpl.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                System.err.println("Resource not found: " + fileName);
                throw new Exception("Resource not found: " + fileName);
            }

            int loaded = 0;
            homeAppliances.clear();

            try (MappingIterator<HomeAppliance> it = mapper.readerFor(HomeAppliance.class).readValues(inputStream)) {
                for (counter = 0; it.hasNextValue(); counter++) {

                    HomeAppliance homeAppliance = it.nextValue();

                    homeAppliances.add(homeAppliance);
                    loaded++;

                    if (loaded == batchSize) {
                        indexer.addToIndexCallback(homeAppliances);
                        homeAppliances.clear();
                        loaded = 0;
                    }

                }
            }
        } catch (IOException e) {
            homeAppliances.clear();
            System.err.println("Error reading resource: " + e.getMessage());
        } finally {
            indexer.addToIndexCallback(homeAppliances);
        }

        return counter;
    }
}
