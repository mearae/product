package com.example.demo.cart;

import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class CartController {

    private final CartService cartService;

    // 카트에 상품 추가
    @PostMapping("/carts/add")
    public ResponseEntity<?> addCartList(
            @RequestBody @Valid List<CartRequest.SaveDto> requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Error error) {
        cartService.addCartList(requestDto, customUserDetails.getUser());

        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(null);
        return ResponseEntity.ok(apiResult);
    }

    // 카트 전체 상품 확인
    @GetMapping("/carts/update")
    public ResponseEntity<?> update(
            @RequestBody @Valid List<CartRequest.UpdateDto> requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Error error) {
        CartResponse.UpdateDto updateDto = cartService.update(requestDto, customUserDetails.getUser());

        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(updateDto);
        return ResponseEntity.ok(apiResult);
    }

    // 카트 전체 상품 확인
    @GetMapping("/carts")
    public ResponseEntity<?> carts(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        CartResponse.FindAllDto findAllDto = cartService.findAll();

        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(findAllDto);
        return ResponseEntity.ok(apiResult);
    }
}
