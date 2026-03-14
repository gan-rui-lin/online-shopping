package com.helloworld.onlineshopping.modules.product.controller;

import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.product.dto.ProductSearchDTO;
import com.helloworld.onlineshopping.modules.product.dto.ProductSpuCreateDTO;
import com.helloworld.onlineshopping.modules.product.dto.ProductSpuUpdateDTO;
import com.helloworld.onlineshopping.modules.product.service.ProductService;
import com.helloworld.onlineshopping.modules.product.vo.ProductDetailVO;
import com.helloworld.onlineshopping.modules.product.vo.ProductSimpleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product", description = "Product APIs")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Search products")
    @GetMapping
    public Result<PageResult<ProductSimpleVO>> search(ProductSearchDTO dto) {
        return Result.success(productService.searchProducts(dto));
    }

    @Operation(summary = "Get product detail")
    @GetMapping("/{spuId}")
    public Result<ProductDetailVO> detail(@PathVariable Long spuId) {
        return Result.success(productService.getProductDetail(spuId));
    }

    @Operation(summary = "Create product (merchant)")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> create(@Valid @RequestBody ProductSpuCreateDTO dto) {
        productService.createProduct(dto);
        return Result.success();
    }

    @Operation(summary = "On shelf (merchant)")
    @PutMapping("/{spuId}/on-shelf")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> onShelf(@PathVariable Long spuId) {
        productService.updateProductStatus(spuId, 1);
        return Result.success();
    }

    @Operation(summary = "Off shelf (merchant)")
    @PutMapping("/{spuId}/off-shelf")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> offShelf(@PathVariable Long spuId) {
        productService.updateProductStatus(spuId, 2);
        return Result.success();
    }

    @Operation(summary = "Update product (merchant)")
    @PutMapping("/{spuId}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> update(@PathVariable Long spuId, @Valid @RequestBody ProductSpuUpdateDTO dto) {
        productService.updateProduct(spuId, dto);
        return Result.success();
    }

    @Operation(summary = "Get my products (merchant)")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<PageResult<ProductSimpleVO>> getMyProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productService.getMyProducts(pageNum, pageSize));
    }

    @Operation(summary = "Delete product (merchant)")
    @DeleteMapping("/{spuId}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> delete(@PathVariable Long spuId) {
        productService.deleteProduct(spuId);
        return Result.success();
    }
}
