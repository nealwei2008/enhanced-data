package com.yoloho.enhanced.data.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;

import com.yoloho.enhanced.common.annotation.NonNull;
import com.yoloho.enhanced.data.dao.api.PostgreSqlEnhancedDao;

/**
 * PostgreSQL 方言 DAO 实现。
 *
 * @author neal_wei @ Apr 24, 2026
 *
 * @param <T>
 * @param <PK>
 */
public class PostgreSqlEnhancedDaoImpl<T, PK extends Serializable> extends AbstractEnhancedDao<T, PK>
        implements PostgreSqlEnhancedDao<T, PK> {

    public PostgreSqlEnhancedDaoImpl() {
    }

    public PostgreSqlEnhancedDaoImpl(String beanClass) throws ClassNotFoundException {
        super(beanClass);
    }

    public PostgreSqlEnhancedDaoImpl(String beanClass, String tableName) throws ClassNotFoundException {
        super(beanClass, tableName);
    }

    public PostgreSqlEnhancedDaoImpl(String beanClass, String tableName, SqlSessionFactory sqlSessionFactory)
            throws ClassNotFoundException {
        super(beanClass, tableName, sqlSessionFactory);
    }

    /**
     * PostgreSQL 标准插入语法需要 {@code insert into}。
     *
     * @param ignore
     *      MySQL 兼容参数，PostgreSQL 实现不使用
     * @param replace
     *      MySQL 兼容参数，PostgreSQL 实现不使用
     * @return PostgreSQL insert 操作关键字
     */
    @Override
    protected String getInsertOperation(boolean ignore, boolean replace) {
        return "insert into";
    }

    @Override
    public int insertOnConflictDoNothing(@NonNull T bean, @NonNull String... conflictColumns) {
        List<T> beans = java.util.Collections.singletonList(bean);
        return insertOnConflictDoNothing(beans, conflictColumns);
    }

    @Override
    public int insertOnConflictDoNothing(@NonNull List<T> beanList, @NonNull String... conflictColumns) {
        return insertWithSuffix(beanList, buildOnConflictDoNothingSuffix(conflictColumns));
    }

    @Override
    @NonNull
    public T insertOnConflictDoNothingAndReturn(@NonNull T bean, @NonNull String... conflictColumns) {
        List<T> beans = java.util.Collections.singletonList(bean);
        List<T> list = insertOnConflictDoNothingAndReturn(beans, conflictColumns);
        return list.get(0);
    }

    @Override
    @NonNull
    public List<T> insertOnConflictDoNothingAndReturn(@NonNull List<T> beanList,
            @NonNull String... conflictColumns) {
        return insertAndReturnWithSuffix(beanList, buildOnConflictDoNothingSuffix(conflictColumns));
    }
}
