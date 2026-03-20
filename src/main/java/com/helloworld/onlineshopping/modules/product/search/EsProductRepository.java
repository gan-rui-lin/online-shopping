package com.helloworld.onlineshopping.modules.product.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EsProductRepository extends ElasticsearchRepository<EsProductDocument, Long> {
}
