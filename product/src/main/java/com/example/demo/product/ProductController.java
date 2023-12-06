package com.example.demo.product;

import com.example.demo.core.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProductController {
    private final ProductService productService;

    // 전체 상품 확인
    @GetMapping("/products")
    public ResponseEntity<?> findAll(@RequestParam(defaultValue = "0") int page) {
        List<ProductResponse.FindAllByIdDto> productResonses = productService.findAll(page);
        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(productResonses);
        return ResponseEntity.ok(apiResult);
    }

    // 개별 상품 확인
    @GetMapping("/product/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        ProductResponse.FindByIdDto options = productService.findById(id);
        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(options);
        return ResponseEntity.ok(apiResult);
    }

    @PostMapping("/product/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productService.deleteById(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/product/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid ProductRequest.SaveDto productDto) {
        productService.updateById(id, productDto);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/product/save")
    public ResponseEntity<?> save(@RequestBody @Valid ProductRequest.SaveDto productDto) {
        System.out.println(productDto.getProductName());
        productService.save(productDto);

        return ResponseEntity.ok().build();
    }
}