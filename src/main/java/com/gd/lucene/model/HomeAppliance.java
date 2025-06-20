package com.gd.lucene.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "brands",
        "description",
        "attributes",
        "id",
        "categories",
        "title",
        "priceInfo",
        "name",
        "availableTime",
        "uri",
        "images"
})
// @Generated("jsonschema2pojo")
public class HomeAppliance {


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

    @JsonProperty("name")
    private String name;

    @JsonProperty("availableTime")
    private String availableTime;
    //    private OffsetDateTime availableTime;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("images")
    private List<Image> images;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * No args constructor for use in serialization
     */
    public HomeAppliance() {
    }

    public HomeAppliance(List<String> brands, String description, Attributes attributes, String id, List<String> categories, String title, PriceInfo priceInfo, String name, String availableTime, String uri, List<Image> images) {
        super();
        this.brands = brands;
        this.description = description;
        this.attributes = attributes;
        this.id = id;
        this.categories = categories;
        this.title = title;
        this.priceInfo = priceInfo;
        this.name = name;
        this.availableTime = availableTime;
        this.uri = uri;
        this.images = images;
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

    @JsonProperty("attributes")
    public Attributes getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
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

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("availableTime")
    public String getAvailableTime() {
        return availableTime;
    }

    @JsonProperty("availableTime")
    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("images")
    public List<Image> getImages() {
        return images;
    }

    @JsonProperty("images")
    public void setImages(List<Image> images) {
        this.images = images;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


    @Override
    public String toString() {
        return "HomeAppliance{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
