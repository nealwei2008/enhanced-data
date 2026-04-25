package com.yoloho.enhanced.data.dao.impl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.yoloho.enhanced.data.dao.annotations.EnableEnhancedDao;
import com.yoloho.enhanced.data.dao.annotations.EnableSqlSessionFactory;

/**
 * 外部 MySQL 版注解配置 DAO 测试。
 * <p>
 * 该类复用 {@link EnhancedDaoAnnotationTest} 的断言，连接信息从
 * {@code dao.test.jdbc.url}、{@code dao.test.jdbc.username}、{@code dao.test.jdbc.password}
 * 读取，未提供时使用类上的默认值。使用 {@code mvn test -Pdao-external-integration-tests}
 * 运行。
 */
@SpringBootApplication
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ExternalEnhancedDaoAnnotationTest.class)
@EnableSqlSessionFactory(
    name = "testSessionFactory",
    connectionUrl = "${dao.test.jdbc.url:jdbc:mysql://192.168.110.240:3306/test?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true}",
    username = "${dao.test.jdbc.username:test}",
    password = "${dao.test.jdbc.password:Test@123}"
)
@EnableEnhancedDao(
    scanPath = "com.yoloho.enhanced.data.dao.impl",
    sqlSessionFactory = "testSessionFactory"
)
@Tag("external-integration")
public class ExternalEnhancedDaoAnnotationTest extends EnhancedDaoAnnotationTest {
}
