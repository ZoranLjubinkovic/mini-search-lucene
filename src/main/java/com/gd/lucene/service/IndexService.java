package com.gd.lucene.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gd.lucene.analyzer.StemmingAnalyzer;
import com.gd.lucene.analyzer.NoStemmingNoStopWordsAnalyzer;
import com.gd.lucene.api.exchange.LoadToIndexResult;
import com.gd.lucene.model.HomeAppliance;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.gd.lucene.model.HomeAppliance.fieldBrands;
import static com.gd.lucene.model.HomeAppliance.fieldCategories;
import static com.gd.lucene.model.HomeAppliance.fieldColor;
import static com.gd.lucene.model.HomeAppliance.fieldDescription;
import static com.gd.lucene.model.HomeAppliance.fieldTitle;

@ApplicationScoped
public class IndexService {

    @Inject
    Logger log;

    static final JsonMapper mapper = new JsonMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
    }

    //    private final Path idx = Paths.get("index");
    private Analyzer analyzer;

    private final Directory directory = new ByteBuffersDirectory();

    private IndexWriter indexWriter;

    static final String fileName = "conversational_demo_demoproducts_flattened.json";


    @PostConstruct
    void init() {
        // ...
        log.info("LuceneIndexingService: PostConstruct ..");
        this.analyzer = createAnalyzer();
        getOrCreateIndexWriter(this.analyzer);
    }

    @PreDestroy
    public void preDestroy() {
        log.info("LuceneIndexingService: PreDestroy ..");

        if (indexWriter != null) {
            try {
                indexWriter.close();
            } catch (Exception e) {
            }
        }
        if (analyzer != null) {
            analyzer.close();
        }
        if (directory != null) {
            try {
                directory.close();
            } catch (Exception e) {
            }
        }
    }


    private Analyzer createAnalyzer() {

        StemmingAnalyzer stemmingAnalyzer = new StemmingAnalyzer();

        Map<String, Analyzer> fieldToAnalyzerMap = Map.of(
                fieldTitle, stemmingAnalyzer,
                fieldDescription, stemmingAnalyzer,
                fieldBrands, stemmingAnalyzer,
                fieldCategories, stemmingAnalyzer
//                fieldColor, stemmingAnalyzer
        );

        return new PerFieldAnalyzerWrapper(new NoStemmingNoStopWordsAnalyzer(), fieldToAnalyzerMap);
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public IndexWriter getOrCreateIndexWriter() {

        return getOrCreateIndexWriter(this.analyzer);
    }

    private IndexWriter getOrCreateIndexWriter(Analyzer analyzer) {

        if (indexWriter == null) {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            try {
                indexWriter = new IndexWriter(directory, indexWriterConfig);
            } catch (IOException e) {
                log.error("Error creating index writer", e);
                throw new RuntimeException(e);
            }
        }
        return indexWriter;
    }

    public LoadToIndexResult loadDataIntoIndex() throws IOException {

        int loaded = 0;

        getOrCreateIndexWriter(this.analyzer);

        indexWriter.deleteAll();
        indexWriter.commit();

        try (InputStream inputStream = IndexService.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                System.err.println("Resource not found: " + fileName);
                throw new Exception("Resource not found: " + fileName);
            }


            try (MappingIterator<HomeAppliance> it = mapper.readerFor(HomeAppliance.class).readValues(inputStream)) {
                for (int i = 0; it.hasNextValue(); i++) {

                    HomeAppliance homeAppliance = it.nextValue();
                    Document document = homeAppliance.toDocument();
                    loaded++;
                    indexWriter.addDocument(document);
                    indexWriter.flush();
                }

                indexWriter.commit();
            }
        } catch (IOException e) {
            System.err.println("Error reading resource: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading index", e);
            throw new RuntimeException(e);
        }

        IndexReader reader = DirectoryReader.open(directory);
        int numDocs = reader.numDocs();
        reader.close();

        log.info("Loaded " + loaded + " documents into index " + numDocs + " docs.");
        return new LoadToIndexResult(loaded, numDocs);
    }
}
