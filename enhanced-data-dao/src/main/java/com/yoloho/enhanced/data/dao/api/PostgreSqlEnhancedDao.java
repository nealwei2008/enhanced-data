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
    int insertOnConflictDoNothing(@NonNull T bean, @NonNull String... conflictColumns);

    int insertOnConflictDoNothing(@NonNull List<T> beanList, @NonNull String... conflictColumns);

    @NonNull
    T insertOnConflictDoNothingAndReturn(@NonNull T bean, @NonNull String... conflictColumns);

    @NonNull
    List<T> insertOnConflictDoNothingAndReturn(@NonNull List<T> beanList, @NonNull String... conflictColumns);
}
