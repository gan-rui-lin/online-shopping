package com.helloworld.onlineshopping.modules.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helloworld.onlineshopping.modules.review.entity.ProductReviewEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReviewEntity> {
}
