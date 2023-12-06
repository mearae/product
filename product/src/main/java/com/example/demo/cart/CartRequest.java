package com.example.demo.cart;

import com.example.demo.option.Option;
import com.example.demo.user.User;
import lombok.Getter;
import lombok.Setter;

public class CartRequest {

    @Setter
    @Getter
    public static class SaveDto{
        private Long optionId;
        private Long quantity;

        public Cart toEntity(Option option, User user) {
            return Cart.builder()
                    .option(option)
                    .user(user)
                    .quantity(quantity)
                    .price(option.getPrice() * quantity)
                    .build();
        }
    }

    @Setter
    @Getter
    public static class UpdateDto{
        private Long cartId;
        private Long quantity;
    }
}
