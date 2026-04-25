package com.yoloho.enhanced.data.dao.impl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.yoloho.enhanced.data.dao.testsupport.MySqlContainerInitializer;

/**
 * MySQL 容器版 DAO XML 配置测试。
 * <p>
 * 该类复用 {@link EnhancedDaoImplTest} 的全部断言，通过 {@link MySqlContainerInitializer}
 * 启动临时 MySQL。使用 {@code mvn test -Pdao-container-integration-tests} 运行。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:context.xml", initializers = MySqlContainerInitializer.class)
@Tag("container-integration")
public class ContainerEnhancedDaoImplTest extends EnhancedDaoImplTest {
}
