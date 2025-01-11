package com.example.Demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import co.elastic.clients.util.DateTime;

//Document helps to
@Document(indexName="trade")
public class TradeOrder {
    @Id
    private Long id;

    @Field(type= FieldType.Keyword)
    private String symbol;

    @Field(type= FieldType.Date)
    private DateTime dateTime;

    @Field(type= FieldType.Double)
    private Double open;
    @Field(type= FieldType.Double)
    private Double high;

    @Field(type= FieldType.Double)
    private Double low;
    @Field(type= FieldType.Double)
    private Double close;
    @Field(type = FieldType.Integer)
    private Integer volume;


}
