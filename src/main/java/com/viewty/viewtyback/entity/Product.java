package com.viewty.viewtyback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 상품명
    private String name;
    // 상품 가격
    private long price;
    // 상품 이미지
    @Column(length = 2048)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",
                foreignKey = @ForeignKey(
                        name = "FK_PRODUCT_CATEGORY",
                        foreignKeyDefinition = "FOREIGN KEY (category_id) REFERENCES product_category(id) ON DELETE CASCADE"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductCategory categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id",
                foreignKey = @ForeignKey(
                            name = "FK_PRODUCT_OPTION",
                            foreignKeyDefinition = "FOREIGN KEY (option_id) REFERENCES product_options(id) ON DELETE CASCADE"))
    private ProductOption optionId;

    @Column(name = "prod_ingredients", columnDefinition = "TEXT")
    private String prodIngredients;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProductIngredientMap> ingredientMaps = new ArrayList<>();
}
