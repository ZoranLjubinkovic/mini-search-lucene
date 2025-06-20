package com.gd.lucene.service;

import com.gd.lucene.endpoint.io.SearchResponse;
import com.gd.lucene.endpoint.io.SearchResponses;
import com.gd.lucene.endpoint.io.SortTypes;
import com.gd.lucene.model.DocumentUpdateRequestBody;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.gd.lucene.service.Indexer.listSeparator;
import static com.gd.lucene.service.NDJsonFileReader.fieldBrands;
import static com.gd.lucene.service.NDJsonFileReader.fieldCategories;
import static com.gd.lucene.service.NDJsonFileReader.fieldDescription;
import static com.gd.lucene.service.NDJsonFileReader.fieldId;
import static com.gd.lucene.service.NDJsonFileReader.fieldImageUri;
import static com.gd.lucene.service.NDJsonFileReader.fieldPrice;
import static com.gd.lucene.service.NDJsonFileReader.fieldTitle;

public interface LuceneService {

    Set<String> emptySet = new HashSet<>();

    String[] emptyStringArray = {};

    IndexWriter indexWriter();

    Directory directory();


    Analyzer analyzer();

    void analyzer(Analyzer analyzer);

    SearchResponses search(Query query) throws Exception;

    SearchResponses search(String query, SortTypes sortTypes) throws Exception;

    SearchResponse update(String uuid, DocumentUpdateRequestBody documentUpdateRequestBody) throws Exception;

    void delete(String uuid) throws IOException;

    static List<String> getTokens(Analyzer analyzer, String input) {
        List<String> result = new LinkedList<>();
        try (Reader reader = new StringReader(input)) {
            try (TokenStream stream = analyzer.tokenStream("", reader)) {
                stream.reset();
                while (stream.incrementToken()) {
                    String string = stream.getAttribute(CharTermAttribute.class).toString();
                    result.add(Indexer.filter(string));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    static SearchResponses toSearchResponse(ScoreDoc[] scoreDocs, StoredFields storedFields, long total, int maxResults) throws Exception {
        Set<String> brandSetAll = new HashSet<>();
        Set<String> categoriesSetAll = new HashSet<>();

        List<SearchResponse> searchResponseList = new ArrayList<>();
        for (int i = 0; i < scoreDocs.length; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            Document doc = storedFields.document(scoreDoc.doc);

            String categories = doc.get(fieldCategories);
            String[] categoriesList = categories != null ? categories.split(listSeparator) : emptyStringArray;
            Set<String> categoriesSet = new HashSet<>(Arrays.asList(categoriesList));
            categoriesSetAll.addAll(categoriesSet);

            String brands = doc.get(fieldBrands);
            String[] brandsList = brands != null ? brands.split(listSeparator) : emptyStringArray;
            Set<String> brandsSet = new HashSet<>(Arrays.asList(brandsList));
            brandSetAll.addAll(brandsSet);

            if (i < maxResults) {

                SearchResponse searchResponse = new SearchResponse();

                searchResponse.setId(doc.get(fieldId));
                searchResponse.setCategories(categoriesSet);

                searchResponse.setBrands(brandsSet);

                searchResponse.setTitle(doc.get(fieldTitle));
                searchResponse.setDescription(doc.get(fieldDescription));
                searchResponse.setImageUri(doc.get(fieldImageUri));
                String priceAsString = doc.get(fieldPrice);
                try {
                    double price = Double.parseDouble(priceAsString);
                    searchResponse.setPrice(price);
                } catch (Exception e) {
                    System.err.println("Error getting price: priceAsString: " + priceAsString + " : " + e);
                }

                searchResponseList.add(searchResponse);
            }

        }
        SearchResponses searchResponses = new SearchResponses(searchResponseList, total);
        searchResponses.setBrands(brandSetAll);
        searchResponses.setCategories(categoriesSetAll);

        return searchResponses;
    }

}
