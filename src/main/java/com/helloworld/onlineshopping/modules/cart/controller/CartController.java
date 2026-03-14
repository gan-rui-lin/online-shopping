package com.helloworld.onlineshopping.modules.cart.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.cart.dto.CartAddDTO;
import com.helloworld.onlineshopping.modules.cart.dto.CartUpdateDTO;
import com.helloworld.onlineshopping.modules.cart.service.CartService;
import com.helloworld.onlineshopping.modules.cart.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "Shopping Cart APIs")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Add item to cart")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CartAddDTO dto) {
        cartService.addItem(dto);
        return Result.success();
    }

    @Operation(summary = "Get cart list")
    @GetMapping("/list")
    public Result<CartVO> list() {
        return Result.success(cartService.getCartList());
    }

    @Operation(summary = "Update cart item")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartUpdateDTO dto) {
        cartService.updateItem(dto);
        return Result.success();
    }

    @Operation(summary = "Remove cart item")
    @DeleteMapping("/item/{skuId}")
    public Result<Void> remove(@PathVariable Long skuId) {
        cartService.removeItem(skuId);
        return Result.success();
    }
}
