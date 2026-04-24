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

    @Override
    public int insertOnConflictDoNothing(@NonNull T bean, @NonNull String... conflictColumns) {
        throw new UnsupportedOperationException("PostgreSQL on conflict do nothing is not implemented yet");
    }

    @Override
    public int insertOnConflictDoNothing(@NonNull List<T> beanList, @NonNull String... conflictColumns) {
        throw new UnsupportedOperationException("PostgreSQL on conflict do nothing is not implemented yet");
    }

    @Override
    @NonNull
    public T insertOnConflictDoNothingAndReturn(@NonNull T bean, @NonNull String... conflictColumns) {
        throw new UnsupportedOperationException("PostgreSQL on conflict do nothing is not implemented yet");
    }

    @Override
    @NonNull
    public List<T> insertOnConflictDoNothingAndReturn(@NonNull List<T> beanList,
            @NonNull String... conflictColumns) {
        throw new UnsupportedOperationException("PostgreSQL on conflict do nothing is not implemented yet");
    }
}
