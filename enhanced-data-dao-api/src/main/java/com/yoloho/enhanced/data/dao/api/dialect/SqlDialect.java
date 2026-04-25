package com.yoloho.enhanced.data.dao.api.dialect;

/**
 * SQL 方言抽象。
 * <p>
 * 该接口用于集中收敛 MySQL、PostgreSQL 等数据库在标识符引用、分页、
 * 特殊表达式和能力支持上的差异，避免调用方和通用 DAO 模板直接感知具体数据库语法。
 * <p>
 * 设计原则：
 * <ul>
 *   <li>只抽象 enhanced-dao 当前明确需要的差异点，避免过度设计。</li>
 *   <li>公共能力由 DAO 主流程使用，数据库特性能力由具体方言声明支持情况。</li>
 *   <li>老 MySQL 项目默认行为应尽量保持不变，新项目应显式选择方言。</li>
 * </ul>
 *
 * @author neal_wei @ Apr 23, 2026
 */
public interface SqlDialect {
    String name();

    String quoteIdentifier(String identifier);

    String renderPaging(int offset, int limit);

    String renderUpdateLimit(int limit);

    String renderDeleteLimit(int limit);

    String renderJoinedStringContains(String columnName, String parameterPlaceholder);

    String renderTimestampCompare(String columnName, String operator, String parameterPlaceholder);

    boolean supportsInsertIgnore();

    boolean supportsReplace();

    boolean supportsUpdateDeleteLimit();
}
