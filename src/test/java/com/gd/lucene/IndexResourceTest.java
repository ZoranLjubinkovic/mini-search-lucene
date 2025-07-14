package com.gd.lucene;

import com.gd.lucene.api.exchange.LoadToIndexResult;
import com.gd.lucene.api.exchange.SearchResponses;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class IndexResourceTest {

    @Inject
    Logger log;

    @Test
    void testCreateIndexEndpoint() {

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

}