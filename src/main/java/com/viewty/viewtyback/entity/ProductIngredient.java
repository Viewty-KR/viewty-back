package com.viewty.viewtyback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.Primary;

@Entity
@Table(name = "product_ingredient", indexes = {@Index(name = "idx_ingredient_name", columnList = "name")})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProductIngredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column
    private String engName;

    @Column
    private String casno;

    @Column
    private String definition;

    @Column
    private String synonym;

    @Column
    private String functional;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "prod_ingredient_id", // 요구하신 컬럼명
//            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT) // 물리적 FK 제약 조건 생성 방지
//    )
//    private Product product;
}
