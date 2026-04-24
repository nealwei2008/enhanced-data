package com.yoloho.enhanced.data.dao.impl;

import java.io.Serializable;

import org.apache.ibatis.session.SqlSessionFactory;

import com.yoloho.enhanced.data.dao.api.EnhancedDao;

/**
 * 默认公共 DAO 实现。
 *
 * @author neal_wei @ Apr 24, 2026
 *
 * @param <T>
 * @param <PK>
 */
public class EnhancedDaoImpl<T, PK extends Serializable> extends AbstractEnhancedDao<T, PK>
        implements EnhancedDao<T, PK> {

    public EnhancedDaoImpl() {
    }

    public EnhancedDaoImpl(String beanClass) throws ClassNotFoundException {
        super(beanClass);
    }

    public EnhancedDaoImpl(String beanClass, String tableName) throws ClassNotFoundException {
        super(beanClass, tableName);
    }

    public EnhancedDaoImpl(String beanClass, String tableName, SqlSessionFactory sqlSessionFactory)
            throws ClassNotFoundException {
        super(beanClass, tableName, sqlSessionFactory);
    }
}
