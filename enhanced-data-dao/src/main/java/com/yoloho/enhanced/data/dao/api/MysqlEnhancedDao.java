package com.yoloho.enhanced.data.dao.api;

import java.io.Serializable;
import java.util.List;

import com.yoloho.enhanced.common.annotation.NonNull;

/**
 * MySQL 专属 DAO 扩展接口。
 * <p>
 * 该接口承载 MySQL 特有的写入语义，例如 {@code insert ignore} 和 {@code replace into}。
 * 这些能力不再推荐放在 {@link EnhancedDao} 公共接口中使用；老项目保留旧入口用于兼容，
 * 新 MySQL 项目应优先使用本接口中的显式方法。
 *
 * @author neal_wei @ Apr 23, 2026
 *
 * @param <T>
 * @param <PK>
 */
public interface MysqlEnhancedDao<T, PK extends Serializable> extends EnhancedDao<T, PK> {
    /**
     * 插入，重复主键时是否忽略插入。
     * <p>
     * 废弃原因：布尔参数语义弱，推荐改为显式的 {@link #insertIgnore(Object)}。
     *
     * @param bean
     * @param ignore
     * @return
     */
    @Deprecated
    int insert(@NonNull T bean, boolean ignore);

    /**
     * 插入并返回插入后的 bean。
     * <p>
     * 废弃原因：布尔参数语义弱，推荐改为显式的 {@link #insertIgnoreAndReturn(Object)}。
     *
     * @param bean
     * @param ignore
     * @return
     */
    @NonNull
    @Deprecated
    T insertAndReturn(@NonNull T bean, boolean ignore);

    /**
     * 批量插入，重复主键时是否忽略插入。
     * <p>
     * 废弃原因：布尔参数语义弱，推荐改为显式的 {@link #insertIgnore(List)}。
     *
     * @param beanList
     * @param ignore
     * @return
     */
    @Deprecated
    int insert(@NonNull List<T> beanList, boolean ignore);

    /**
     * 批量插入并返回插入后的 bean。
     * <p>
     * 废弃原因：布尔参数语义弱，推荐改为显式的 {@link #insertIgnoreAndReturn(List)}。
     *
     * @param beanList
     * @param ignore
     * @return
     */
    @NonNull
    @Deprecated
    List<T> insertAndReturn(@NonNull List<T> beanList, boolean ignore);

    /**
     * MySQL {@code insert ignore}。
     *
     * @param bean
     *      待写入对象
     * @return 受影响行数
     */
    int insertIgnore(@NonNull T bean);

    /**
     * 批量 MySQL {@code insert ignore}。
     *
     * @param beanList
     *      待写入对象集合
     * @return 受影响行数
     */
    int insertIgnore(@NonNull List<T> beanList);

    /**
     * MySQL {@code insert ignore} 并返回写入后的对象。
     *
     * @param bean
     *      待写入对象
     * @return 写入对象
     */
    @NonNull
    T insertIgnoreAndReturn(@NonNull T bean);

    /**
     * 批量 MySQL {@code insert ignore} 并返回写入后的对象集合。
     *
     * @param beanList
     *      待写入对象集合
     * @return 写入对象集合
     */
    @NonNull
    List<T> insertIgnoreAndReturn(@NonNull List<T> beanList);

    /**
     * MySQL replace into 语义。
     *
     * @param bean
     * @return
     */
    int replace(@NonNull T bean);

    /**
     * MySQL replace into 语义。
     *
     * @param beanList
     * @return
     */
    int replace(@NonNull List<T> beanList);

    /**
     * MySQL {@code replace into} 并返回写入后的对象。
     *
     * @param bean
     *      待写入对象
     * @return 写入对象
     */
    @NonNull
    T replaceAndReturn(@NonNull T bean);

    /**
     * 批量 MySQL {@code replace into} 并返回写入后的对象集合。
     *
     * @param beanList
     *      待写入对象集合
     * @return 写入对象集合
     */
    @NonNull
    List<T> replaceAndReturn(@NonNull List<T> beanList);
}
