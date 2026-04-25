package com.yoloho.enhanced.data.dao.testsupport;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * DAO PostgreSQL Testcontainers 初始化器。
 * <p>
 * 被 PostgreSQL 方言容器测试使用。运行方式：
 * {@code mvn test -Pdao-container-integration-tests -Dtest=ContainerPostgreSqlEnhancedDaoTest}。
 * 本地 Docker 必须可用；部分 Docker 环境需要额外传入 {@code -Dapi.version=1.40}。
 * <p>
 * 初始化器启动 {@code postgres:16}，执行 {@code postgresql-unittest.sql}，并向 Spring
 * 测试环境写入 {@code dao.test.jdbc.url}、{@code dao.test.jdbc.username}、
 * {@code dao.test.jdbc.password}。
 *
 * @author neal_wei @ Apr 25, 2026
 */
public class PostgreSqlContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("postgresql-unittest.sql");

    static {
        POSTGRESQL.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "dao.test.jdbc.url=" + POSTGRESQL.getJdbcUrl(),
                "dao.test.jdbc.username=" + POSTGRESQL.getUsername(),
                "dao.test.jdbc.password=" + POSTGRESQL.getPassword());
    }
}
