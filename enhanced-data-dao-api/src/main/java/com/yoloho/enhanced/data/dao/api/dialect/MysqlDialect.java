package com.yoloho.enhanced.data.dao.api.dialect;

/**
 * MySQL 方言实现。
 * <p>
 * 该实现承担两个职责：
 * <ul>
 *   <li>保留 enhanced-dao 老项目依赖的 MySQL 默认 SQL 行为。</li>
 *   <li>为新接口提供显式的 MySQL 能力声明，例如 insert ignore、replace、update/delete limit。</li>
 * </ul>
 * <p>
 * 注意：该类不承载业务语义，只负责 MySQL SQL 片段渲染和能力声明。
 *
 * @author neal_wei @ Apr 23, 2026
 */
public class MysqlDialect implements SqlDialect {
    public static final MysqlDialect INSTANCE = new MysqlDialect();

    @Override
    public String name() {
        return "mysql";
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public String renderPaging(int offset, int limit) {
        return " limit " + offset + ", " + limit;
    }

    @Override
    public String renderUpdateLimit(int limit) {
        return " limit " + limit;
    }

    @Override
    public String renderDeleteLimit(int limit) {
        return " limit " + limit;
    }

    @Override
    public String renderJoinedStringContains(String columnName, String parameterPlaceholder) {
        return String.format("concat(',', %s, ',') like %s", quoteIdentifier(columnName), parameterPlaceholder);
    }

    @Override
    public String renderTimestampCompare(String columnName, String operator, String parameterPlaceholder) {
        return columnName + " " + operator + " FROM_UNIXTIME(" + parameterPlaceholder + ")";
    }

    @Override
    public boolean supportsInsertIgnore() {
        return true;
    }

    @Override
    public boolean supportsReplace() {
        return true;
    }

    @Override
    public boolean supportsUpdateDeleteLimit() {
        return true;
    }
}
