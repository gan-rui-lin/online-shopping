package com.helloworld.onlineshopping.modules.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.product.dto.CategoryCreateDTO;
import com.helloworld.onlineshopping.modules.product.entity.CategoryEntity;
import com.helloworld.onlineshopping.modules.product.mapper.CategoryMapper;
import com.helloworld.onlineshopping.modules.product.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<CategoryVO> getCategoryTree() {
        List<CategoryEntity> all = categoryMapper.selectList(
            new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getStatus, 1)
                .orderByAsc(CategoryEntity::getSortOrder));

        Map<Long, List<CategoryVO>> parentMap = all.stream()
            .map(this::toVO)
            .collect(Collectors.groupingBy(CategoryVO::getParentId));

        List<CategoryVO> roots = parentMap.getOrDefault(0L, List.of());
        roots.forEach(root -> fillChildren(root, parentMap));
        return roots;
    }

    public void createCategory(CategoryCreateDTO dto) {
        CategoryEntity entity = new CategoryEntity();
        entity.setParentId(dto.getParentId());
        entity.setCategoryName(dto.getCategoryName());
        entity.setSortOrder(dto.getSortOrder());
        entity.setLevel(dto.getParentId() == 0 ? 1 : 2);
        entity.setStatus(1);
        categoryMapper.insert(entity);
    }

    private void fillChildren(CategoryVO parent, Map<Long, List<CategoryVO>> map) {
        List<CategoryVO> children = map.getOrDefault(parent.getId(), List.of());
        parent.setChildren(children);
        children.forEach(child -> fillChildren(child, map));
    }

    private CategoryVO toVO(CategoryEntity entity) {
        CategoryVO vo = new CategoryVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setCategoryName(entity.getCategoryName());
        vo.setLevel(entity.getLevel());
        return vo;
    }
}
