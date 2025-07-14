package com.gd.lucene.api.exchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchResponses {

    private static List<SearchResponse> emptyResponses = List.of();

    private String luceneQuery = "";

    private SortType sort;

    private long found = 0L;

    public SearchResponses() {
    }

    @JsonProperty("data")
    private List<SearchResponse> searchResponses = emptyResponses;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("error")
    private String error = null;


    public SearchResponses(String error) {
        this.error = error;
    }

    public SearchResponses(List<SearchResponse> searchResponses, long found) {
        this.searchResponses = searchResponses;
        this.found = found;
    }

    public SearchResponses(List<SearchResponse> searchResponses) {
        this.searchResponses = searchResponses;
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

    public void setSort(SortType sortType) {
        this.sort = sortType;
    }

    public SortType getSort() {
        return sort;
    }

}
