package com.example.Demo.model;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Document helps to
@Document(indexName="trade")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TradeOrder {
    @Id
    private Long id;

    @Field(type= FieldType.Keyword)
    private String symbol;

    @Field(type=FieldType.Date, format={}, pattern={"uuuu-MM-dd'T'HH:mm"})
    private LocalDateTime date;

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
