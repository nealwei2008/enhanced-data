package com.yoloho.enhanced.data.dao.impl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 外部 MySQL 版手工配置 DAO 测试。
 * <p>
 * 该类复用 {@link EnhancedDaoManualTest} 的断言，加载不依赖注解扫描的
 * {@code context-no-anno.xml}。连接由 {@code dao.test.jdbc.*} 属性提供。使用
 * {@code mvn test -Pdao-external-integration-tests} 运行。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:context-no-anno.xml", inheritLocations = false)
@Tag("external-integration")
public class ExternalEnhancedDaoManualTest extends EnhancedDaoManualTest {
}
