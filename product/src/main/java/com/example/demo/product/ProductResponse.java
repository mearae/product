package com.example.demo.product;

import com.example.demo.option.Option;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ProductResponse {

    @Setter
    @Getter
    public static class FindAllByIdDto {
        // PK
        private Long id;

        // 상품명
        private String productName;

        // 상품 설명
        private String description;

        // 이미지 정보
        private String image;

        // 가격
        private int price;

        public FindAllByIdDto(Product product) {
            this.id = product.getId();
            this.productName = product.getProductName();
            this.description = product.getDescription();
            this.image = product.getImage();
            this.price = product.getPrice();
        }
    }

    @Setter
    @Getter
    public static class FindByIdDto {
        // PK
        private Long id;

        // 상품명
        private String productName;

        // 상품 설명
        private String description;

        // 이미지 정보
        private String image;

        // 가격
        private int price;

        private List<OptionDto> optionList;

        public FindByIdDto(Product product, List<Option> optionList) {
            this.id = product.getId();
            this.productName = product.getProductName();
            this.description = product.getDescription();
            this.image = product.getImage();
            this.price = product.getPrice();
            this.optionList = optionList.stream().map(OptionDto::new)
                    .collect(Collectors.toList());
        }

        public Product toEntity(){
            return Product.builder()
                    .productName(productName)
                    .description(description)
                    .image(image)
                    .price(price)
                    .build();
        }
    }

    @Setter
    @Getter
    public static class OptionDto{
        private Long id;
        private String optionName;
        private Long price;
        private Long quantity;

        public OptionDto(Option option){
            this.id = option.getId();
            this.optionName = option.getOptionName();
            this.price = option.getPrice();
            this.quantity = option.getQuantity();;
        }
    }
}
