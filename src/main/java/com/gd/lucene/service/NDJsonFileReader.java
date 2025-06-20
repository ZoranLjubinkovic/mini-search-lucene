package com.gd.lucene.service;

public interface NDJsonFileReader {

    int batchSize = 100;

    String fieldId = "id";
    String fieldDescription = "description";
    String fieldTitle = "title";

    String fieldBrand = "brand";
    String fieldBrands = "brands";

    String fieldCategory = "category";
    String fieldCategories = "categories";

    String fieldImageUri = "imageUri";
    String fieldPrice = "price";

    int streamDataToIndexer(Indexer indexer) throws Exception;

}
