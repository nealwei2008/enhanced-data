package com.yoloho.enhanced.data.cache.redis;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 外部 Redis 版 RedisService 测试。
 * <p>
 * 该类复用 {@link RedisServiceTest} 的断言，连接由 {@code context-redis.xml} 中的
 * {@code redis.test.*} 属性提供。使用 {@code mvn test -Pcache-external-integration-tests}
 * 运行。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:context-redis.xml")
@Tag("external-integration")
public class ExternalRedisServiceTest extends RedisServiceTest {
}
