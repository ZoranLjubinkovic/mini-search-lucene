package com.gd.lucene.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "wattage",
        "platematerial",
        "color",
        "numberofwaffles",
        "browningcontrol",
        "document_type",
        "tenant"
})
// // @Generated("jsonschema2pojo")
public class Attributes {

    @JsonProperty("wattage")
    private Wattage wattage;
    @JsonProperty("platematerial")
    private Platematerial platematerial;
    @JsonProperty("color")
    private Color color;
    @JsonProperty("numberofwaffles")
    private Numberofwaffles numberofwaffles;
    @JsonProperty("browningcontrol")
    private Browningcontrol browningcontrol;
    @JsonProperty("document_type")
    private DocumentType documentType;
    @JsonProperty("tenant")
    private Tenant tenant;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public Attributes() {
    }

    public Attributes(Wattage wattage, Platematerial platematerial, Color color, Numberofwaffles numberofwaffles, Browningcontrol browningcontrol, DocumentType documentType, Tenant tenant) {
        super();
        this.wattage = wattage;
        this.platematerial = platematerial;
        this.color = color;
        this.numberofwaffles = numberofwaffles;
        this.browningcontrol = browningcontrol;
        this.documentType = documentType;
        this.tenant = tenant;
    }

    @JsonProperty("wattage")
    public Wattage getWattage() {
        return wattage;
    }

    @JsonProperty("wattage")
    public void setWattage(Wattage wattage) {
        this.wattage = wattage;
    }

    @JsonProperty("platematerial")
    public Platematerial getPlatematerial() {
        return platematerial;
    }

    @JsonProperty("platematerial")
    public void setPlatematerial(Platematerial platematerial) {
        this.platematerial = platematerial;
    }

    @JsonProperty("color")
    public Color getColor() {
        return color;
    }

    @JsonProperty("color")
    public void setColor(Color color) {
        this.color = color;
    }

    @JsonProperty("numberofwaffles")
    public Numberofwaffles getNumberofwaffles() {
        return numberofwaffles;
    }

    @JsonProperty("numberofwaffles")
    public void setNumberofwaffles(Numberofwaffles numberofwaffles) {
        this.numberofwaffles = numberofwaffles;
    }

    @JsonProperty("browningcontrol")
    public Browningcontrol getBrowningcontrol() {
        return browningcontrol;
    }

    @JsonProperty("browningcontrol")
    public void setBrowningcontrol(Browningcontrol browningcontrol) {
        this.browningcontrol = browningcontrol;
    }

    @JsonProperty("document_type")
    public DocumentType getDocumentType() {
        return documentType;
    }

    @JsonProperty("document_type")
    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    @JsonProperty("tenant")
    public Tenant getTenant() {
        return tenant;
    }

    @JsonProperty("tenant")
    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
