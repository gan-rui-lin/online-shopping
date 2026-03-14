package com.helloworld.onlineshopping.modules.address.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.address.dto.AddressCreateDTO;
import com.helloworld.onlineshopping.modules.address.dto.AddressUpdateDTO;
import com.helloworld.onlineshopping.modules.address.service.AddressService;
import com.helloworld.onlineshopping.modules.address.vo.AddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Address", description = "User Address APIs")
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Create address")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AddressCreateDTO dto) {
        addressService.create(dto);
        return Result.success();
    }

    @Operation(summary = "Get address list")
    @GetMapping("/list")
    public Result<List<AddressVO>> list() {
        return Result.success(addressService.list());
    }

    @Operation(summary = "Update address")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AddressUpdateDTO dto) {
        addressService.update(dto);
        return Result.success();
    }

    @Operation(summary = "Delete address")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success();
    }
}
