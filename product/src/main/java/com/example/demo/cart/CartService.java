package com.example.demo.cart;

import com.example.demo.core.error.exception.Exception400;
import com.example.demo.core.error.exception.Exception401;
import com.example.demo.core.error.exception.Exception404;
import com.example.demo.core.error.exception.Exception500;
import com.example.demo.option.Option;
import com.example.demo.option.OptionRepository;
import com.example.demo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Table(name = "cart_tb",
        indexes = {
            @Index(name = "cart_user_id_idx", columnList = "user_id"),
            @Index(name = "cart_option_id_idx", columnList = "option_id"),
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_cart_option_user", columnNames = {"user_id", "option_id"})
        })
public class CartService {

    private final CartRepository cartRepository;
    private final OptionRepository optionRepository;

    public CartResponse.FindAllDto findAll() {
        List<Cart> cartList = cartRepository.findAll();

        return new CartResponse.FindAllDto(cartList);
    }

    // 카트에 상품 추가
    @Transactional
    public void addCartList(List<CartRequest.SaveDto> requestDto, User user) {

        // 동일한 상품 예외처리(중복 제거)
        Set<Long> optionId = new HashSet<>();

        for (CartRequest.SaveDto cart : requestDto){
            if (!optionId.add(cart.getOptionId()))
                throw new Exception400("이미 동일한 상품 옵션이 있습니다. : " + cart.getOptionId());
        }

        List<Cart> cartList = requestDto.stream().map(cartDto ->{
            Option option = optionRepository.findById(cartDto.getOptionId()).orElseThrow(
                    () -> new Exception404("해당 상품 옵션을 찾을 수 없습니다. : " + cartDto.getOptionId()));
            return cartDto.toEntity(option, user);
        }).collect(Collectors.toList());

        cartList.forEach( cart -> {
            try {
                cartRepository.save(cart);
            } catch (Exception e) {
                throw new Exception500("카트에 담던 중 오류가 발생했습니다. :  " + e.getMessage());
            }
        });
    }

    @Transactional
    public CartResponse.UpdateDto update(List<CartRequest.UpdateDto> requestDto, User user) {
        List<Cart> cartList = cartRepository.findAllByUserId(user.getId());

        // 현 회원의 전체 카트 id
        List<Long> cartIds = cartList.stream().map(cart -> cart.getId()).collect(Collectors.toList());
        // 요청 카트 id
        List<Long> requestIds = requestDto.stream().map(dto -> dto.getCartId()).collect(Collectors.toList());

        if (cartIds.size() == 0)
            throw new Exception401("주문 가능한 상품이 없습니다.");

        if (requestDto.stream().distinct().count() != requestIds.size())
            throw new Exception400("동일한 카트 아이디를 주문할 수 없습니다.");

        for (Long requestId : requestIds){
            if (cartIds.contains(requestId))
                throw new Exception400("카트에 없는 상품을 주문할 수 없습니다. : " + requestId);
        }

        for (CartRequest.UpdateDto updateDto : requestDto){
            for (Cart cart : cartList) {
                if (cart.getId() == updateDto.getCartId()){
                    cart.update(updateDto.getQuantity(), cart.getPrice() * cart.getQuantity());
                }
            }
        }

        return new CartResponse.UpdateDto(cartList);
    }
}
