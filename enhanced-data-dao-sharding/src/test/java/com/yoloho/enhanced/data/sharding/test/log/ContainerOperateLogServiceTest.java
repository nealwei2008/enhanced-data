package com.yoloho.enhanced.data.sharding.test.log;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.yoloho.enhanced.data.sharding.test.support.MySqlContainerInitializer;

/**
 * MySQL 容器版 sharding DAO 测试。
 * <p>
 * 该类复用 {@link OperateLogServiceTest} 的断言，通过 Testcontainers 启动临时 MySQL。
 * 使用 {@code mvn test -Psharding-container-integration-tests} 运行。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:context.xml", initializers = MySqlContainerInitializer.class)
@Tag("container-integration")
public class ContainerOperateLogServiceTest extends OperateLogServiceTest {
}
