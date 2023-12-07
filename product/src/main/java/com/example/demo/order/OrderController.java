package com.example.demo.order;

import com.example.demo.core.security.CustomUserDetails;
import com.example.demo.core.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders/save")
    public ResponseEntity<?> save(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        OrderResponse.FindByIdDto findByIdDto = orderService.save(customUserDetails.getUser());
        return ResponseEntity.ok(ApiUtils.success(findByIdDto));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        OrderResponse.FindByIdDto findByIdDto = orderService.findById(id);
        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(findByIdDto);
        return ResponseEntity.ok(apiResult);
    }

    @PostMapping("/orders/clear")
    public ResponseEntity<?> clear() {
        orderService.clear();
        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(null);
        return ResponseEntity.ok(apiResult);
    }
}
