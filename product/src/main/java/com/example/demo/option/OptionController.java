package com.example.demo.option;


import com.example.demo.core.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class OptionController {
    private final OptionService optionService;

    /**
     * @param id
     * ProductId
     * @return
     * List'<'OptionResponse.FindByProductIdDto> optionResponses
     */
    @GetMapping("/products/{id}/options")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        List<OptionResponse.FindByProductIdDto> optionDto =
                optionService.findByProductId(id);

        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(optionDto);
        return ResponseEntity.ok(apiResult);
    }

    @GetMapping("/options")
    public ResponseEntity<?> findAll() {
        List<OptionResponse.FindAllDto> optionDto =
                optionService.findAll();

        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(optionDto);
        return ResponseEntity.ok(apiResult);
    }

    @PostMapping("/products/{id}/option/save")
    public ResponseEntity<?> save(@PathVariable Long id, @RequestBody @Valid OptionResponse.FindByProductIdDto optionDto){
        Option option = optionService.save(id, optionDto);

        if (option != null){
            ApiUtils.ApiResult<?> apiResult = ApiUtils.success(optionDto);
            return ResponseEntity.ok(apiResult);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/option/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        optionService.delete(id);

        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(null);
        return ResponseEntity.ok(apiResult);
    }

    @PostMapping("/option/update")
    public ResponseEntity<?> update(@ModelAttribute OptionResponse.FindAllDto optionDto) {
        optionService.update(optionDto);

        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(null);
        return ResponseEntity.ok(apiResult);
    }
}
