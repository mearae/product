package com.example.demo.cart;

import com.example.demo.option.Option;
import com.example.demo.product.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class CartResponse {

    @Setter
    @Getter
    public static class UpdateDto{
        private List<CartDto> dtoList;

        private Long totalPrice;

        public UpdateDto(List<Cart> dtoList) {
            this.dtoList = dtoList.stream().map(CartDto::new).collect(Collectors.toList());
            this.totalPrice = dtoList.stream()
                    .mapToLong(cart -> cart.getOption().getPrice() * cart.getQuantity())
                    .sum();
        }

        @Setter
        @Getter
        public class CartDto{
            private Long cartId;

            private Long optionId;

            private String optionName;

            private Long quantity;

            private Long price;

            public CartDto(Cart cart) {
                this.cartId = cart.getId();
                this.optionId = cart.getOption().getId();
                this.optionName = cart.getOption().getOptionName();
                this.quantity = cart.getQuantity();
                this.price = cart.getPrice();
            }
        }
    }

    //  list                      list
    // [products[id, productName, cartDtos[id, optionDto[id, optionName, price], price]], totalPricing]
    @Setter
    @Getter
    public static class FindAllDto {
        List<ProductDto> products;

        private Long totalPricing;

        public FindAllDto(List<Cart> cartList) {
            this.products = cartList.stream()
                    .map(cart -> cart.getOption().getProduct()).distinct()
                    .map(product -> new ProductDto(product, cartList)).collect(Collectors.toList());


            this.totalPricing = cartList.stream()
                    .mapToLong(cart -> cart.getOption().getPrice() * cart.getQuantity())
                    .sum();
        }

        @Setter
        @Getter
        public class ProductDto {
            private Long id;

            private String productName;

            List<CartDto> cartDtos;

            public ProductDto(Product product, List<Cart> cartList){
                this.id = product.getId();
                this.productName = product.getProductName();
                this.cartDtos = cartList.stream()
                        .filter(cart -> cart.getOption().getProduct().getId() == product.getId())
                        .map(CartDto::new).collect(Collectors.toList());
            }

            @Setter
            @Getter
            public class CartDto {
                private Long id;

                private OptionDto optionDto;

                private Long price;

                public CartDto(Cart cart) {
                    this.id = cart.getId();
                    this.optionDto = new OptionDto(cart.getOption());
                    this.price = cart.getPrice();
                }

                public class OptionDto {
                    private Long id;
                    private String optionName;
                    private Long price;

                    public OptionDto(Option option) {
                        this.id = option.getId();
                        this.optionName = option.getOptionName();
                        this.price = option.getPrice();
                    }
                }
            }
        }
    }
}
