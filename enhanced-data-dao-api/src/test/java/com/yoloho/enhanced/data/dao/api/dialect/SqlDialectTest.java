package com.yoloho.enhanced.data.dao.api.dialect;

import org.junit.Assert;
import org.junit.Test;

/**
 * SQL 方言基础行为测试。
 * <p>
 * 该测试只验证无数据库依赖的 SQL 片段渲染和能力声明，确保 MySQL 兼容行为与 PostgreSQL
 * 新方言行为在接口层有稳定边界。
 *
 * @author neal_wei @ Apr 23, 2026
 */
public class SqlDialectTest {
    @Test
    public void mysqlDialectTest() {
        SqlDialect dialect = Dialects.mysql();

        Assert.assertEquals("mysql", dialect.name());
        Assert.assertEquals("`demo_table`", dialect.quoteIdentifier("demo_table"));
        Assert.assertEquals(" limit 10, 20", dialect.renderPaging(10, 20));
        Assert.assertTrue(dialect.supportsInsertIgnore());
        Assert.assertTrue(dialect.supportsReplace());
        Assert.assertTrue(dialect.supportsUpdateDeleteLimit());
    }

    @Test
    public void postgreSqlDialectTest() {
        SqlDialect dialect = Dialects.postgresql();

        Assert.assertEquals("postgresql", dialect.name());
        Assert.assertEquals("\"demo_table\"", dialect.quoteIdentifier("demo_table"));
        Assert.assertEquals(" limit 20 offset 10", dialect.renderPaging(10, 20));
        Assert.assertFalse(dialect.supportsInsertIgnore());
        Assert.assertFalse(dialect.supportsReplace());
        Assert.assertFalse(dialect.supportsUpdateDeleteLimit());
    }
}
