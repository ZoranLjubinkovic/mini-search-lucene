package com.gd.lucene;

import com.gd.lucene.endpoint.io.CreateIndexResponse;
import com.gd.lucene.endpoint.io.SearchResponses;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class IndexerResourceTest {

    @Inject
    Logger log;

    @Test
    void testCreateIndexEndpoint() {

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

}