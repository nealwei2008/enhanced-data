package com.yoloho.enhanced.data.dao.testsupport;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;

/**
 * DAO MySQL Testcontainers 初始化器。
 * <p>
 * 被 {@code @Tag("container-integration")} 测试类使用。运行方式：
 * {@code mvn test -Pdao-container-integration-tests}。本地 Docker 必须可用；部分 Docker
 * 环境需要额外传入 {@code -Dapi.version=1.40}。
 * <p>
 * 初始化器启动 {@code mysql:8.4}，执行 {@code unittest.sql}，并向 Spring 测试环境写入
 * {@code dao.test.jdbc.url}、{@code dao.test.jdbc.username}、{@code dao.test.jdbc.password}。
 *
 * @author neal_wei @ Apr 25, 2026
 */
public class MySqlContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("unittest.sql");

    static {
        MYSQL.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "dao.test.jdbc.url=" + jdbcUrl(),
                "dao.test.jdbc.username=" + MYSQL.getUsername(),
                "dao.test.jdbc.password=" + MYSQL.getPassword());
    }

    private String jdbcUrl() {
        String separator = MYSQL.getJdbcUrl().contains("?") ? "&" : "?";
        return MYSQL.getJdbcUrl() + separator + "allowMultiQueries=true";
    }
}
