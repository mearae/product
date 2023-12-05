package com.example.demo.option;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class OptionResponse {

    @NoArgsConstructor
    @Setter
    @Getter
    public static class FindByProductIdDto{
        private Long id;

        private Long productId;

        // 옵션상품 이름, 필수 입력값
        private String optionName;

        // 옵션 상품 가격
        private Long price;

        // 옵션 상품 수량
        private Long quantity;

        public FindByProductIdDto(Option option){
            this.id = option.getId();
            this.productId = option.getProduct().getId();
            this.optionName = option.getOptionName();
            this.price = option.getPrice();
            this.quantity = option.getQuantity();
        }
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class FindAllDto{
        private Long id;

        private Long productId;

        // 옵션상품 이름, 필수 입력값
        private String optionName;

        // 옵션 상품 가격
        private Long price;

        // 옵션 상품 수량
        private Long quantity;

        public FindAllDto(Option option){
            this.id = option.getId();
            this.productId = option.getProduct().getId();
            this.optionName = option.getOptionName();
            this.price = option.getPrice();
            this.quantity = option.getQuantity();
        }
    }
}
