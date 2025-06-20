package com.gd.lucene.endpoint.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResponses {

    private static List<SearchResponse> emptyResponses = List.of();

    private String luceneQuery = "";

    private SortTypes sort;

    private long found = 0L;

    public SearchResponses() {
    }

    @JsonProperty("data")
    private List<SearchResponse> searchResponses = emptyResponses;

    private Set<String> brands = new HashSet<>();
    private Set<String> categories = new HashSet<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("error")
    private String error = null;


    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Set<String> getBrands() {
        return brands;
    }

    public void setBrands(Set<String> brands) {
        this.brands = brands;
    }


    public SearchResponses(String error) {
        this.error = error;
    }

    public SearchResponses(List<SearchResponse> searchResponses, long found) {
        this.searchResponses = searchResponses;
        this.found = found;
    }

    public List<SearchResponse> getSearchResponses() {
        return searchResponses;
    }

    public void setSearchResponses(List<SearchResponse> searchResponses) {
        this.searchResponses = searchResponses;
    }

    public long getFound() {
        return found;
    }

    public void setFound(long found) {
        this.found = found;
    }

    public String getLuceneQuery() {
        return luceneQuery;
    }

    public void setLuceneQuery(String luceneQuery) {
        this.luceneQuery = luceneQuery;
    }

    public void setSort(SortTypes sortType) {
        this.sort = sortType;
    }

    public SortTypes getSort() {
        return sort;
    }

    @Override
    public String toString() {
        return "SearchResponses{" +
                "luceneQuery='" + luceneQuery + '\'' +
                ", sort=" + sort +
                ", found=" + found +
                ", searchResponses=" + searchResponses +
                ", brands=" + brands +
                ", categories=" + categories +
                ", error='" + error + '\'' +
                '}';
    }
}
