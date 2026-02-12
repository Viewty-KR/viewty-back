package com.viewty.viewtyback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restricted_ingredient")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestrictedIngredient {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String division;

    @Column(nullable = false)
    private String name;

    @Column(name = "eng_name")
    private String engName;
    @Column(name = "casNo")
    private String casNo;
    @Column(columnDefinition = "TEXT")
    private String synonym;


}
