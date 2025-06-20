package com.gd.lucene.service;

import com.gd.lucene.endpoint.io.CreateIndexResponse;
import com.gd.lucene.model.HomeAppliance;
import com.gd.lucene.service.utils.UserQueryProcessor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.paukov.combinatorics3.Generator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gd.lucene.service.LuceneService.emptySet;
import static com.gd.lucene.service.LuceneService.getTokens;
import static com.gd.lucene.service.NDJsonFileReader.fieldBrand;
import static com.gd.lucene.service.NDJsonFileReader.fieldBrands;
import static com.gd.lucene.service.NDJsonFileReader.fieldCategories;
import static com.gd.lucene.service.NDJsonFileReader.fieldCategory;

public interface Indexer {


    String listSeparator = ";;;";


    Set<String> uniqueCategories = new HashSet<>(1024);
    Set<String> uniqueBrands = new HashSet<>(1024);

    CreateIndexResponse initLoading() throws Exception;

    void addToIndexCallback(List<HomeAppliance> homeAppliances) throws Exception;

    static String filter(String str) {
        return str.trim()
                .replaceAll("&", "and") // some cats & brands have this in name !
                .replaceAll("-", "and") // some cats & brands have this in name !
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    static Set<String> processList(Collection<String> list) {
        return list.stream()
                .map(Indexer::filter)
                .collect(Collectors.toSet());
    }

    static void processCategories(Collection<String> categories, Document doc) {

        if (categories == null || categories.isEmpty()) {
            return;
        }

        String categoriesAsString = String.join(listSeparator, categories);
        doc.add(new StringField(fieldCategories, categoriesAsString, Field.Store.YES));

        Set<String> categoriesToIndex = processCategoryList(categories);
        for (String category : categoriesToIndex) {
            doc.add(new TextField(fieldCategory, category.toLowerCase().trim(), Field.Store.YES));
        }
        uniqueCategories.addAll(categoriesToIndex);


    }

    static Set<String> processCategoryList(Collection<String> categoryList) {

        if (categoryList == null || categoryList.isEmpty()) {
            return emptySet;
        }

        Set<String> categories = processList(categoryList);

        Set<String> categorySet = new HashSet<>();

        for (String category : categories) {
            if (category != null) {
                String[] split = category.split(">");
                for (String s : split) {
                    if (s != null) {
                        List<String> tokens = getTokens(UserQueryProcessor.edgeNGramAnalyzer, s);
                        for (String token : tokens) {
                            int whitespaces = UserQueryProcessor.countWhitespaces(token);
                            if (whitespaces > 0) {
                                String[] words = token.split(" ");
                                Generator.permutation(words)
                                        .simple()
                                        .stream()
                                        .forEach(p -> {
                                            String joined = String.join(" ", p).trim().toLowerCase();
                                            categorySet.add(joined);
                                        });
                            } else {
                                categorySet.add(token);
                            }
                        }
                    }
                }
            }
        }
        return categorySet;
    }


    static void processBrands(Collection<String> brands, Document doc) {
        if (brands == null || brands.isEmpty()) {
            return;
        }
        String brandsAsString = String.join(listSeparator, brands);
        doc.add(new StringField(fieldBrands, brandsAsString, Field.Store.YES));

        Set<String> brandsToIndex = processBrandList(brands);
        for (String brand : brandsToIndex) {
            doc.add(new TextField(fieldBrand, brand.toLowerCase().trim(), Field.Store.YES));
        }
        uniqueBrands.addAll(brandsToIndex);
    }

    static Set<String> processBrandList(Collection<String> brandList) {
        if (brandList == null || brandList.isEmpty()) {
            return emptySet;
        }
        Set<String> brandsSet = new HashSet<>(brandList.size());

        Set<String> brands = processList(brandList);

        for (String brand : brands) {
            if (brand != null) {

                brandsSet.add(brand);
            }
        }
        return brandsSet;
    }
}
