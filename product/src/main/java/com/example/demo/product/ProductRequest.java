package com.example.demo.product;

import lombok.Getter;
import lombok.Setter;

public class ProductRequest {

    @Setter
    @Getter
    public static class SaveDto{
        // 상품명
        private String productName;

        // 상품 설명
        private String description;

        // 이미지 정보
        private String image;

        // 가격
        private Long price;

        public Product toEntity(){
            return Product.builder()
                    .productName(productName)
                    .description(description)
                    .image(image)
                    .price(price)
                    .build();
        }
    }
}
