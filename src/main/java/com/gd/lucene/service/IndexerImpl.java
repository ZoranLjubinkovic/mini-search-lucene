package com.gd.lucene.service;

import com.gd.lucene.endpoint.io.CreateIndexResponse;
import com.gd.lucene.model.HomeAppliance;
import com.gd.lucene.model.Image;
import com.gd.lucene.model.PriceInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gd.lucene.service.NDJsonFileReader.fieldDescription;
import static com.gd.lucene.service.NDJsonFileReader.fieldId;
import static com.gd.lucene.service.NDJsonFileReader.fieldImageUri;
import static com.gd.lucene.service.NDJsonFileReader.fieldPrice;
import static com.gd.lucene.service.NDJsonFileReader.fieldTitle;

@ApplicationScoped
public class IndexerImpl implements Indexer {

    @Inject
    Logger log;

    @Inject
    private LuceneService luceneService;


    @Inject
    NDJsonFileReader newlineDelimitedJsonFileReader;

    IndexWriter indexWriter;

    @Override
    public CreateIndexResponse initLoading() throws Exception {

        int load = 0;
        int numDocs = 0;
        indexWriter = luceneService.indexWriter();

        indexWriter.deleteAll();
        try {
            load = newlineDelimitedJsonFileReader.streamDataToIndexer(this);
            try {
                indexWriter.close();
            } catch (IOException e) {
                log.error("Error closing indexWriter", e);
            }
            IndexReader reader = DirectoryReader.open(luceneService.directory());
            numDocs = reader.numDocs();
            reader.close();
        } catch (Exception e) {
            log.error("Error loading index", e);
        }
        return new CreateIndexResponse(load, numDocs);
    }

    private final List<Document> batch = new ArrayList<>(NDJsonFileReader.batchSize);

    static FieldType fieldTypeForUuid = new FieldType();

    static {
        fieldTypeForUuid.setIndexOptions(IndexOptions.DOCS);
        fieldTypeForUuid.setTokenized(false);
        fieldTypeForUuid.setStored(true);
    }


    @Override
    public void addToIndexCallback(List<HomeAppliance> homeAppliances) throws Exception {

        batch.clear();

        for (HomeAppliance ha : homeAppliances) {
            Document doc = new Document();

            Field idField = new Field(fieldId, ha.getId(), fieldTypeForUuid);
            // search by id: update, delete !
            doc.add(idField);

            doc.add(new TextField(fieldDescription, ha.getDescription() != null ? ha.getDescription().toLowerCase() : null, Field.Store.YES));
            doc.add(new TextField(fieldTitle, ha.getTitle() != null ? ha.getTitle().toLowerCase() : null, Field.Store.YES));

            Indexer.processBrands(ha.getBrands(), doc);

            Indexer.processCategories(ha.getCategories(), doc);

            List<Image> images = ha.getImages();
            if (images != null && !images.isEmpty()) {
                // one picture == 1_000 words ;)
                Image image = images.get(0);
                if (image != null) {
                    doc.add(new StringField(fieldImageUri, image.getUri(), Field.Store.YES));
                }
            }
            PriceInfo priceInfo = ha.getPriceInfo();
            if (priceInfo != null) {
                Double price = priceInfo.getPrice();

                if (price == null) {
                    price = priceInfo.getOriginalPrice();
                }
                if (price != null) {
                    processPrice(doc, price);
                } else {
                    log.warn("Price is null: " + ha.getId());
                }
            } else {
                log.warn("Price info is null: " + ha.getId());
            }

            batch.add(doc);
        }

        indexWriter.addDocuments(batch);
        indexWriter.commit();
    }

    static void processPrice(Document doc, Double price) {
        try {
            DoubleField doubleField = new DoubleField(fieldPrice, price, Field.Store.YES);
            doc.add(doubleField);
            long value = Double.doubleToRawLongBits(price);
            SortedNumericDocValuesField sortedNumericDocValuesField = new SortedNumericDocValuesField(fieldPrice, value);
            doc.add(sortedNumericDocValuesField);
        } catch (Exception e) {
            System.err.println("Error getting price: " + e);
        }
    }
}

