package com.example.demo.product;

import com.example.demo.option.Option;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    @Column(length = 100, nullable = false)
    private String productName;

    // 상품 설명
    @Column(length = 500, nullable = false)
    private String description;

    // 이미지 정보
    @Column(length = 100)
    private String image;

    // 가격
    private Long price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Option> options = new LinkedList<>();

    @Builder
    public Product(Long id, String productName, String description, String image, Long price, List<Option> options) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.image = image;
        this.price = price;
        this.options = options;
    }

    public void updateFromDto(ProductRequest.SaveDto dto){
        this.productName = dto.getProductName();
        this.description = dto.getDescription();
        this.image = dto.getImage();
        this.price = dto.getPrice();
    }
}
