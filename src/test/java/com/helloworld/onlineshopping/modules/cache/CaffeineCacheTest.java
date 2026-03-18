package com.helloworld.onlineshopping.modules.cache;

import com.helloworld.onlineshopping.modules.dict.service.DictService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class CaffeineCacheTest {

    @Autowired
    private DictService dictService;

    @AfterEach
    public void cleanup() {
        dictService.clearDictCache("ORDER_STATUS");
    }

    @Test
    public void testLocalCache() {
        // First Call: The getDictFromDatabase logic runs and we trigger "SIMULATING DB QUERY"
        Map<String, String> result1 = dictService.getDictByCode("ORDER_STATUS");
        Assertions.assertNotNull(result1);
        Assertions.assertEquals("Pending", result1.get("0"));

        // Second Call: Caffeine directly intercepts and returns result out of JVM memory
        Map<String, String> result2 = dictService.getDictByCode("ORDER_STATUS");
        Assertions.assertEquals("Shipped", result2.get("2"));

        // Confirm memory references are identical (which proves it's returning the exact cached object from Caffeine)
        Assertions.assertSame(result1, result2, "Memory references should be exactly equal due to Caffeine caching.");
        System.out.println("✅ Caffeine Local Cache Verification Passed!");
    }
}
