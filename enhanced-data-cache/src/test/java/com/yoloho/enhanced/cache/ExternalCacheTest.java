package com.yoloho.enhanced.cache;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 外部 Redis 版 cache 测试。
 * <p>
 * 该类复用 {@link CacheTest} 的断言，连接由 {@code context.xml} 中的
 * {@code redis.test.*} 属性提供。使用 {@code mvn test -Pcache-external-integration-tests}
 * 运行。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:context.xml")
@Tag("external-integration")
public class ExternalCacheTest extends CacheTest {
}
