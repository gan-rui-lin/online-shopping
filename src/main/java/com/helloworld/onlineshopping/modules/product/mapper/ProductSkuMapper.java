package com.helloworld.onlineshopping.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSkuEntity> {

    @Update("UPDATE product_sku SET stock = stock - #{count}, lock_stock = lock_stock + #{count}, " +
            "version = version + 1 WHERE id = #{skuId} AND stock >= #{count} AND version = #{version}")
    int lockStock(@Param("skuId") Long skuId, @Param("count") int count, @Param("version") int version);

    @Update("UPDATE product_sku SET stock = stock + #{count}, lock_stock = lock_stock - #{count}, " +
            "version = version + 1 WHERE id = #{skuId} AND lock_stock >= #{count}")
    int unlockStock(@Param("skuId") Long skuId, @Param("count") int count);

    @Update("UPDATE product_sku SET lock_stock = lock_stock - #{count}, " +
            "version = version + 1 WHERE id = #{skuId} AND lock_stock >= #{count}")
    int deductStock(@Param("skuId") Long skuId, @Param("count") int count);
}
