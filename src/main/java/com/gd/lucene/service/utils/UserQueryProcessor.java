package com.gd.lucene.service.utils;

import com.gd.lucene.service.Indexer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.gd.lucene.service.Indexer.uniqueBrands;
import static com.gd.lucene.service.Indexer.uniqueCategories;
import static com.gd.lucene.service.LuceneService.getTokens;
import static com.gd.lucene.service.NDJsonFileReader.fieldBrand;
import static com.gd.lucene.service.NDJsonFileReader.fieldCategory;
import static com.gd.lucene.service.NDJsonFileReader.fieldDescription;
import static com.gd.lucene.service.NDJsonFileReader.fieldTitle;

public interface UserQueryProcessor {

    EdgeNGramAnalyzer edgeNGramAnalyzer = new EdgeNGramAnalyzer(1, 4);

    static int countWhitespaces(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    static Query checkLuceneSyntax(String userQuery) {

        if (userQuery.contains(":")
                || userQuery.contains("\"")
                || userQuery.contains("#")
                || userQuery.contains("~")
                || userQuery.contains("+")) { //TODO: add more character used by Lucene parser ?
            String[] searchableFields = {
                    fieldTitle,
                    fieldDescription,
                    fieldBrand,
                    fieldCategory
            };
            MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(
                    searchableFields, new StandardAnalyzer()
            );
            try {
                return multiFieldQueryParser.parse(userQuery);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * @param userQueryArg 'raw' user query
     * @param analyzer
     * @return - lucene Query
     *
     * <p>query for search in fields: Brand, Category, Title, Description
     * <p>
     * <p> analyze 'raw' user query and detect usage of Lucene query syntax: ie.
     * <p>          brand:Bosch
     * <p> OR
     * <p> try to 'detect' Category/Brand by searching pre-populated 'cache' of categories and brands,
     * <p> and properly make Query for found category(-ies) and / or brand(-s) in right field(s): ie.
     * <p> raw query:
     * <p>      LG Freestanding Ranges  something
     * <p> should create lucene Query:
     * <p>       "#brand:lg #category:\"freestanding ranges\" title:something description:something",
     * <p>
     * <p> remaining words from user query will be used to find by Title and Desc.
     */
    static Query searchByMultiFields(String userQueryArg, Analyzer analyzer) {

        String userQuery = Indexer.filter(userQueryArg);

        Query query = checkLuceneSyntax(userQuery);

        if (query != null) {
            return query;
        }

        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        List<String> userQueryNGramTokens = getTokens(edgeNGramAnalyzer, userQuery);
        //
        //  Brands
        //
        List<String> brandsFound = findInCache(userQueryNGramTokens, uniqueBrands);

        QueryParser queryParser4Brand = new QueryParser(fieldBrand, analyzer);
        userQuery = addQuery(fieldBrand, brandsFound, userQuery, queryParser4Brand, builder);


        userQueryNGramTokens = getTokens(edgeNGramAnalyzer, userQuery);
        //
        //  Categories
        //

        List<String> categoriesFound = findInCache(userQueryNGramTokens, uniqueCategories);
        QueryParser queryParser4Category = new QueryParser(fieldCategory, analyzer);
        userQuery = addQuery(fieldCategory, categoriesFound, userQuery, queryParser4Category, builder);


        List<String> userQueryTokens = getTokens(analyzer, userQuery);
        //
        //  Title & Description
        //
        for (String userQueryToken : userQueryTokens) {

            TermQuery termQueryTitle = new TermQuery(new Term(fieldTitle, userQueryToken));
            TermQuery termQueryDescription = new TermQuery(new Term(fieldDescription, userQueryToken));
            if (!categoriesFound.isEmpty() || !brandsFound.isEmpty()) { //user actually want to see the Brand / Category ?
                builder.add(termQueryTitle, BooleanClause.Occur.SHOULD);
                builder.add(termQueryDescription, BooleanClause.Occur.SHOULD);
            } else {
                builder.add(termQueryTitle, BooleanClause.Occur.MUST);
                builder.add(termQueryDescription, BooleanClause.Occur.MUST);
            }
        }

        return builder.build();
    }

    static String addQuery(String field, List<String> found, String userQuery, QueryParser queryParser, BooleanQuery.Builder builder) {
        if (found.isEmpty()) {
            return userQuery;
        }

        if (found.size() > 1) {

            Set<String> toRemove = new HashSet<>();

            Collections.sort(found, Comparator.comparingInt(UserQueryProcessor::countWhitespaces));

            for (int i = 0; i < found.size(); i++) {
                String shortPhrase = found.get(i);
                for (int j = found.size() - 1; j >= i; j--) {
                    String longPhrase = found.get(j);
                    if (i < j && longPhrase.contains(shortPhrase)) {
                        toRemove.add(shortPhrase);
                    }
                }

            }
            found.removeAll(toRemove);
        }

        for (String s : found) {

            int whitespaces = countWhitespaces(s);
            try {
                Query q =
                        (whitespaces > 0)
                                ?
                                queryParser.parse("\"" + s + "\"")
                                :
                                new TermQuery(new Term(field, s));


                if (found.size() == 1) {
                    builder.add(q, BooleanClause.Occur.FILTER);
                } else {
                    builder.add(q, BooleanClause.Occur.SHOULD);
                }

                userQuery = userQuery.replace(" " + s + " ", " ");
                userQuery = userQuery.replace(s + " ", " ");
                userQuery = userQuery.replace(" " + s, " ");

                if (userQuery.equalsIgnoreCase(s)) {
                    userQuery = "";
                }

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return userQuery;
    }


    static List<String> findInCache(Collection<String> tokens, Set<String> uniques) {
        List<String> found = new LinkedList<>();
        for (String token : tokens) {
            if (uniques.contains(token)) {
                found.add(token);
            }
        }
        return found;
    }

}
