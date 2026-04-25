package com.yoloho.enhanced.data.dao.dialect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.yoloho.enhanced.data.dao.api.dialect.DialectType;

/**
 * SQL 方言解析器测试。
 * <p>
 * 测试重点是显式方言配置优先级和 AUTO 模式下基于已集成 JDBC Driver 的兼容识别行为。
 * JDBC URL 识别不依赖真实数据库连接，在单元测试中直接覆盖。
 *
 * @author neal_wei @ Apr 23, 2026
 */
public class SqlDialectResolverTest {
    @Test
    public void explicitMysqlTest() {
        Assertions.assertEquals("mysql", SqlDialectResolver.resolve(DialectType.MYSQL, null).name());
    }

    @Test
    public void explicitPostgreSqlTest() {
        Assertions.assertEquals("postgresql", SqlDialectResolver.resolve(DialectType.POSTGRESQL, null).name());
    }

    @Test
    public void autoWithMultipleDriverMarkersAndNoDataSourceShouldFailTest() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> SqlDialectResolver.resolve(DialectType.AUTO, null));
    }

    @Test
    public void detectMysqlJdbcUrlTest() {
        Assertions.assertEquals(DialectType.MYSQL,
                SqlDialectResolver.detectByJdbcUrl("jdbc:mysql://127.0.0.1:3306/test"));
    }

    @Test
    public void detectPostgreSqlJdbcUrlTest() {
        Assertions.assertEquals(DialectType.POSTGRESQL,
                SqlDialectResolver.detectByJdbcUrl("jdbc:postgresql://127.0.0.1:5432/test"));
    }
}
