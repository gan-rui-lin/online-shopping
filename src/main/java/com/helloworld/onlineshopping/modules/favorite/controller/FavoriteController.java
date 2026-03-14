package com.helloworld.onlineshopping.modules.favorite.controller;

import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.favorite.dto.FavoriteToggleDTO;
import com.helloworld.onlineshopping.modules.favorite.service.FavoriteService;
import com.helloworld.onlineshopping.modules.favorite.vo.FavoriteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Favorite", description = "User Favorite APIs")
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @Operation(summary = "Toggle favorite")
    @PostMapping("/toggle")
    public Result<Boolean> toggle(@Valid @RequestBody FavoriteToggleDTO dto) {
        return Result.success(favoriteService.toggleFavorite(dto.getSpuId()));
    }

    @Operation(summary = "Favorite list")
    @GetMapping("/list")
    public Result<PageResult<FavoriteVO>> list(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(favoriteService.getFavoriteList(pageNum, pageSize));
    }

    @Operation(summary = "Check if favorited")
    @GetMapping("/check/{spuId}")
    public Result<Boolean> check(@PathVariable Long spuId) {
        return Result.success(favoriteService.isFavorited(spuId));
    }
}
