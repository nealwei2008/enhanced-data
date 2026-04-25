package com.yoloho.enhanced.data.dao.api.dialect;

/**
 * 方言实例入口。
 * <p>
 * 该类提供 enhanced-dao 内置方言的统一访问方式，避免调用方直接 new 具体实现。
 * V1 阶段采用无状态单例，便于保持兼容、降低 Spring 注入复杂度；后续如果方言需要配置项，
 * 可以在这里扩展为工厂方法。
 *
 * @author neal_wei @ Apr 23, 2026
 */
public final class Dialects {
    private Dialects() {
    }

    /**
     * 获取内置 MySQL 方言实例。
     *
     * @return MySQL 方言
     */
    public static SqlDialect mysql() {
        return MysqlDialect.INSTANCE;
    }

    /**
     * 获取内置 PostgreSQL 方言实例。
     *
     * @return PostgreSQL 方言
     */
    public static SqlDialect postgresql() {
        return PostgreSqlDialect.INSTANCE;
    }
}
