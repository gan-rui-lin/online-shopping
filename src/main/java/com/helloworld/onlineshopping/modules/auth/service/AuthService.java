package com.helloworld.onlineshopping.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.utils.JwtUtil;
import com.helloworld.onlineshopping.modules.auth.dto.LoginDTO;
import com.helloworld.onlineshopping.modules.auth.dto.RegisterDTO;
import com.helloworld.onlineshopping.modules.auth.vo.LoginVO;
import com.helloworld.onlineshopping.modules.user.entity.RoleEntity;
import com.helloworld.onlineshopping.modules.user.entity.UserEntity;
import com.helloworld.onlineshopping.modules.user.entity.UserRoleEntity;
import com.helloworld.onlineshopping.modules.user.mapper.RoleMapper;
import com.helloworld.onlineshopping.modules.user.mapper.UserMapper;
import com.helloworld.onlineshopping.modules.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.token-head}")
    private String tokenHead;

    @Transactional
    public void register(RegisterDTO dto) {
        // Check username uniqueness
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("Username already exists");
        }

        // Check phone uniqueness
        if (dto.getPhone() != null) {
            Long phoneCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getPhone, dto.getPhone()));
            if (phoneCount > 0) {
                throw new BusinessException("Phone number already registered");
            }
        }

        // Create user
        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        user.setUserType(1); // default buyer
        userMapper.insert(user);

        // Assign buyer role
        RoleEntity buyerRole = roleMapper.selectOne(
            new LambdaQueryWrapper<RoleEntity>().eq(RoleEntity::getRoleCode, "ROLE_BUYER"));
        if (buyerRole != null) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(user.getId());
            userRole.setRoleId(buyerRole.getId());
            userRoleMapper.insert(userRole);
        }
    }

    public LoginVO login(LoginDTO dto) {
        UserEntity user = userMapper.selectOne(
            new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "Invalid username or password");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("Account is disabled");
        }

        // Get roles
        List<UserRoleEntity> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRoleEntity>().eq(UserRoleEntity::getUserId, user.getId()));
        List<Long> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        List<String> roleCodes = List.of();
        if (!roleIds.isEmpty()) {
            List<RoleEntity> roles = roleMapper.selectBatchIds(roleIds);
            roleCodes = roles.stream().map(RoleEntity::getRoleCode).collect(Collectors.toList());
        }

        // Generate token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), Map.of("roles", roleCodes));

        // Update last login time
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // Build response
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setTokenHead(tokenHead);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setUserType(user.getUserType());
        vo.setRoles(roleCodes);
        return vo;
    }
}
