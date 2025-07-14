package com.gd.lucene;

import com.gd.lucene.api.exchange.LoadToIndexResult;
import com.gd.lucene.api.exchange.SearchResponses;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
class SearchResourceTest {

    @Inject
    Logger log;

    @Test
    void testSearchAllEndpoint() {

        LoadToIndexResult loadToIndexResult =
                given().when()
                        .post("/index")
                        .then().statusCode(200)
                        .extract()
                        .body()
                        .as(LoadToIndexResult.class);

        int indexed = loadToIndexResult.indexed();
        int loaded = loadToIndexResult.loaded();

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
        assert size == 450;
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
        String idreturned = searchResponses.getSearchResponses().iterator().next().getId();
        assertThat(id.equals(idreturned), is(true));
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

        assertThat(searchResponses.getFound() > 0, is(true));
        String luceneQuery = searchResponses.getLuceneQuery();

        System.out.println("luceneQuery: " + luceneQuery);
        System.out.println("searchResponses.getFound(): " + searchResponses.getFound());
        assert "title:bosch description:bosch brands:bosch categories:bosch color:bosch".equals(luceneQuery);

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

        String luceneQuery = searchResponses.getLuceneQuery();

        System.out.println("luceneQuery: " + luceneQuery);

        assertThat(searchResponses.getFound() > 0, is(true));

    }


}