package com.yoloho.enhanced.data.dao.api;

import java.io.Serializable;
import java.util.List;

import com.yoloho.enhanced.common.annotation.NonNull;

/**
 * PostgreSQL 专属 DAO 扩展接口。
 * <p>
 * 该接口用于承载 PostgreSQL 的自然数据库特性，例如 {@code on conflict do nothing}。
 * 业务层不应直接感知该接口；推荐由项目内 Repository 或基础设施层包装为业务语义。
 *
 * @author neal_wei @ Apr 23, 2026
 *
 * @param <T>
 * @param <PK>
 */
public interface PostgreSqlEnhancedDao<T, PK extends Serializable> extends EnhancedDao<T, PK> {
    /**
     * PostgreSQL {@code insert ... on conflict (...) do nothing} 写入。
     *
     * @param bean
     *      待写入对象
     * @param conflictColumns
     *      冲突目标属性名，按实体属性名填写
     * @return 受影响行数
     */
    int insertOnConflictDoNothing(@NonNull T bean, @NonNull String... conflictColumns);

    /**
     * PostgreSQL 批量 {@code insert ... on conflict (...) do nothing} 写入。
     *
     * @param beanList
     *      待写入对象集合
     * @param conflictColumns
     *      冲突目标属性名，按实体属性名填写
     * @return 受影响行数
     */
    int insertOnConflictDoNothing(@NonNull List<T> beanList, @NonNull String... conflictColumns);

    /**
     * PostgreSQL {@code insert ... on conflict (...) do nothing} 写入，并返回填充自增主键后的对象。
     *
     * @param bean
     *      待写入对象
     * @param conflictColumns
     *      冲突目标属性名，按实体属性名填写
     * @return 写入对象
     */
    @NonNull
    T insertOnConflictDoNothingAndReturn(@NonNull T bean, @NonNull String... conflictColumns);

    /**
     * PostgreSQL 批量 {@code insert ... on conflict (...) do nothing} 写入，并返回填充自增主键后的对象集合。
     *
     * @param beanList
     *      待写入对象集合
     * @param conflictColumns
     *      冲突目标属性名，按实体属性名填写
     * @return 写入对象集合
     */
    @NonNull
    List<T> insertOnConflictDoNothingAndReturn(@NonNull List<T> beanList, @NonNull String... conflictColumns);
}
