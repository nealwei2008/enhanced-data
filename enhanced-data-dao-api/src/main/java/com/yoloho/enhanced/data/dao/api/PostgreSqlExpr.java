package com.yoloho.enhanced.data.dao.api;

import com.yoloho.enhanced.data.dao.api.dialect.DialectType;

/**
 * PostgreSQL 方言表达式入口。
 * <p>
 * 该类只放 PostgreSQL 单表条件表达式，复杂 SQL 仍应使用 MyBatis XML。
 *
 * @author neal_wei @ Apr 24, 2026
 */
public final class PostgreSqlExpr {
    private PostgreSqlExpr() {
    }

    /**
     * JSONB contains 表达式，渲染为 {@code column @> parameter::jsonb}。
     *
     * @param modelClass 实体类型
     * @param fieldName 属性名
     * @param json JSON 字符串
     * @return 表达式
     */
    public static ExprEntry jsonbContains(Class<?> modelClass, String fieldName, String json) {
        return ExprEntry.of(DialectType.POSTGRESQL, modelClass, fieldName, json,
                (dialect, column, param) -> column + " @> " + param + "::jsonb");
    }
}
