package com.gd.lucene;

import com.gd.lucene.endpoint.io.CreateIndexResponse;
import com.gd.lucene.endpoint.io.SearchResponses;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
class SearchResourceTest {

    @Inject
    Logger log;

    @Test
    void testSearchAllEndpoint() {

        CreateIndexResponse createIndexResponse =
                given().when()
                        .post("/index")
                        .then().statusCode(200)
                        .extract()
                        .body()
                        .as(CreateIndexResponse.class);

        int indexed = createIndexResponse.indexed();
        int loaded = createIndexResponse.loaded();

        assert indexed == loaded;

        SearchResponses searchResponses = given()
                .when().get("/search/all")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SearchResponses.class);

        long found = searchResponses.getFound();
        long size = searchResponses.getSearchResponses().size();
        assert found == loaded;
        assert size == 10;
    }


    @Test
    void testSearchByBrandEndpoint() {
        String brand = "Bosch";
        SearchResponses searchResponses = given()
                .pathParam("brand", brand) // Set the value for the {userId} parameter
                .when()
                .get("/search/brand/{brand}") // URL with path parameter
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SearchResponses.class);

        assertThat(searchResponses.getFound() > 0, is(true));
        assertThat(searchResponses.getBrands().size() > 0, is(true));
        assertThat(searchResponses.getBrands().iterator().next().contains(brand), is(true));
    }

    @Test
    void testSearchByIdEndpoint() {
        String id = "fd5f9246-94dd-4cef-9714-ceec97468b2f";
        SearchResponses searchResponses = given()
                .pathParam("id", id) // Set the value for the {userId} parameter
                .when()
                .get("/search/id/{id}") // URL with path parameter
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SearchResponses.class);

        assertThat(searchResponses.getFound() > 0, is(true));
        assertThat(searchResponses.getBrands().size() == 1, is(true));
        String id1 = searchResponses.getSearchResponses().iterator().next().getId();
        assertThat(id.equals(id), is(true));
    }


    @Test
    void testSearchByMultiFieldsEndpointOneBrandOnly() {

        String brand = "Bosch";

        SearchResponses searchResponses =
                given()
                        .queryParam("q", brand)
                        .when().get("/search/")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(SearchResponses.class);

        log.info("response: " + searchResponses);
        assertThat(searchResponses.getFound() > 0, is(true));
        Set<String> brands = searchResponses.getBrands();
        String luceneQuery = searchResponses.getLuceneQuery();

        assert "#brand:bosch".equals(luceneQuery);
        System.out.println(luceneQuery);
        assert brands.size() == 1;
        String foundBrand = brands.iterator().next();
        assert foundBrand.equals(brand);

        // Lucene syntax

        searchResponses =
                given()
                        .queryParam("q", "brand:Bosch")
                        .when().get("/search/")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(SearchResponses.class);

        log.info("response: " + searchResponses);
        assertThat(searchResponses.getFound() > 0, is(true));
        brands = searchResponses.getBrands();
        luceneQuery = searchResponses.getLuceneQuery();
        System.out.println(luceneQuery);

        assert "brand:bosch".equals(luceneQuery);
        System.out.println(luceneQuery);
        assert brands.size() == 1;
        foundBrand = brands.iterator().next();
        assert foundBrand.equals(brand);
    }

    @Test
    void testSearchByMultiFieldsEndpointTwoBrands() {

        Set<String> brandSet = Set.of("Bosch", "LG");
        String paramBrands = String.join(" ", brandSet);

        SearchResponses searchResponses =
                given()
                        .queryParam("q", paramBrands)
                        .when().get("/search/")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(SearchResponses.class);

        assertThat(searchResponses.getFound() > 0, is(true));
        Set<String> brands = searchResponses.getBrands();
        assert brands.size() == 2;
        assert brandSet.equals(brands);

        // Lucene syntax
        paramBrands =
                brandSet.stream()
                        .map(s -> " brand:" + s)
                        .collect(Collectors.joining(" "));

        searchResponses =
                given()
                        .queryParam("q", paramBrands)
                        .when().get("/search/")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(SearchResponses.class);

        assertThat(searchResponses.getFound() > 0, is(true));
        brands = searchResponses.getBrands();

        assert brands.size() == 2;
        assert brandSet.equals(brands);
    }

    @Test
    void testSearchByMultiFieldsEndpointBrandAndCategory() {

        String brand = "Bosch";
        String category = "Side-by-Side Refrigerators";

        Set<String> brandAndCategory = Set.of(brand, category);

        String paramBrands = String.join(" ", brandAndCategory);

        SearchResponses searchResponses =
                given()
                        .queryParam("q", paramBrands)
                        .when().get("/search/")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(SearchResponses.class);

        assertThat(searchResponses.getFound() > 0, is(true));
        Set<String> brands = searchResponses.getBrands();

        System.out.println("brands: " + brands);
        assert brands.size() == 1;
        assert brand.contains(brand);

        Set<String> categories = searchResponses.getCategories();
        System.out.println("categories: " + categories);

        assert categories.size() == 1;

        String cats = categories.iterator().next().trim();
        List<String> categoryList = Arrays.asList(cats.split(">"));
        boolean found = false;
        for (String cat : categoryList) {
            if (cat.trim().equals(category)) {
                found = true;
            }
        }

        assert found;

        // Lucene syntax
        searchResponses =
                given()
                        .queryParam("q", paramBrands)
                        .when().get("/search/")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(SearchResponses.class);

        assertThat(searchResponses.getFound() > 0, is(true));
        brands = searchResponses.getBrands();

        assert brands.size() == 1;
        assert brands.contains(brand);


        categories = searchResponses.getCategories();
        System.out.println("categories: " + categories);

        assert categories.size() == 1;

        cats = categories.iterator().next().trim();
        categoryList = Arrays.asList(cats.split(">"));
        found = false;
        for (String cat : categoryList) {
            if (cat.trim().equals(category)) {
                found = true;
            }
        }

        assert found;
    }

}