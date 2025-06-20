package com.gd.lucene;

import com.gd.lucene.endpoint.io.CreateIndexResponse;
import com.gd.lucene.endpoint.io.SearchResponse;
import com.gd.lucene.endpoint.io.SearchResponses;
import com.gd.lucene.model.DocumentUpdateRequestBody;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
class DocumentResourceTest {

    @Inject
    Logger log;


    @Test
    void testDeleteDocEndpoint() {

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
        SearchResponse searchResponse = searchResponses.getSearchResponses().iterator().next();


        String idFound = searchResponse.getId();
        assert idFound.equals(id);

        given()
                .pathParam("uuid", id)
                .when()
                .delete("/doc/{uuid}")
                .then()
                .statusCode(204); // no content


        given()
                .pathParam("id", id) // Set the value for the {userId} parameter
                .when()
                .get("/search/id/{id}") // URL with path parameter
                .then()
                .statusCode(404);  // not found

    }


    @Test
    void testUpdateDocEndpoint() {

        String id = "42146006-d320-479f-92d6-a54d23eccd75";

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
        SearchResponse searchResponse = searchResponses.getSearchResponses().iterator().next();


        String idFound = searchResponse.getId();
        String titleFound = searchResponse.getTitle();

        assert idFound.equals(id);


        DocumentUpdateRequestBody requestBody = new DocumentUpdateRequestBody();

        String titleBeUpdatedTo = "Updated title";
        requestBody.setTitle(titleBeUpdatedTo);

        requestBody.setPrice(searchResponse.getPrice());
        requestBody.setBrands(searchResponse.getBrands());
        requestBody.setCategories(searchResponse.getCategories());
        requestBody.setDescription(searchResponse.getDescription());
        requestBody.setImageUri(searchResponse.getImageUri());

        assert !titleFound.equals(titleBeUpdatedTo);

        SearchResponse searchResponseUpdated = given()
                .pathParam("uuid", id)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/doc/{uuid}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SearchResponse.class);

        String idUpdated = searchResponseUpdated.getId();
        String titleUpdated = searchResponseUpdated.getTitle();

        assert idFound.equals(idUpdated);
        assert id.equals(idUpdated);
        assert titleBeUpdatedTo.equalsIgnoreCase(titleUpdated);
        assert !titleBeUpdatedTo.equalsIgnoreCase(titleFound);


    }

}