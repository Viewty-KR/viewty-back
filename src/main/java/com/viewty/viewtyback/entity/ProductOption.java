package com.viewty.viewtyback.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_options")
@NoArgsConstructor
public class ProductOption extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "op_code")
    private String opCode;

    @OneToMany(mappedBy = "optionId", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @Column
    private String name;

    @Column
    private long price;

    @Column
    private String status;

}
