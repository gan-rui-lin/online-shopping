package com.helloworld.onlineshopping.modules.user.service;

import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.user.dto.ChangePasswordDTO;
import com.helloworld.onlineshopping.modules.user.dto.UpdateProfileDTO;
import com.helloworld.onlineshopping.modules.user.entity.UserEntity;
import com.helloworld.onlineshopping.modules.user.entity.UserRoleEntity;
import com.helloworld.onlineshopping.modules.user.entity.RoleEntity;
import com.helloworld.onlineshopping.modules.user.mapper.UserMapper;
import com.helloworld.onlineshopping.modules.user.mapper.UserRoleMapper;
import com.helloworld.onlineshopping.modules.user.mapper.RoleMapper;
import com.helloworld.onlineshopping.modules.user.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "user:info", key = "T(com.helloworld.onlineshopping.common.security.SecurityUtil).getCurrentUserId()")
    public UserInfoVO getCurrentUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        List<UserRoleEntity> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRoleEntity>().eq(UserRoleEntity::getUserId, userId));
        List<Long> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        List<String> roleCodes = List.of();
        if (!roleIds.isEmpty()) {
            roleCodes = roleMapper.selectBatchIds(roleIds).stream()
                .map(RoleEntity::getRoleCode).collect(Collectors.toList());
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setUserType(user.getUserType());
        vo.setRoles(roleCodes);
        return vo;
    }

    @CacheEvict(value = "user:info", key = "T(com.helloworld.onlineshopping.common.security.SecurityUtil).getCurrentUserId()")
    public void updateProfile(UpdateProfileDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        userMapper.updateById(user);
    }

    @CacheEvict(value = "user:info", key = "T(com.helloworld.onlineshopping.common.security.SecurityUtil).getCurrentUserId()")
    public void changePassword(ChangePasswordDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}
