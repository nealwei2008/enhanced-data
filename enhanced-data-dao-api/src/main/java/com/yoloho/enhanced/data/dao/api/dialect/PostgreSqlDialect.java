package com.yoloho.enhanced.data.dao.api.dialect;

/**
 * PostgreSQL 方言实现。
 * <p>
 * 该实现用于让 enhanced-dao 的公共 SQL 能力在 PostgreSQL 下以自然语义执行，
 * 而不是强行模拟 MySQL 的历史特性。对 PostgreSQL 不支持或不建议泛化的能力，
 * 这里会显式返回不支持或抛出异常，避免隐式生成风险 SQL。
 * <p>
 * 设计重点：
 * <ul>
 *   <li>PostgreSQL 是一等方言，不是 MySQL 语法的兼容模式。</li>
 *   <li>公共能力优先保持清晰，库特性能力后续通过扩展接口暴露。</li>
 *   <li>不在 V1 中通用化 update/delete limit 等非自然语义。</li>
 * </ul>
 *
 * @author neal_wei @ Apr 23, 2026
 */
public class PostgreSqlDialect implements SqlDialect {
    public static final PostgreSqlDialect INSTANCE = new PostgreSqlDialect();

    @Override
    public String name() {
        return "postgresql";
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String renderPaging(int offset, int limit) {
        return " limit " + limit + " offset " + offset;
    }

    @Override
    public String renderUpdateLimit(int limit) {
        throw new UnsupportedOperationException("PostgreSQL does not support generic UPDATE ... LIMIT");
    }

    @Override
    public String renderDeleteLimit(int limit) {
        throw new UnsupportedOperationException("PostgreSQL does not support generic DELETE ... LIMIT");
    }

    @Override
    public String renderJoinedStringContains(String columnName, String parameterPlaceholder) {
        return String.format("(',' || %s || ',') like %s", quoteIdentifier(columnName), parameterPlaceholder);
    }

    @Override
    public String renderTimestampCompare(String columnName, String operator, String parameterPlaceholder) {
        return columnName + " " + operator + " to_timestamp(" + parameterPlaceholder + ")";
    }

    @Override
    public boolean supportsInsertIgnore() {
        return false;
    }

    @Override
    public boolean supportsReplace() {
        return false;
    }

    @Override
    public boolean supportsUpdateDeleteLimit() {
        return false;
    }
}
