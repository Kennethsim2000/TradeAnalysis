package com.example.Demo.model;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderData {
    @JsonProperty("1. open")
    private Double open;
    @JsonProperty("2. high")
    private Double high;
    @JsonProperty("3. low")
    private Double low;
    @JsonProperty("4. close")
    private Double close;
    @JsonProperty("5. volume")
    private Integer volume;
}

