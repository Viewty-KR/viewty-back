package com.viewty.viewtyback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "product_ingredient_map",
        indexes = {
            @Index(name = "idx_pim_product_id", columnList = "product_id"),
            @Index(name = "idx_pim_ingredient_id", columnList = "ingredient_id")
        })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProductIngredientMap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, 
                foreignKey = @ForeignKey(name = "FK_PIM_PRODUCT"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false,
                foreignKey = @ForeignKey(name = "FK_PIM_INGREDIENT"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductIngredient ingredient;
}
