package com.mpcamargo;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "foo",
        "bar",
        "baz"
})
@Generated("jsonschema2pojo")
public class Example {

    @JsonProperty("foo")
    private String foo;
    @JsonProperty("bar")
    private Integer bar;
    @JsonProperty("baz")
    private Boolean baz;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("foo")
    public String getFoo() {
        return foo;
    }

    @JsonProperty("foo")
    public void setFoo(String foo) {
        this.foo = foo;
    }

    @JsonProperty("bar")
    public Integer getBar() {
        return bar;
    }

    @JsonProperty("bar")
    public void setBar(Integer bar) {
        this.bar = bar;
    }

    @JsonProperty("baz")
    public Boolean getBaz() {
        return baz;
    }

    @JsonProperty("baz")
    public void setBaz(Boolean baz) {
        this.baz = baz;
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
