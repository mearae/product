package com.example.demo.option;

import lombok.Getter;
import lombok.Setter;

public class OptionRequest {

    @Setter
    @Getter
    public static class SaveDto{
        private Long productId;

        // 옵션상품 이름, 필수 입력값
        private String optionName;

        // 옵션 상품 가격
        private Long price;

        // 옵션 상품 수량
        private Long quantity;

        public Option toEntity(){
            return Option.builder()
                    .optionName(optionName)
                    .price(price)
                    .quantity(quantity)
                    .build();
        }
    }
}
