package com.gd.lucene.endpoint.io;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class SearchResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("brands")
    private Set<String> brands;

    @JsonProperty("categories")
    private Set<String> categories;

    @JsonProperty("price")
    private Double price;


    @JsonProperty("imageUri")
    private String imageUri;


    public SearchResponse(String id, String title, String description, Set<String> brands, Set<String> categories, Double price, String imageUri) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.brands = brands;
        this.categories = categories;
        this.price = price;
        this.imageUri = imageUri;
    }

    public SearchResponse() {
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getBrands() {
        return brands;
    }

    public void setBrands(Set<String> brands) {
        this.brands = brands;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public String toString() {
        return "SearchResponse{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", brands=" + brands +
                ", categories=" + categories +
                ", price=" + price +
                ", imageUri='" + imageUri + '\'' +
                '}';
    }
}
