package com.helloworld.onlineshopping.modules.product.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "product_spu")
public class EsProductDocument {

    @Id
    private Long spuId;

    @Field(type = FieldType.Long)
    private Long shopId;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String subTitle;

    @Field(type = FieldType.Keyword)
    private String brandName;

    @Field(type = FieldType.Keyword)
    private String mainImage;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal minPrice;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal maxPrice;

    @Field(type = FieldType.Integer)
    private Integer salesCount;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Integer)
    private Integer auditStatus;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
}
