package com.example.demo.order;

import com.example.demo.order.item.Item;
import com.example.demo.product.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {

    @Setter
    @Getter
    public static class FindByIdDto {
        private Long id;
        private List<ProductDto> productDtos;
        private Long totalPrice;

        public FindByIdDto(Order order, List<Item> itemList) {
            this.id = order.getId();
            this.productDtos = itemList.stream()
                    .map(item -> item.getOption().getProduct()).distinct()
                    .map(product -> new ProductDto(itemList, product)).collect(Collectors.toList());

            this.totalPrice = itemList.stream()
                    .mapToLong(item -> item.getOption().getPrice() * item.getQuantity()).sum();
        }

        @Setter
        @Getter
        public class ProductDto{

            private String productName;
            private List<ItemDto> itemDtos;

            public ProductDto(List<Item> itemList, Product product) {
                this.productName = product.getProductName();
                this.itemDtos = itemList.stream()
                        .filter(item -> item.getOption().getProduct().getId() == product.getId())
                        .map(ItemDto::new).collect(Collectors.toList());
            }

            @Setter
            @Getter
            public class ItemDto{
                private Long id;
                private String optionName;
                private Long quantity;
                private Long price;

                public ItemDto(Item item) {
                    this.id = item.getId();
                    this.optionName = item.getOption().getOptionName();
                    this.quantity = item.getQuantity();
                    this.price = item.getPrice();
                }
            }
        }
    }
}
