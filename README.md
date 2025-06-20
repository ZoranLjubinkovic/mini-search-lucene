# Mini-search Lucene

This project uses Quarkus, Lucene
and combinatoricslib3 (for Permutations ) <https://github.com/dpaukov/combinatoricslib3>

## 'Software engineer to Search engineer' course.

### Schema mappings
Not all fields from json data are used:
```java

    static FieldType fieldTypeForUuid = new FieldType();

    static {
        fieldTypeForUuid.setIndexOptions(IndexOptions.DOCS);
        fieldTypeForUuid.setTokenized(false);
        fieldTypeForUuid.setStored(true);
    }
    String fieldId = "id"; // fieldTypeForUuid - searchable, not parsed
    String fieldDescription = "description"; // TextField
    String fieldTitle = "title"; // TextField

    String fieldBrand = "brand"; // TextField
    String fieldBrands = "brands"; // StringField

    String fieldCategory = "category";  // TextField
    String fieldCategories = "categories";  // StringField

    String fieldImageUri = "imageUri"; // StringField
    String fieldPrice = "price"; // DoubleField, SortedNumericDocValuesField
```
Search logic:

[EdgeNGramAnalyzer](src/main/java/com/gd/lucene/service/utils/EdgeNGramAnalyzer.java)

[UserQueryProcessor](src/main/java/com/gd/lucene/service/utils/UserQueryProcessor.java)

     * Query for search in fields: Brand, Category, Title, Description
     * 
     * analyze 'raw' user query and detect usage of Lucene query syntax: ie.
     *           brand:Bosch
     *  OR
     *  try to 'detect' Category/Brand by searching pre-populated 'cache' of categories and brands,
     *  and properly make Query for found category(-ies) and / or brand(-s) in right field(s): ie.
     *  raw query:
     *       LG Freestanding Ranges  something
     *  should create lucene Query:
     *        "#brand:lg #category:\"freestanding ranges\" title:something description:something",
     * 
     *  remaining words from user query will be used to find by Title and Desc.

## All tests:

```shell
./mvnw clean test
```

## Run main class:

```shell
./mvnw quarkus:run
```

## Open <http://localhost:8080/q/swagger-ui/>


## Packaging the application

The application can be packaged using:

```shell script
./mvnw package
```


```bash
DOCKER_DEFAULT_PLATFORM="linux/amd64"  \
docker build -f src/main/Docker/Dockerfile.jvm \
-t europe-west6-docker.pkg.dev/gd-gcp-em-search-re-training/trainingartifacts/zlj-lucene-mini_search:0.1 .
```

```bash
docker run -i --rm -p 8080:8080 \
europe-west6-docker.pkg.dev/gd-gcp-em-search-re-training/trainingartifacts/zlj-lucene-mini_search:0.1
```



## Open <http://localhost:8080/q/swagger-ui/>

## Deploy to GCP

```bash
docker push \
europe-west6-docker.pkg.dev/gd-gcp-em-search-re-training/trainingartifacts/zlj-lucene-mini_search:0.1
```



# Open <https://zlj-lucene-mini-search-gixfoxo4pq-ew.a.run.app/q/swagger-ui/>



## Author

### Zoran Ljubinković [zljubinkovic@griddynamics.com](mailto:zljubinkovic@griddynamics.com)




