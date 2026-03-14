package com.helloworld.onlineshopping.modules.address.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.address.dto.AddressCreateDTO;
import com.helloworld.onlineshopping.modules.address.dto.AddressUpdateDTO;
import com.helloworld.onlineshopping.modules.address.entity.UserAddressEntity;
import com.helloworld.onlineshopping.modules.address.mapper.UserAddressMapper;
import com.helloworld.onlineshopping.modules.address.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressMapper addressMapper;

    @Transactional
    public void create(AddressCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        // If setting as default, clear other defaults
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefaults(userId);
        }

        UserAddressEntity entity = new UserAddressEntity();
        entity.setUserId(userId);
        entity.setReceiverName(dto.getReceiverName());
        entity.setReceiverPhone(dto.getReceiverPhone());
        entity.setProvince(dto.getProvince());
        entity.setCity(dto.getCity());
        entity.setDistrict(dto.getDistrict());
        entity.setDetailAddress(dto.getDetailAddress());
        entity.setPostalCode(dto.getPostalCode());
        entity.setIsDefault(dto.getIsDefault());
        entity.setTagName(dto.getTagName());
        addressMapper.insert(entity);
    }

    public List<AddressVO> list() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<UserAddressEntity> entities = addressMapper.selectList(
            new LambdaQueryWrapper<UserAddressEntity>()
                .eq(UserAddressEntity::getUserId, userId)
                .orderByDesc(UserAddressEntity::getIsDefault)
                .orderByDesc(UserAddressEntity::getCreateTime));
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public void update(AddressUpdateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserAddressEntity entity = addressMapper.selectById(dto.getId());
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException("Address not found");
        }

        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefaults(userId);
        }

        if (dto.getReceiverName() != null) entity.setReceiverName(dto.getReceiverName());
        if (dto.getReceiverPhone() != null) entity.setReceiverPhone(dto.getReceiverPhone());
        if (dto.getProvince() != null) entity.setProvince(dto.getProvince());
        if (dto.getCity() != null) entity.setCity(dto.getCity());
        if (dto.getDistrict() != null) entity.setDistrict(dto.getDistrict());
        if (dto.getDetailAddress() != null) entity.setDetailAddress(dto.getDetailAddress());
        if (dto.getPostalCode() != null) entity.setPostalCode(dto.getPostalCode());
        if (dto.getIsDefault() != null) entity.setIsDefault(dto.getIsDefault());
        if (dto.getTagName() != null) entity.setTagName(dto.getTagName());
        addressMapper.updateById(entity);
    }

    public void delete(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserAddressEntity entity = addressMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException("Address not found");
        }
        addressMapper.deleteById(id);
    }

    private void clearDefaults(Long userId) {
        List<UserAddressEntity> defaults = addressMapper.selectList(
            new LambdaQueryWrapper<UserAddressEntity>()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDefault, 1));
        for (UserAddressEntity addr : defaults) {
            addr.setIsDefault(0);
            addressMapper.updateById(addr);
        }
    }

    private AddressVO toVO(UserAddressEntity entity) {
        AddressVO vo = new AddressVO();
        vo.setId(entity.getId());
        vo.setReceiverName(entity.getReceiverName());
        vo.setReceiverPhone(entity.getReceiverPhone());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setDetailAddress(entity.getDetailAddress());
        vo.setFullAddress(entity.getProvince() + entity.getCity() + entity.getDistrict() + entity.getDetailAddress());
        vo.setIsDefault(entity.getIsDefault());
        vo.setTagName(entity.getTagName());
        return vo;
    }
}
