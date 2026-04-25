package com.yoloho.enhanced.data.dao.impl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 外部 MySQL 版 DAO XML 配置测试。
 * <p>
 * 该类复用 {@link EnhancedDaoImplTest} 的全部断言，连接由 {@code context.xml} 中的
 * {@code dao.test.jdbc.*} 属性提供。使用 {@code mvn test -Pdao-external-integration-tests}
 * 运行。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:context.xml")
@Tag("external-integration")
public class ExternalEnhancedDaoImplTest extends EnhancedDaoImplTest {
}
