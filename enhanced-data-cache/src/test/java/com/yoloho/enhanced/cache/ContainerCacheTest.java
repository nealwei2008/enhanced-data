package com.yoloho.enhanced.cache;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.yoloho.enhanced.data.cache.testsupport.RedisContainerInitializer;

/**
 * Redis 容器版 cache 测试。
 * <p>
 * 该类复用 {@link CacheTest} 的断言，通过 Testcontainers 启动临时 Redis。使用
 * {@code mvn test -Pcache-container-integration-tests} 运行。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:context.xml", initializers = RedisContainerInitializer.class)
@Tag("container-integration")
public class ContainerCacheTest extends CacheTest {
}
