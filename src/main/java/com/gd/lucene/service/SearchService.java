package com.gd.lucene.service;

import com.gd.lucene.analyzer.NoStemmingNoStopWordsAnalyzer;
import com.gd.lucene.api.exchange.AnalyzerType;
import com.gd.lucene.api.exchange.SearchResponse;
import com.gd.lucene.api.exchange.SearchResponses;
import com.gd.lucene.api.exchange.SortType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gd.lucene.model.HomeAppliance.fieldBrands;
import static com.gd.lucene.model.HomeAppliance.fieldCategories;
import static com.gd.lucene.model.HomeAppliance.fieldColor;
import static com.gd.lucene.model.HomeAppliance.fieldDescription;
import static com.gd.lucene.model.HomeAppliance.fieldId;
import static com.gd.lucene.model.HomeAppliance.fieldImageUri;
import static com.gd.lucene.model.HomeAppliance.fieldPrice;
import static com.gd.lucene.model.HomeAppliance.fieldTitle;

@ApplicationScoped
public class SearchService {

    @Inject
    Logger logger;

    @Inject
    IndexService indexService;

    private DirectoryReader directoryReader;
    private IndexSearcher indexSearcher;
    private Analyzer stemmingAnalyzer;
    private Analyzer noStemmingAnalyzer;


    @PostConstruct
    void init() {
        // ...
        logger.info("SearchService: PostConstruct ..");

        IndexWriter indexWriter = indexService.getOrCreateIndexWriter();
        try {
            directoryReader = DirectoryReader.open(indexWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        indexSearcher = new IndexSearcher(directoryReader);

        this.stemmingAnalyzer = indexService.getAnalyzer();
        this.noStemmingAnalyzer = new NoStemmingNoStopWordsAnalyzer();

    }

    @PreDestroy
    void destroy() {
        // ...
        logger.info("SearchService: PreDestroy ..");

        if (directoryReader != null) {
            try {
                directoryReader.close();
            } catch (IOException e) {
            }
        }
        if (stemmingAnalyzer != null) {
            try {
                stemmingAnalyzer.close();
            } catch (Exception e) {
            }
        }
        if (noStemmingAnalyzer != null) {
            try {
                noStemmingAnalyzer.close();
            } catch (Exception e) {
            }
        }
    }

    static final String[] searchableFields = {
            fieldTitle,
            fieldDescription,
            fieldBrands,
            fieldCategories,
            fieldColor,
    };


    public SearchResponses searchById(String uuid) throws Exception {
        Query query = new TermQuery(new Term(fieldId, uuid.trim()));
        return search(query, 1, SortType.SORT_NOTHING, null, null);
    }

    public SearchResponses searchMatchAll(int maxResults) throws Exception {
        Query query = new MatchAllDocsQuery();
        return search(query, maxResults, SortType.SORT_NOTHING, null, null);
    }

    public SearchResponses searchMulti(
            String queryString,
            int maxResults,
            SortType sortType,
            Double priceFrom,
            Double priceTo,
            AnalyzerType analyzerType) throws Exception {

        Analyzer chosenAnalyzer = stemmingAnalyzer;
        if (analyzerType == AnalyzerType.ANALYZER_STEMMING) {
            chosenAnalyzer = stemmingAnalyzer;
        } else if (analyzerType == AnalyzerType.ANALYZER_NO_STEMMING_NO_STOP_WORDS) {
            chosenAnalyzer = noStemmingAnalyzer;
        }

        MultiFieldQueryParser multiFieldQueryParser =
                new MultiFieldQueryParser(
                        searchableFields,
                        chosenAnalyzer
                );

        Query query = multiFieldQueryParser.parse(queryString);

        return search(query, maxResults, sortType, priceFrom, priceTo);
    }

    private SearchResponses search(
            Query query,
            int maxResults,
            SortType sortType,
            Double priceFrom,
            Double priceTo) throws Exception {

        if (priceFrom != null && priceTo != null) {
            if (priceFrom > priceTo) {
                throw new IllegalArgumentException("priceFrom > priceTo");
            }
        }

        return getSearchResponses(maxResults, query, sortType, priceFrom, priceTo);
    }


    private SearchResponses getSearchResponses(
            int maxResults,
            Query query,
            SortType sortType,
            Double priceFrom,
            Double priceTo
    ) throws IOException {


        Sort sort = null;

        // sort by price asc / desc OR without sort
        switch (sortType) {

            case SORT_NOTHING -> {
            }

            case SORT_BY_PRICE_ASC ->
                    sort = new Sort(new SortedNumericSortField(fieldPrice, SortField.Type.DOUBLE, false));

            case SORT_BY_PRICE_DESC ->
                    sort = new Sort(new SortedNumericSortField(fieldPrice, SortField.Type.DOUBLE, true));

        }

        // price range
        if (priceFrom != null || priceTo != null) {

            if (priceFrom == null) {
                priceFrom = 0.0;
            }
            if (priceTo == null) {
                priceTo = 1_000_000_000.0;
            }

            Query priceRangeQuery = DoublePoint.newRangeQuery(fieldPrice, priceFrom, priceTo);
            query = new BooleanQuery.Builder()
                    .add(query, BooleanClause.Occur.MUST)
                    .add(priceRangeQuery, BooleanClause.Occur.FILTER)
                    .build();
        }

        TopDocs topDocs;

        IndexSearcher searcher = getOrCreateIndexSearcherIfNeeded();

        if (sort != null) {
            topDocs = searcher.search(query, maxResults, sort);
        } else {
            topDocs = searcher.search(query, maxResults);
        }

        List<SearchResponse> searchResponsesList = new ArrayList<>();
        for (ScoreDoc sd : topDocs.scoreDocs) {

            SearchResponse searchResponse = new SearchResponse();

            searchResponse.setScore(sd.score);

            Document document = searcher.doc(sd.doc);
            searchResponse.setId(document.get(fieldId));
            searchResponse.setTitle(document.get(fieldTitle));

            searchResponse.setDescription(document.get(fieldDescription));

            String[] catValues = document.getValues(fieldCategories);
            List<String> catSet = new ArrayList<>(Arrays.asList(catValues));
            searchResponse.setCategories(catSet);

            String[] brandValues = document.getValues(fieldBrands);
            List<String> brandSet = new ArrayList<>(Arrays.asList(brandValues));
            searchResponse.setBrands(brandSet);

            searchResponse.setPrice(document.getField(fieldPrice).numericValue().doubleValue());
            String color = document.get(fieldColor);
            searchResponse.setColor(color);

            searchResponse.setImageUri(document.get(fieldImageUri));


            searchResponsesList.add(searchResponse);
        }

        SearchResponses searchResponses = new SearchResponses(searchResponsesList);
        searchResponses.setLuceneQuery(query.toString());
        searchResponses.setSort(sortType);
        searchResponses.setFound(searchResponsesList.size());
        return searchResponses;
    }


    private IndexSearcher getOrCreateIndexSearcherIfNeeded() throws IOException {
        DirectoryReader newReader = DirectoryReader.openIfChanged(directoryReader);
        if (newReader != null) {
            DirectoryReader oldReader = directoryReader;
            directoryReader = newReader;
            indexSearcher = new IndexSearcher(newReader);
            oldReader.close();
        }
        return indexSearcher;
    }
}