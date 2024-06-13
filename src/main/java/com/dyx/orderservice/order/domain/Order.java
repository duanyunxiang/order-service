package com.dyx.orderservice.order.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

// 与表之间的映射
@Table("orders")
public record Order(
        @Id
        Long id,

        String bookIsbn,
        String bookName,
        Double bookPrice,
        Integer quantity,
        OrderStatus status,

        @CreatedDate
        Instant createDate,
        @LastModifiedDate
        Instant lastModifiedDate,
        @Version
        int version
) {
    public static Order of(String bookIsbn,String bookName,Double bookPrice,Integer quantity,OrderStatus status){
        return new Order(null,bookIsbn,bookName,bookPrice,quantity,status,null,null,0);
    }
}
