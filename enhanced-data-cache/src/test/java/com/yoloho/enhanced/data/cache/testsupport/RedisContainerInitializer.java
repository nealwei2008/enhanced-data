package com.yoloho.enhanced.data.cache.testsupport;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Redis Testcontainers 初始化器。
 * <p>
 * 被 cache 模块 {@code @Tag("container-integration")} 测试类使用。运行方式：
 * {@code mvn test -Pcache-container-integration-tests}。本地 Docker 必须可用。
 * <p>
 * 初始化器启动 {@code redis:7-alpine}，并向 Spring 测试环境写入
 * {@code redis.test.host}、{@code redis.test.port}、{@code redis.test.password}。
 *
 * @author neal_wei @ Apr 25, 2026
 */
public class RedisContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String PASSWORD = "test";

    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", PASSWORD);

    static {
        REDIS.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "redis.test.host=" + REDIS.getHost(),
                "redis.test.port=" + REDIS.getMappedPort(6379),
                "redis.test.password=" + PASSWORD);
    }
}
