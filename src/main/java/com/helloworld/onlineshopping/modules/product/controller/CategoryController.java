package com.helloworld.onlineshopping.modules.product.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.product.dto.CategoryCreateDTO;
import com.helloworld.onlineshopping.modules.product.service.CategoryService;
import com.helloworld.onlineshopping.modules.product.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "Category APIs")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get category tree")
    @GetMapping
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "Create category (admin)")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> create(@Valid @RequestBody CategoryCreateDTO dto) {
        categoryService.createCategory(dto);
        return Result.success();
    }
}
