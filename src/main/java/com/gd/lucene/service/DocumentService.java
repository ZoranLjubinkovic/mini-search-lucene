package com.gd.lucene.service;

import com.gd.lucene.api.exchange.SearchResponse;
import com.gd.lucene.api.exchange.SearchResponses;
import com.gd.lucene.model.Attributes;
import com.gd.lucene.model.Color;
import com.gd.lucene.model.DocumentUpdateRequestBody;
import com.gd.lucene.model.HomeAppliance;
import com.gd.lucene.model.Image;
import com.gd.lucene.model.PriceInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.gd.lucene.model.HomeAppliance.fieldId;

@ApplicationScoped
public class DocumentService {

    @Inject
    Logger logger;

    @Inject
    IndexService indexService;

    @Inject
    SearchService searchService;


    public void delete(String uuid) throws Exception {

        if (!exists(uuid)) {
            throw new Exception("Document not found: id = " + uuid);
        }
        IndexWriter indexWriter = indexService.getOrCreateIndexWriter();

        indexWriter.deleteDocuments(new Term(fieldId, uuid.trim()));
        indexWriter.flush();
        indexWriter.commit();

        if (exists(uuid)) {
            throw new Exception("Document not deleted: id = " + uuid);
        }

        logger.info("Delete: doc with uuid: " + uuid);
    }

    public SearchResponse update(String uuid, DocumentUpdateRequestBody updateRequestBody) throws Exception {

        if (!exists(uuid)) {
            throw new Exception("Document not found: id = " + uuid);
        }

        IndexWriter indexWriter = indexService.getOrCreateIndexWriter();

        HomeAppliance homeAppliance = mapToHomeAppliance(uuid, updateRequestBody);

        Document document = homeAppliance.toDocument();

        indexWriter.updateDocument(new Term(fieldId, uuid.trim()), document);

        indexWriter.flush();
        indexWriter.commit();

        SearchResponses updatedSearchResponses = searchService.searchById(uuid);

        if (updatedSearchResponses != null) {
            List<SearchResponse> searchResponsesFound = updatedSearchResponses.getSearchResponses();
            if (!searchResponsesFound.isEmpty()) {
                SearchResponse searchResponse = searchResponsesFound.get(0);
                logger.info("Updating: found existing doc: " + searchResponse + " with uuid: " + uuid + " ...");
                return searchResponse;
            }
        }
        throw new Exception("Document not found: id = " + uuid);
    }

    private boolean exists(String uuid) throws Exception {
        boolean found = true;
        SearchResponses searchResponses = searchService.searchById(uuid);
        if (searchResponses == null || searchResponses.getSearchResponses().isEmpty()) {
            found = false;
        } else {
            SearchResponse searchResponse = searchResponses.getSearchResponses().get(0);
            if (searchResponse == null) {
                found = false;
            }
        }
        return found;
    }

    private static HomeAppliance mapToHomeAppliance(String uuid, DocumentUpdateRequestBody updateRequestBody) {

        HomeAppliance homeAppliance = new HomeAppliance();

        homeAppliance.setId(uuid);

        homeAppliance.setTitle(updateRequestBody.getTitle());

        homeAppliance.setDescription(updateRequestBody.getDescription());

        homeAppliance.setBrands(updateRequestBody.getBrands());

        homeAppliance.setCategories(updateRequestBody.getCategories());


        PriceInfo priceInfo = new PriceInfo();
        priceInfo.setPrice(updateRequestBody.getPrice());
        homeAppliance.setPriceInfo(priceInfo);

        Image image = new Image();
        image.setUri(updateRequestBody.getImageUri());
        List<Image> images = new ArrayList<>();
        images.add(image);
        homeAppliance.setImages(images);

        String colorString = updateRequestBody.getColor();

        if (colorString != null) {

            Color color = new Color();
            color.setText(List.of(colorString));
            Attributes attributes = new Attributes();
            attributes.setColor(color);
            homeAppliance.setAttributes(attributes);
        }


        return homeAppliance;
    }

}
