package com.yoloho.enhanced.data.dao.impl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.yoloho.enhanced.data.dao.annotations.EnableEnhancedDao;
import com.yoloho.enhanced.data.dao.annotations.EnableSqlSessionFactory;
import com.yoloho.enhanced.data.dao.testsupport.MySqlContainerInitializer;

/**
 * MySQL 容器版注解配置 DAO 测试。
 * <p>
 * 该类复用 {@link EnhancedDaoAnnotationTest} 的断言，验证
 * {@link EnableSqlSessionFactory} 和 {@link EnableEnhancedDao} 注解配置在 Testcontainers
 * MySQL 下可用。使用 {@code mvn test -Pdao-container-integration-tests} 运行。
 */
@SpringBootApplication
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ContainerEnhancedDaoAnnotationTest.class)
@ContextConfiguration(initializers = MySqlContainerInitializer.class)
@EnableSqlSessionFactory(
    name = "testSessionFactory",
    connectionUrl = "${dao.test.jdbc.url}",
    username = "${dao.test.jdbc.username}",
    password = "${dao.test.jdbc.password}"
)
@EnableEnhancedDao(
    scanPath = "com.yoloho.enhanced.data.dao.impl",
    sqlSessionFactory = "testSessionFactory"
)
@Tag("container-integration")
public class ContainerEnhancedDaoAnnotationTest extends EnhancedDaoAnnotationTest {
}
