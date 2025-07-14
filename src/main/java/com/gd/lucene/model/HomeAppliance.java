package com.gd.lucene.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.util.List;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)

@JsonInclude(JsonInclude.Include.NON_NULL)
// @Generated("jsonschema2pojo")
public class HomeAppliance {


    public static final String fieldId = "id";

    public static final String fieldDescription = "description";

    public static final String fieldTitle = "title";

    public static final String fieldBrands = "brands";

    public static final String fieldCategories = "categories";

    public static final String fieldImageUri = "imageUri";

    public static final String fieldPrice = "price";

    public static final String fieldColor = "color";


    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("brands")
    private List<String> brands;

    @JsonProperty("categories")
    private List<String> categories;

    @JsonProperty("attributes")
    private Attributes attributes;

    @JsonProperty("priceInfo")
    private PriceInfo priceInfo;

    @JsonProperty("images")
    private List<Image> images;

    public HomeAppliance() {
    }


    @JsonProperty("brands")
    public List<String> getBrands() {
        return brands;
    }

    @JsonProperty("brands")
    public void setBrands(List<String> brands) {
        this.brands = brands;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("categories")
    public List<String> getCategories() {
        return categories;
    }

    @JsonProperty("categories")
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("priceInfo")
    public PriceInfo getPriceInfo() {
        return priceInfo;
    }

    @JsonProperty("priceInfo")
    public void setPriceInfo(PriceInfo priceInfo) {
        this.priceInfo = priceInfo;
    }


    @JsonProperty("images")
    public List<Image> getImages() {
        return images;
    }

    @JsonProperty("images")
    public void setImages(List<Image> images) {
        this.images = images;
    }


    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Document toDocument() {

        final Document document = new Document();

        if (getId() != null) {
            document.add(new StringField(fieldId, getId(), Field.Store.YES));
        }

        if (getTitle() != null) {
            document.add(new TextField(fieldTitle, getTitle(), Field.Store.YES));
        }
        if (getDescription() != null) {
            document.add(new TextField(fieldDescription, getDescription(), Field.Store.YES));
        }

        List<String> brandList = getBrands();
        if (brandList != null && !brandList.isEmpty()) {
            for (String b : brandList) {
                document.add(new TextField(fieldBrands, b, Field.Store.YES));
            }
        }
        List<String> categoryList = getCategories();
        if (categoryList != null && !categoryList.isEmpty()) {
            for (String c : categoryList) {
                document.add(new TextField(fieldCategories, c, Field.Store.YES));
            }
        }

        String imageUri = getImageUri();
        if (imageUri != null && !imageUri.isEmpty()) {
            document.add(new StoredField(fieldImageUri, imageUri));
        }

        for (String color : getColorList()) {
            document.add(new TextField(fieldColor, color, Field.Store.YES));
        }

        Double price = getPrice();
        if (price != null) {
            document.add(new DoubleField(fieldPrice, price, Field.Store.YES));
            document.add(new DoublePoint(fieldPrice, price));
            long value = Double.doubleToRawLongBits(price);
            document.add(new SortedNumericDocValuesField(fieldPrice, value));
        }

        return document;
    }

    public String getImageUri() {
        List<Image> images = getImages();
        if (images != null && !images.isEmpty()) {
            // one picture == 1_000 words ;)
            Image image = images.get(0);
            if (image != null) {
                return image.getUri();
            }
        }
        return null;
    }

    public Double getPrice() {

        PriceInfo priceInfo = getPriceInfo();
        if (priceInfo != null) {
            Double price = priceInfo.getPrice();

            if (price == null) {
                price = priceInfo.getOriginalPrice();
            }
            if (price != null) {
                return price; //(document, price);
            } else {
                System.err.println("Price info is null: " + getId());
            }
        }
        return null;
    }

    public List<String> getColorList() {
        return Optional.ofNullable(getAttributes())
                .flatMap(attributes -> Optional.ofNullable(attributes.getColor()))
                .map(color -> color.getText()).orElse(List.of());
    }
}
