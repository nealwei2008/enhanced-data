package com.yoloho.enhanced.data.dao.impl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.yoloho.enhanced.data.dao.testsupport.MySqlContainerInitializer;

/**
 * MySQL 容器版手工配置 DAO 测试。
 * <p>
 * 该类复用 {@link EnhancedDaoManualTest} 的断言，加载不依赖注解扫描的
 * {@code context-no-anno.xml}，并通过 Testcontainers 提供 MySQL。使用
 * {@code mvn test -Pdao-container-integration-tests} 运行。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:context-no-anno.xml", inheritLocations = false,
        initializers = MySqlContainerInitializer.class)
@Tag("container-integration")
public class ContainerEnhancedDaoManualTest extends EnhancedDaoManualTest {
}
