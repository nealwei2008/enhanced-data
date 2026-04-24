package com.yoloho.enhanced.data.dao.dialect;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.enhanced.data.dao.api.dialect.DialectType;

/**
 * SQL 方言解析器测试。
 * <p>
 * 测试重点是显式方言配置优先级和 AUTO 模式下基于已集成 JDBC Driver 的兼容识别行为。
 * JDBC URL 与多驱动扫描场景后续可在集成测试中补充。
 *
 * @author neal_wei @ Apr 23, 2026
 */
public class SqlDialectResolverTest {
    @Test
    public void explicitMysqlTest() {
        Assert.assertEquals("mysql", SqlDialectResolver.resolve(DialectType.MYSQL, null).name());
    }

    @Test
    public void explicitPostgreSqlTest() {
        Assert.assertEquals("postgresql", SqlDialectResolver.resolve(DialectType.POSTGRESQL, null).name());
    }

    @Test
    public void autoWithMysqlDriverMarkerShouldSelectMysqlTest() {
        Assert.assertEquals("mysql", SqlDialectResolver.resolve(DialectType.AUTO, null).name());
    }
}
