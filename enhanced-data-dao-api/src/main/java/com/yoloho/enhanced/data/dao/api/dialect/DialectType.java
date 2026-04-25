package com.yoloho.enhanced.data.dao.api.dialect;

/**
 * 内置数据库方言类型。
 * <p>
 * 该枚举用于启动期配置当前 DAO 所绑定的数据源方言。{@link #AUTO} 表示由 enhanced-dao
 * 根据绑定的 {@code SqlSessionFactory/DataSource} 或已集成的 JDBC 驱动自动识别；
 * 若无法识别，应在启动期直接失败，而不是在工具类或运行路径中隐式回退。
 * <p>
 * 设计约束：
 * <ul>
 *   <li>老项目可保持默认 AUTO，由已有 MySQL 集成自动识别。</li>
 *   <li>新项目推荐显式配置 MYSQL 或 POSTGRESQL。</li>
 *   <li>多数据源场景应按 DAO/SqlSessionFactory 绑定方言，不应依赖全局默认。</li>
 * </ul>
 *
 * @author neal_wei @ Apr 23, 2026
 */
public enum DialectType {
    AUTO,
    MYSQL,
    POSTGRESQL
}
