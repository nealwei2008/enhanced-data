package com.yoloho.enhanced.data.dao.impl;

import java.io.Serializable;

import org.apache.ibatis.session.SqlSessionFactory;

import com.yoloho.enhanced.data.dao.api.MysqlEnhancedDao;

/**
 * MySQL 方言 DAO 实现。
 *
 * @author neal_wei @ Apr 24, 2026
 *
 * @param <T>
 * @param <PK>
 */
public class MysqlEnhancedDaoImpl<T, PK extends Serializable> extends AbstractEnhancedDao<T, PK>
        implements MysqlEnhancedDao<T, PK> {

    public MysqlEnhancedDaoImpl() {
    }

    public MysqlEnhancedDaoImpl(String beanClass) throws ClassNotFoundException {
        super(beanClass);
    }

    public MysqlEnhancedDaoImpl(String beanClass, String tableName) throws ClassNotFoundException {
        super(beanClass, tableName);
    }

    public MysqlEnhancedDaoImpl(String beanClass, String tableName, SqlSessionFactory sqlSessionFactory)
            throws ClassNotFoundException {
        super(beanClass, tableName, sqlSessionFactory);
    }
}
