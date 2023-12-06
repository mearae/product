package com.example.demo.product;

import com.example.demo.core.error.exception.Exception404;
import com.example.demo.option.Option;
import com.example.demo.option.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;

    public List<ProductResponse.FindAllByIdDto> findAll(int page) {
        Pageable pageable = PageRequest.of(page, 3);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponse.FindAllByIdDto> productDto =
                productPage.getContent().stream()
                        .map(ProductResponse.FindAllByIdDto::new)
                        .collect(Collectors.toList());

        return productDto;
    }

    public ProductResponse.FindByIdDto findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new Exception404("해당 상품을 찾을 수 없습니다. : " + id));

        // product.getId() 로 option 상품 검색
        List<Option> options = optionRepository.findByProductId(product.getId());

        // 검색이 완료된 제품 반환
        return new ProductResponse.FindByIdDto(product, options);

    }

    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void updateById(Long id, ProductRequest.SaveDto productDto) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()){
            Product product = optionalProduct.get();
            product.updateFromDto(productDto);
        }
    }

    @Transactional
    public void save(ProductRequest.SaveDto productDto) {
        System.out.println(productDto.getProductName());
        productRepository.save(productDto.toEntity());
    }

}
