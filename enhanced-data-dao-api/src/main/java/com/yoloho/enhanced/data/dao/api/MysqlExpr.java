package com.yoloho.enhanced.data.dao.api;

import com.yoloho.enhanced.data.dao.api.dialect.DialectType;

/**
 * MySQL 方言表达式入口。
 * <p>
 * 该类只放 MySQL 单表条件表达式，复杂 SQL 仍应使用 MyBatis XML。
 *
 * @author neal_wei @ Apr 24, 2026
 */
public final class MysqlExpr {
    private MysqlExpr() {
    }

    /**
     * MySQL {@code find_in_set} 表达式，渲染为 {@code find_in_set(parameter, column)}。
     *
     * @param modelClass 实体类型
     * @param fieldName 属性名
     * @param value 待匹配值
     * @return 表达式
     */
    public static ExprEntry findInSet(Class<?> modelClass, String fieldName, String value) {
        return ExprEntry.of(DialectType.MYSQL, modelClass, fieldName, value,
                (dialect, column, param) -> "find_in_set(" + param + ", " + column + ")");
    }
}
