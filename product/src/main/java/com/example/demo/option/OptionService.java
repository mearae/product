package com.example.demo.option;

import com.example.demo.core.error.exception.Exception404;
import com.example.demo.product.Product;
import com.example.demo.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class OptionService {
    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public List<OptionResponse.FindByProductIdDto> findByProductId(Long id){
        List<Option> optionList = optionRepository.findByProductId(id);
        List<OptionResponse.FindByProductIdDto> optionResponses =
                optionList.stream().map(OptionResponse.FindByProductIdDto::new)
                .collect(Collectors.toList());

        return optionResponses;
    }

    public List<OptionResponse.FindAllDto> findAll() {
        List<Option> optionList = optionRepository.findAll();

        List<OptionResponse.FindAllDto> findAllDtos =
                optionList.stream().map(OptionResponse.FindAllDto::new)
                .collect(Collectors.toList());

        return findAllDtos;
    }

    @Transactional
    public Option save(Long productId, OptionResponse.FindByProductIdDto optionDto) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();

            Option option = Option.builder()
                    .optionName(optionDto.getOptionName())
                    .price(optionDto.getPrice())
                    .quantity(1L)
                    .product(product)
                    .build();
            optionRepository.save(option);
            return option;
        } else {
            return null;
        }
    }

    @Transactional
    public void delete(Long id) {
        optionRepository.deleteById(id);
    }

    @Transactional
    public void update(OptionResponse.FindAllDto optionDto) {
        Option option = optionRepository.findById(optionDto.getId()).orElseThrow(
                () -> new Exception404("해당 옵션을 찾을 수 없습니다. : " + optionDto.getId()));

        option.updateFromDto(optionDto);
    }
}
