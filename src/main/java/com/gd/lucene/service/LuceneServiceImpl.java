package com.gd.lucene.service;

import com.gd.lucene.endpoint.io.SearchResponse;
import com.gd.lucene.endpoint.io.SearchResponses;
import com.gd.lucene.endpoint.io.SortTypes;
import com.gd.lucene.model.DocumentUpdateRequestBody;
import com.gd.lucene.service.utils.UserQueryProcessor;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Bits;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.gd.lucene.service.Indexer.listSeparator;
import static com.gd.lucene.service.Indexer.processBrands;
import static com.gd.lucene.service.Indexer.processCategories;
import static com.gd.lucene.service.Indexer.processList;
import static com.gd.lucene.service.Indexer.uniqueBrands;
import static com.gd.lucene.service.Indexer.uniqueCategories;
import static com.gd.lucene.service.IndexerImpl.fieldTypeForUuid;
import static com.gd.lucene.service.NDJsonFileReader.fieldBrands;
import static com.gd.lucene.service.NDJsonFileReader.fieldCategory;
import static com.gd.lucene.service.NDJsonFileReader.fieldDescription;
import static com.gd.lucene.service.NDJsonFileReader.fieldId;
import static com.gd.lucene.service.NDJsonFileReader.fieldImageUri;
import static com.gd.lucene.service.NDJsonFileReader.fieldPrice;
import static com.gd.lucene.service.NDJsonFileReader.fieldTitle;

@Singleton
public class LuceneServiceImpl implements LuceneService {

    @Inject
    Logger log;

    private Directory directory;

    private Analyzer analyzer;


    @Override
    public Analyzer analyzer() {
        return analyzer;
    }

    @Override
    public void analyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    private IndexWriter indexWriter;

    public LuceneServiceImpl() {
        try {
            directory = new ByteBuffersDirectory();
//            directory = FSDirectory.open(Path.of("index"));
            analyzer = new StandardAnalyzer();

        } catch (Exception e) {
            log.error(e);
        }
    }


    @Override
    public IndexWriter indexWriter() {

        boolean shouldCreate = false;

        if (indexWriter == null) {
            shouldCreate = true;
        } else { //if (!indexWriter.isOpen()) {
            try {
                indexWriter.close();
            } catch (IOException e) {
                log.warn("Error indexWriter !!!", e);
            } finally {
                indexWriter = null;
                shouldCreate = true;
            }
        }
        if (shouldCreate) {

            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer());
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            try {
                indexWriter = new IndexWriter(directory, indexWriterConfig);
                return indexWriter;
            } catch (IOException e) {
                log.error("Error creating index writer", e);
                throw new RuntimeException(e);
            }
        }

        return indexWriter;
    }

    @Override
    public Directory directory() {
        return directory;
    }


    @Override
    public SearchResponses search(String userQuery, SortTypes sortTypes) throws Exception {
        Query query = UserQueryProcessor.searchByMultiFields(userQuery, analyzer());
        return search(query, sortTypes);
    }


    @Override
    public SearchResponses search(Query query) throws Exception {
        return search(query, SortTypes.SORT_NOTHING);
    }


    private SearchResponses search(Query query, SortTypes sortTypes) throws Exception {

        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(indexReader);

            Sort sort = null;

            switch (sortTypes) {

                case SORT_NOTHING -> {
                }

                case SORT_BY_PRICE_ASC ->
                        sort = new Sort(new SortedNumericSortField(fieldPrice, SortField.Type.DOUBLE, false));

                case SORT_BY_PRICE_DESC ->
                        sort = new Sort(new SortedNumericSortField(fieldPrice, SortField.Type.DOUBLE, true));

            }

            TopDocs topDocs;
            int maxResults = 10;
            if (sort != null) {
                topDocs = searcher.search(query, 1_000, sort);
            } else {
                topDocs = searcher.search(query, 1_000);
            }

            StoredFields storedFields = searcher.storedFields();
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            SearchResponses searchResponse = LuceneService.toSearchResponse(scoreDocs, storedFields, topDocs.totalHits.value, maxResults);
            searchResponse.setLuceneQuery(query.toString());
            searchResponse.setSort(sortTypes);
            return searchResponse;
        }
    }


    @Override
    public void delete(String uuid) throws IOException {
        Term term = new Term(fieldId, uuid);
        IndexWriter writer = indexWriter();

        writer.deleteDocuments(term);
        writer.commit();
        writer.flush();
        writer.close();

        log.info("Delete: doc with uuid: " + uuid);

        updateUniqueCategoriesAndBrands();

    }


    @Override
    public SearchResponse update(String uuid, DocumentUpdateRequestBody updateRequestBody) throws Exception {
        Term term = new Term(fieldId, uuid);
        Query query = new TermQuery(term);
        SearchResponses searchResponses = search(query);
        if (searchResponses != null) {
            List<SearchResponse> searchResponsesFound = searchResponses.getSearchResponses();
            if (!searchResponsesFound.isEmpty()) {
                SearchResponse searchResponse = searchResponsesFound.get(0);
                log.info("Updating: found existing doc: " + searchResponse + " with uuid: " + uuid + " ...");
            }
        }

        IndexWriter writer = indexWriter();

        Document doc = new Document();

        processBrands(updateRequestBody.getBrands(), doc);

        processCategories(updateRequestBody.getCategories(), doc);

        String imageUri = updateRequestBody.getImageUri();
        if (imageUri != null) {
            doc.add(new StringField(fieldImageUri, imageUri, Field.Store.YES));
        }
        Double price = updateRequestBody.getPrice();
        if (price != null) {
            IndexerImpl.processPrice(doc, price);
        } else {
            throw new Exception("price must not be null");
        }

        Field idField = new Field(fieldId, uuid, fieldTypeForUuid);
        doc.add(idField);

        String description = updateRequestBody.getDescription();
        doc.add(new TextField(fieldDescription, description != null ? description.toLowerCase() : "", Field.Store.YES));

        String title = updateRequestBody.getTitle();
        doc.add(new TextField(fieldTitle, title != null ? title.toLowerCase() : "", Field.Store.YES));


        writer.updateDocument(term, doc);

        writer.commit();
        writer.flush();
        writer.close();

        SearchResponse searchResponse = null;
        searchResponses = search(query);
        if (searchResponses != null) {
            List<SearchResponse> searchResponsesFound = searchResponses.getSearchResponses();
            if (searchResponsesFound.size() > 0) {
                searchResponse = searchResponsesFound.get(0);
            }
        }
        log.info("Update: doc with uuid: " + uuid);

        updateUniqueCategoriesAndBrands();

        return searchResponse;
    }


    private void updateUniqueCategoriesAndBrands() {
        IndexReader indexReader;
        try {
            indexReader = DirectoryReader.open(directory);
        } catch (IOException ex) {
            log.error(ex);
            throw new RuntimeException(ex);
        }

        uniqueCategories.clear();
        uniqueBrands.clear();

        for (LeafReaderContext leafContext : indexReader.leaves()) {
            try (LeafReader leafReader = leafContext.reader()) {
                Bits liveDocs = leafReader.getLiveDocs();
                int maxDoc = leafReader.maxDoc();

                for (int i = 0; i < maxDoc; i++) {
                    if (liveDocs == null || liveDocs.get(i)) {
                        StoredFields storedFields = leafReader.storedFields();
                        Document doc = storedFields.document(i);

                        String brands = doc.get(fieldBrands);
                        if (brands != null) {
                            uniqueBrands.addAll(processList(Arrays.asList(brands.split(listSeparator))));
                        }

                        String categories = doc.get(fieldCategory);

                        if (categories != null) {
                            uniqueCategories.addAll(processList(Arrays.asList(categories.split(listSeparator))));
                        }
                    }
                }
            } catch (IOException ex) {
                log.error(ex);
            }
        }

    }

    @PreDestroy
    public void preDestroy() {
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
}


