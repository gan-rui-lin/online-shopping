package com.helloworld.onlineshopping.modules.dict.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DictService {

    private final Cache<String, Object> dictCache;

    // Simulate database lookup
    private Map<String, String> getDictFromDatabase(String dictCode) {
        System.out.println("====== [SIMULATING DB QUERY] Getting dict: " + dictCode + " ======");
        Map<String, String> data = new HashMap<>();
        if ("ORDER_STATUS".equals(dictCode)) {
            data.put("0", "Pending");
            data.put("1", "Paid");
            data.put("2", "Shipped");
            data.put("3", "Completed");
            data.put("4", "Cancelled");
        } else if ("SYS_CONFIG".equals(dictCode)) {
            data.put("FreeShippingThreshold", "99.00");
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getDictByCode(String dictCode) {
        // Use Caffeine Cache with load function
        // If data exists in cache, directly return. If not, execute the getDictFromDatabase logic.
        return (Map<String, String>) dictCache.get(dictCode, k -> getDictFromDatabase(k));
    }

    public void clearDictCache(String dictCode) {
        System.out.println("====== Clearing DB dict Cache: " + dictCode + " ======");
        dictCache.invalidate(dictCode);
    }
}
