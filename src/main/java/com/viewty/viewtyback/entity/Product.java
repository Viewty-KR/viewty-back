package com.viewty.viewtyback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@NoArgsConstructor
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 상품명
    private String name;
    // 상품 가격
    private String price;
    // 상품 이미지
    private String imgUrl;
    // 용량/중량
    private String capacity;
    // 주요 사양
    private String specifications;
    // 사용 기한
    private String expiryDate;
    // 사용 방법
    private String usageMethod;
    // 제조업자
    private String manufacturer;
    // 제조국
    private String country;
    // 기능성 여부
    private String isFunctional;
    // 주의사항(@Lob)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String precautions;
    // 품질보증기준
    private String qa;
    // cs 전화번호
    private String csNumber;
    // 배송비(일반)
    private String deliveryFee;
    // 배송비(제주/도서산간)
    private String deliveryJejuFee;


//     전 성분(@Lob)
//    @Lob
//    @Column(columnDefinition = "TEXT")
//    private String ingredients;

    @Builder
    public Product(String name, String price, String imgUrl, String capacity, String specifications, String expiryDate, String usageMethod, String manufacturer, String country, String isFunctional, String precautions, String qa, String csNumber, String deliveryFee, String deliveryJejuFee) {
        this.name = name;
        this.price = price;
        this.imgUrl = imgUrl;
        this.capacity = capacity;
        this.specifications = specifications;
        this.expiryDate = expiryDate;
        this.usageMethod = usageMethod;
        this.manufacturer = manufacturer;
        this.country = country;
        this.isFunctional = isFunctional;
        this.precautions = precautions;
        this.qa = qa;
        this.csNumber = csNumber;
        this.deliveryFee = deliveryFee;
        this.deliveryJejuFee = deliveryJejuFee;
    }
}
