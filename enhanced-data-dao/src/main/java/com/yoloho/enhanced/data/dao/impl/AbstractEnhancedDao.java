package com.yoloho.enhanced.data.dao.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yoloho.enhanced.common.util.StringUtil;
import com.yoloho.enhanced.data.dao.api.EnhancedDao;
import com.yoloho.enhanced.data.dao.api.IgnoreKey;
import com.yoloho.enhanced.data.dao.api.PrimaryKey;
import com.yoloho.enhanced.data.dao.api.UnionPrimaryKey;
import com.yoloho.enhanced.data.dao.api.UpdateEntry;
import com.yoloho.enhanced.data.dao.api.dialect.DialectType;
import com.yoloho.enhanced.data.dao.api.dialect.SqlDialect;
import com.yoloho.enhanced.data.dao.api.filter.DynamicQueryFilter;
import com.yoloho.enhanced.data.dao.api.filter.QueryData;
import com.yoloho.enhanced.data.dao.dialect.SqlDialectResolver;
import com.yoloho.enhanced.data.dao.util.ColumnUtil;

/**
 * 增强的dao动态操作类
 * 
 * 本类其实也可以继承，但继承时要注意namespace需要用自己的，与generic类别的相关参数区分开
 * 
 * @author jason<jason@dayima.com> @ May 29, 2018
 * @author neal_wei @ Apr 23, 2026
 *
 * @param <T>
 * @param <PK>
 */
public abstract class AbstractEnhancedDao<T, PK extends Serializable> extends SqlSessionDaoSupport
        implements EnhancedDao<T, PK> {
    public static final Logger logger = LoggerFactory.getLogger(AbstractEnhancedDao.class);
    private static final String KEY_PRIMARY_QUERY = "__EnhancedPrimaryQuery__";
    private static final class Column {
        private String columnName;
        private String quotedColumnName;
        private String PropertyName;
        private boolean primary = false;
        private boolean autoIncrement = false;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public String getQuotedColumnName() {
            return quotedColumnName;
        }
        public void setQuotedColumnName(String quotedColumnName) {
            this.quotedColumnName = quotedColumnName;
        }
        public String getPropertyName() {
            return PropertyName;
        }
        public void setPropertyName(String propertyName) {
            PropertyName = propertyName;
        }
        public boolean isPrimary() {
            return primary;
        }
        public void setPrimary(boolean primary) {
            this.primary = primary;
        }
        public boolean isAutoIncrement() {
            return autoIncrement;
        }
        public void setAutoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
        }
    }

    private SqlSessionFactory sqlSessionFactory;
    private String tableName;
    private Class<T> beanClass;
    private DialectType dialectType = DialectType.AUTO;
    private SqlDialect dialect;
    private Map<String, Column> fieldsMapping = Maps.newConcurrentMap();
    private String fieldsGenerated;
    private Set<String> primaryKeys = Sets.newConcurrentHashSet();
    private int batchSize = 200;
    /**
     * 各namespace常量
     */
    private final static String NAMESPACE = "com.yoloho.enhanced.data.dao.namespace.Generic";
    private final static String NAMESPACE_GET = NAMESPACE + ".get";
    private final static String NAMESPACE_INSERT = NAMESPACE + ".insert";
    private final static String NAMESPACE_DELETE = NAMESPACE + ".delete";
    private final static String NAMESPACE_UPDATE = NAMESPACE + ".update";
    private final static String NAMESPACE_UPDATE_FILETER = NAMESPACE + ".updateByFilter";
    /**
     * 各传递到mapper中map的key标准常量
     */
    private final static String KEY_INSERT = "insertOperation";
    private final static String KEY_PROPERTY_LIST = "properyNameList";
    private final static String KEY_COLUMNS = "columns";
    private final static String KEY_DATA = "data";
    private final static String KEY_AUTO_INCREMENT = "autoIncrementKey";
    
    public AbstractEnhancedDao() {
    }
    
    public AbstractEnhancedDao(String beanClass) throws ClassNotFoundException {
        this(beanClass, null);
    }
    
    public AbstractEnhancedDao(String beanClass, String tableName) throws ClassNotFoundException {
        this(beanClass, tableName, null);
    }
    
    @SuppressWarnings("unchecked")
    public AbstractEnhancedDao(String beanClass, String tableName, SqlSessionFactory sqlSessionFactory) throws ClassNotFoundException {
        if (sqlSessionFactory != null) {
            setSqlSessionFactory(sqlSessionFactory);
        }
        Class<T> cls = (Class<T>) Class.forName(beanClass);
        if (tableName != null && tableName.length() > 0) {
            setTableName(tableName, cls);
        } else {
            setTableName(cls);
        }
    }
    
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
        this.sqlSessionFactory = sqlSessionFactory;
        resolveDialect();
    }

    /**
     * 设置 DAO 绑定的方言类型。
     * <p>
     * Spring Bean 构建阶段会从 {@code @EnableEnhancedDao} 或 XML 配置注入该值。
     * 如果为 AUTO，则在 {@link #setSqlSessionFactory(SqlSessionFactory)} 后通过
     * {@link SqlDialectResolver} 自动识别。
     *
     * @param dialectType 方言类型
     */
    public void setDialectType(DialectType dialectType) {
        this.dialectType = dialectType == null ? DialectType.AUTO : dialectType;
        resolveDialect();
        if (beanClass != null) {
            setTableName(tableName, beanClass);
        }
    }

    private void resolveDialect() {
        if (sqlSessionFactory == null) {
            return;
        }
        this.dialect = SqlDialectResolver.resolve(dialectType, sqlSessionFactory);
    }

    private SqlDialect getDialect() {
        if (dialect == null) {
            resolveDialect();
        }
        if (dialect == null) {
            throw new IllegalStateException("enhanced-dao SQL dialect has not been resolved");
        }
        return dialect;
    }
    
    /**
     * 批量的插入跟更新时，设置批量提交的最多条目
     * 
     * @param batchSize
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    /**
     * 设置表名字，顺便需要设置bean对象的类
     * 
     * @param tableName
     */
    public void setTableName(String tableName, Class<T> cls) {
        this.beanClass = cls;
        if (tableName != null && tableName.length() > 0) {
            this.tableName = tableName;
        } else {
            this.tableName = StringUtil.toUnderline(cls.getSimpleName());
        }
        this.fieldsMapping.clear();
        this.primaryKeys.clear();
        //fields
        List<Field> fields = Lists.newArrayList();
        Set<String> fieldNameSet = Sets.newHashSet();
        //dig fields with super classes
        Class<?> cur = cls;
        while (cur != null) {
            Field[] arr = cur.getDeclaredFields();
            for (Field field : arr) {
                if (fieldNameSet.contains(field.getName())) {
                    //对于被子类覆盖的属性，跳过
                    continue;
                }
                if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    //跳过final和static
                    continue;
                }
                fields.add(field);
                fieldNameSet.add(field.getName());
            }
            cur = cur.getSuperclass();
        }
        for (Field field : fields) {
            //目前是不限访问类型，这里可以考虑下是否限定继承链中仅protected/public才计入
            String fieldName = field.getName();
            if (fieldName.equals("serialVersionUID")) {
                continue;
            }
            if (field.isAnnotationPresent(IgnoreKey.class)) {
                continue;
            }
            Column column = new Column();
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                //primary
                primaryKeys.add(fieldName);
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                column.setPrimary(true);
                column.setAutoIncrement(primaryKey.autoIncrement());
            }
            String propertyName = StringUtil.toCamel(fieldName);
            String columnName = null;
            columnName = StringUtil.toUnderline(fieldName);
            column.setColumnName(columnName);
            if (dialect != null) {
                column.setQuotedColumnName(dialect.quoteIdentifier(columnName));
            } else {
                column.setQuotedColumnName("`" + columnName + "`");
            }
            column.setPropertyName(propertyName);
            this.fieldsMapping.put(propertyName, column);
            logger.debug("field mapping: {} => {} ({})", propertyName, columnName, fieldName);
        }
        //generate fields
        {
            StringBuffer buffer = new StringBuffer();
            for (Entry<String, Column> entry : this.fieldsMapping.entrySet()) {
                Column column = entry.getValue();
                if (buffer.length() > 0) {
                    buffer.append(",");
                }
                buffer.append(column.getQuotedColumnName());
                if (!column.getPropertyName().equals(column.getColumnName())) {
                    if (dialect != null) {
                        buffer.append(" as ").append(dialect.quoteIdentifier(column.getPropertyName()));
                    } else {
                        buffer.append(" as `").append(column.getPropertyName()).append('`');
                    }
                }
            }
            fieldsGenerated = buffer.toString();
            logger.debug("fields: {}", fieldsGenerated);
        }
        logger.debug("init table: {}", this.tableName);
    }
    
    /**
     * 设置自定义property到column的名字映射
     * @param fieldsMapping
     */
    public void setFieldsMapping(Map<String, String> fieldsMapping) {
        for (Entry<String, String> entry : fieldsMapping.entrySet()) {
            setFieldMapping(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 设置自定义property到column的名字映射
     * @param name
     * @param column_name
     */
    public void setFieldMapping(String name, String column_name) {
        if (this.fieldsMapping.containsKey(name)) {
            Column column = this.fieldsMapping.get(name);
            column.setColumnName(column_name);
            column.setQuotedColumnName(dialect == null ? "`" + column_name + "`" : dialect.quoteIdentifier(column_name));
            rebuildFieldsGenerated();
        }
    }

    private void rebuildFieldsGenerated() {
        StringBuffer buffer = new StringBuffer();
        for (Entry<String, Column> entry : this.fieldsMapping.entrySet()) {
            Column column = entry.getValue();
            if (buffer.length() > 0) {
                buffer.append(",");
            }
            buffer.append(column.getQuotedColumnName());
            if (!column.getPropertyName().equals(column.getColumnName())) {
                if (dialect == null) {
                    buffer.append(" as `").append(column.getPropertyName()).append('`');
                } else {
                    buffer.append(" as ").append(dialect.quoteIdentifier(column.getPropertyName()));
                }
            }
        }
        fieldsGenerated = buffer.toString();
    }
    
    /**
     * 仅通过bean类来设置所有信息
     * 表名字为下划线＋全小写规则
     * 
     * @param cls
     */
    public void setTableName(Class<T> cls) {
        setTableName(null, cls);
    }

    @Override
    public int insert(T bean) {
        return insert(bean, false);
    }
    
    public int insert(T bean, boolean ignore) {
        if (ignore) {
            return insertIgnore(bean);
        }
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
        return insert(beans, ignore);
    }
    
    @Override
    public T insertAndReturn(T bean) {
        return insertAndReturn(bean, false);
    }
    
    public T insertAndReturn(T bean, boolean ignore) {
        if (ignore) {
            return insertIgnoreAndReturn(bean);
        }
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
        List<T> list = insertAndReturn(beans, ignore);
        return list.get(0);
    }
    
    @Override
    public int insert(List<T> beanList) {
        return insert(beanList, false);
    }
    
    public int insert(List<T> beanList, boolean ignore) {
        if (ignore) {
            return insertIgnore(beanList);
        }
        return insert(beanList, ignore, false);
    }

    public int insertIgnore(T bean) {
        ensureInsertIgnoreSupported();
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
        return insertIgnore(beans);
    }

    public int insertIgnore(List<T> beanList) {
        ensureInsertIgnoreSupported();
        return insert(beanList, true, false);
    }

    private void ensureInsertIgnoreSupported() {
        if (!getDialect().supportsInsertIgnore()) {
            throw new UnsupportedOperationException("Current SQL dialect does not support insert ignore: "
                    + getDialect().name());
        }
    }
    
    private int insert(List<T> beanList, boolean ignore, boolean replace) {
        return insert(beanList, ignore, replace, "");
    }

    /**
     * 通用插入执行入口。
     *
     * @param beanList
     *      待写入对象集合
     * @param ignore
     *      MySQL insert ignore 兼容开关
     * @param replace
     *      MySQL replace into 兼容开关
     * @param insertSuffixSql
     *      方言特定 insert 后缀
     * @return 受影响行数
     */
    protected int insert(List<T> beanList, boolean ignore, boolean replace, String insertSuffixSql) {
        int count = 0;
        List<String> insertProperyNameList = getInsertPropertyNameList();
        EnhancedCondition dataBase = new EnhancedCondition("", getDialect().quoteIdentifier(tableName));
        dataBase.put(KEY_PROPERTY_LIST, insertProperyNameList);
        dataBase.put(KEY_COLUMNS, fieldsMapping);
        if (hasAutoIncrementKey()) {
            dataBase.put(KEY_AUTO_INCREMENT, 0L);
        }
        dataBase.setInsertSuffixSql(insertSuffixSql);
        dataBase.put(KEY_INSERT, getInsertOperation(ignore, replace));
        if (beanList.size() == 1) {
            // 单条不开启batch
            T bean = beanList.get(0);
            dataBase.put(KEY_DATA, bean);
            count += getSqlSession().insert(NAMESPACE_INSERT, dataBase);
        } else {
            // batch
            int len = beanList.size();
            List<EnhancedCondition> dataList = Lists.newArrayListWithCapacity(len);
            try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                int cur = 0;
                for (T bean : beanList) {
                    EnhancedCondition data = new EnhancedCondition(dataBase);
                    dataList.add(data);
                    data.put(KEY_DATA, bean);
                    session.insert(NAMESPACE_INSERT, data);
                    cur ++;
                    if (cur % batchSize == 0) {
                        //reach batch
                        List<BatchResult> results = session.flushStatements();
                        if (results.size() > 0) {
                            BatchResult batchResult = results.get(0);
                            for (int num : batchResult.getUpdateCounts()) {
                                count += num;
                            }
                            logger.debug("reach a batch");
                        }
                    }
                }
                List<BatchResult> results = session.flushStatements();
                if (results.size() > 0) {
                    BatchResult batchResult = results.get(0);
                    for (int num : batchResult.getUpdateCounts()) {
                        count += num;
                    }
                }
                session.commit();
            }
        }
        return count;
    }
    
    private void setAutoIncrementField(T bean, long autoIncrementKey) {
        for (String key : primaryKeys) {
            try {
                if (fieldsMapping.containsKey(key)) {
                    Column column = fieldsMapping.get(key);
                    if (column.isAutoIncrement()) {
                        Field field = bean.getClass().getDeclaredField(column.getPropertyName());
                        if (field != null) {
                            if (!field.canAccess(bean)) {
                                field.setAccessible(true);
                            }
                            if (field.getType() == int.class) {
                                field.setInt(bean, (int) autoIncrementKey);
                            } else if (Integer.class.isAssignableFrom(field.getType())) {
                                field.set(bean, Integer.valueOf((int)autoIncrementKey));
                            } else if (field.getType() == long.class) {
                                field.setLong(bean, autoIncrementKey);
                            } else if (Long.class.isAssignableFrom(field.getType())) {
                                field.set(bean, Long.valueOf(autoIncrementKey));
                            } else if (field.getType() == short.class) {
                                field.setShort(bean, (short) autoIncrementKey);
                            } else if (Short.class.isAssignableFrom(field.getType())) {
                                field.set(bean, Short.valueOf((short) autoIncrementKey));
                            } else if (field.getType() == byte.class) {
                                field.setByte(bean, (byte) autoIncrementKey);
                            } else if (Byte.class.isAssignableFrom(field.getType())) {
                                field.set(bean, Byte.valueOf((byte) autoIncrementKey));
                            } else if (field.getType() == char.class) {
                                field.setChar(bean, (char) autoIncrementKey);
                            } else if (Character.class.isAssignableFrom(field.getType())) {
                                field.set(bean, Character.valueOf((char) autoIncrementKey));
                            }
                        }
                        break;
                    }
                }
            } catch (NoSuchFieldException e) {
            } catch (SecurityException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
    }
    
    @Override
    public List<T> insertAndReturn(List<T> beanList) {
        return insertAndReturn(beanList, false);
    }
    
    public List<T> insertAndReturn(List<T> beanList, boolean ignore) {
        if (ignore) {
            return insertIgnoreAndReturn(beanList);
        }
        return insertAndReturn(beanList, ignore, false);
    }

    public T insertIgnoreAndReturn(T bean) {
        ensureInsertIgnoreSupported();
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
        List<T> list = insertIgnoreAndReturn(beans);
        return list.get(0);
    }

    public List<T> insertIgnoreAndReturn(List<T> beanList) {
        ensureInsertIgnoreSupported();
        return insertAndReturn(beanList, true, false);
    }
        
    private List<T> insertAndReturn(List<T> beanList, boolean ignore, boolean replace) {
        return _insertAndReturn(beanList, ignore, replace, "");
    }
    
    private void setAutoincreKeyForList(List<T> beanList, BatchResult batchResult, int offset) {
        int count = batchResult.getParameterObjects().size();
        for (int i = 0; i < count; i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataReturn = (Map<String, Object>)batchResult.getParameterObjects().get(i);
            if (dataReturn.containsKey(KEY_AUTO_INCREMENT)) {
                long autoIncrementKey = NumberUtils.toLong(dataReturn.get(KEY_AUTO_INCREMENT).toString(), 0);
                if (autoIncrementKey > 0) {
                    if (offset + i >= beanList.size()) {
                        logger.warn("Results from mybatis out of bounds");
                        break;
                    }
                    setAutoIncrementField(beanList.get(offset + i), autoIncrementKey);
                }
            }
        }
    }
    
    private List<T> _insertAndReturn(List<T> beanList, boolean ignore, boolean replace) {
        return _insertAndReturn(beanList, ignore, replace, "");
    }

    /**
     * 通用插入并回填自增主键入口。
     *
     * @param beanList
     *      待写入对象集合
     * @param ignore
     *      MySQL insert ignore 兼容开关
     * @param replace
     *      MySQL replace into 兼容开关
     * @param insertSuffixSql
     *      方言特定 insert 后缀
     * @return 写入对象集合
     */
    protected List<T> insertAndReturn(List<T> beanList, boolean ignore, boolean replace, String insertSuffixSql) {
        return _insertAndReturn(beanList, ignore, replace, insertSuffixSql);
    }

    private List<T> _insertAndReturn(List<T> beanList, boolean ignore, boolean replace, String insertSuffixSql) {
        try {
            List<String> insertProperyNameList = getInsertPropertyNameList();
            EnhancedCondition dataBase = new EnhancedCondition("", getDialect().quoteIdentifier(this.tableName));
            dataBase.put(KEY_PROPERTY_LIST, insertProperyNameList);
            dataBase.put(KEY_COLUMNS, fieldsMapping);
            if (hasAutoIncrementKey()) {
                dataBase.put(KEY_AUTO_INCREMENT, 0L);
            }
            dataBase.setInsertSuffixSql(insertSuffixSql);
            dataBase.put(KEY_INSERT, getInsertOperation(ignore, replace));
            if (beanList.size() == 1) {
                //单条不开启batch
                T bean = beanList.get(0);
                dataBase.put(KEY_DATA, bean);
                int count = getSqlSession().insert(NAMESPACE_INSERT, dataBase);
                if (count > 0 && dataBase.containsKey(KEY_AUTO_INCREMENT)) {
                    long autoIncrementKey = NumberUtils.toLong(dataBase.get(KEY_AUTO_INCREMENT).toString(), 0);
                    if (autoIncrementKey > 0) {
                        setAutoIncrementField(bean, autoIncrementKey);
                    }
                }
            } else {
                //batch
                int len = beanList.size();
                List<EnhancedCondition> dataList = Lists.newArrayListWithCapacity(len);
                try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                    int offset = 0;
                    int cur = 0;
                    for (T bean : beanList) {
                        EnhancedCondition data = new EnhancedCondition(dataBase);
                        dataList.add(data);
                        data.put(KEY_DATA, bean);
                        session.insert(NAMESPACE_INSERT, data);
                        cur ++;
                        if (cur % batchSize == 0) {
                            logger.info("reach a batch");
                            List<BatchResult> results = session.flushStatements();
                            if (results.size() > 0) {
                                for (BatchResult batchResult : results) {
                                    setAutoincreKeyForList(beanList, batchResult, offset);
                                    offset += batchResult.getParameterObjects().size();
                                }
                            }
                        }
                    }
                    List<BatchResult> results = session.flushStatements();
                    if (results.size() > 0) {
                        for (BatchResult batchResult : results) {
                            setAutoincreKeyForList(beanList, batchResult, offset);
                            offset += batchResult.getParameterObjects().size();
                        }
                    }
                    session.commit();
                }
            }
            return beanList;
        } catch (Exception e) {
            logger.error("batchInsert异常", e);
            throw new RuntimeException("操作失败");
        }
    }
    
    public int replace(T bean) {
        ensureReplaceSupported();
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
        return replace(beans);
    }
    
    public int replace(List<T> beanList) {
        ensureReplaceSupported();
        return insert(beanList, false, true);
    }

    private void ensureReplaceSupported() {
        if (!getDialect().supportsReplace()) {
            throw new UnsupportedOperationException("Current SQL dialect does not support replace: "
                    + getDialect().name());
        }
    }
    
    public T replaceAndReturn(T bean) {
        ensureReplaceSupported();
        List<T> beans = Lists.newArrayListWithCapacity(1);
        beans.add(bean);
        beans = replaceAndReturn(beans);
        return beans.get(0);
    }
    
    public List<T> replaceAndReturn(List<T> beanList) {
        ensureReplaceSupported();
        return insertAndReturn(beanList, false, true);
    }

    /**
     * 构造 PostgreSQL {@code on conflict (...) do nothing} 后缀。
     *
     * @param conflictColumns
     *      冲突目标属性名
     * @return insert suffix SQL
     */
    protected String buildOnConflictDoNothingSuffix(String... conflictColumns) {
        if (conflictColumns == null || conflictColumns.length == 0) {
            throw new IllegalArgumentException("conflictColumns must not be empty");
        }
        StringBuilder builder = new StringBuilder(" on conflict (");
        for (int i = 0; i < conflictColumns.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(quoteColumnByProperty(conflictColumns[i]));
        }
        builder.append(") do nothing");
        return builder.toString();
    }

    /**
     * 使用附加 insert suffix 执行插入。
     *
     * @param beanList
     *      待写入对象集合
     * @param insertSuffixSql
     *      方言特定 insert 后缀
     * @return 受影响行数
     */
    protected int insertWithSuffix(List<T> beanList, String insertSuffixSql) {
        return insert(beanList, false, false, insertSuffixSql);
    }

    /**
     * 使用附加 insert suffix 执行插入并返回对象。
     *
     * @param beanList
     *      待写入对象集合
     * @param insertSuffixSql
     *      方言特定 insert 后缀
     * @return 写入对象集合
     */
    protected List<T> insertAndReturnWithSuffix(List<T> beanList, String insertSuffixSql) {
        return insertAndReturn(beanList, false, false, insertSuffixSql);
    }

    private List<String> getInsertPropertyNameList() {
        List<String> insertProperyNameList = Lists.newArrayList();
        for (Entry<String, Column> entry : fieldsMapping.entrySet()) {
            if (entry.getValue().isAutoIncrement()) {
                continue;
            }
            insertProperyNameList.add(entry.getKey());
        }
        return insertProperyNameList;
    }

    private boolean hasAutoIncrementKey() {
        for (Column column : fieldsMapping.values()) {
            if (column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按实体属性名查找并引用列名。
     *
     * @param propertyName
     *      实体属性名
     * @return 按当前方言引用后的列名
     */
    protected String quoteColumnByProperty(String propertyName) {
        if (fieldsMapping.containsKey(propertyName)) {
            return fieldsMapping.get(propertyName).getQuotedColumnName();
        }
        return getDialect().quoteIdentifier(StringUtil.toUnderline(propertyName));
    }

    /**
     * 获取 insert 操作关键字。
     * <p>
     * MySQL 保留 {@code insert ignore}/{@code replace into} 兼容行为；其他方言可覆盖该方法。
     *
     * @param ignore
     *      是否使用 insert ignore
     * @param replace
     *      是否使用 replace into
     * @return insert 操作关键字
     */
    protected String getInsertOperation(boolean ignore, boolean replace) {
        if (replace) {
            return "replace into";
        }
        if (ignore) {
            return "insert ignore";
        }
        return "insert";
    }

    @Override
    @SuppressWarnings("unchecked")
    public int remove(PK... keys) {
        return remove(Arrays.asList(keys));
    }
    
    private QueryData getPrimaryQueryData(PK key) {
        if (key == null) {
            throw new RuntimeException("主键不能为null");
        }
        DynamicQueryFilter filter = new DynamicQueryFilter();
        if (key instanceof UnionPrimaryKey) {
            //union key
            UnionPrimaryKey primaryValues = (UnionPrimaryKey)key;
            for (String name : primaryKeys) {
                filter.equalPair(name, primaryValues.get(name).toString());
            }
        } else {
            //simple key
            for (String name : primaryKeys) {
                //这里暂时先简单粗暴地处理掉
                filter.equalPair(name, key.toString());
            }
        }
        filter.limit(1);
        QueryData queryData = renderQueryData(filter);
        queryData.put(KEY_PRIMARY_QUERY, Boolean.TRUE);
        return queryData;
    }
    
    private QueryData getPrimaryQueryData(List<PK> keyList) {
        if (keyList == null || keyList.size() == 0) {
            throw new RuntimeException("主键集合不能为null或空");
        }
        DynamicQueryFilter filter = new DynamicQueryFilter();
        if (keyList.get(0) instanceof UnionPrimaryKey) {
            //union key
            filter.or();
            for (int i = 0; i < keyList.size(); i++) {
                UnionPrimaryKey primaryValues = (UnionPrimaryKey)keyList.get(i);
                DynamicQueryFilter subFilter = new DynamicQueryFilter();
                for (String name : primaryKeys) {
                    Object obj = primaryValues.get(name);
                    Preconditions.checkNotNull(obj, "主键不能有null值");
                    subFilter.equalPair(name, obj.toString());
                }
                filter.addSubFilter(subFilter);
            }
        } else {
            //simple single key
            for (String name : primaryKeys) {
                //这里暂时先简单粗暴地处理掉
                filter.in(name, keyList);
                break;
            }
        }
        filter.limit(keyList.size());
        QueryData queryData = renderQueryData(filter);
        queryData.put(KEY_PRIMARY_QUERY, Boolean.TRUE);
        return queryData;
    }
    
    @Override
    public int remove(QueryData queryData) {
        try {
            //单次最大删除行数硬限制
            if (queryData.getLimit() > 1000) {
                queryData.setLimit(1000);
            }
            QueryData resolvedQueryData = resolveQueryData(queryData);
            rejectUpdateDeleteLimit(resolvedQueryData);
            EnhancedCondition condition = buildCondition("", resolvedQueryData);
            return getSqlSession().delete(NAMESPACE_DELETE, condition);
        } catch (RuntimeException e) {
            logger.error("batchRemove异常", e);
            throw e;
        } catch (Exception e) {
            logger.error("batchRemove异常", e);
            throw new RuntimeException("操作失败");
        }
    }

    @Override
    public int remove(List<PK> keyList) {
        return remove(getPrimaryQueryData(keyList));
    }

    @Override
    public List<T> find(QueryData queryData) {
        List<Map<String, Object>> mapList = find(fieldsGenerated, queryData);
        List<T> list = Lists.newArrayListWithCapacity(mapList.size());
        for (Map<String, Object> map : mapList) {
            list.add(((JSONObject)JSON.toJSON(map)).toJavaObject(beanClass));
        }
        return list;
    }
    
    @Override
    public List<Map<String, Object>> find(String fields, QueryData queryData) {
        return find(fields, tableName, queryData);
    }
    
    private List<Map<String, Object>> find(String fields, String tableName, QueryData queryData) {
        EnhancedCondition condition = buildCondition(fields, queryData);
        List<Map<String, Object>> mapList = getSqlSession().<Map<String, Object>>selectList(NAMESPACE_GET, condition);
        if (mapList == null || mapList.size() == 0) {
            return Collections.emptyList();
        }
        return mapList;
    }

    @Override
    public T get(QueryData queryData) {
        queryData.setLimit(1);
        List<T> list = find(queryData);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
    
    public <TT> int count(String fieldName, TT value) {
        return count(renderQueryData(new DynamicQueryFilter().equalPair(fieldName, value)));
    }

    @Override
    public int count(QueryData queryData) {
        try {
            EnhancedCondition condition = buildCondition("count(1) as count", queryData);
            Map<String, Object> map = getSqlSession().<Map<String, Object>>selectOne(NAMESPACE_GET, condition);
            if (map != null) {
                return NumberUtils.toInt(map.get("count").toString(), 0);
            }
            return 0;
        } catch (Exception e) {
            logger.error("count异常", e);
            throw new RuntimeException("操作失败");
        }
    }
    
    @Override
    public int sum(String fieldName, QueryData queryData) {
        try {
            String columnName = fieldName;
            if (fieldsMapping.containsKey(fieldName)) {
                columnName = fieldsMapping.get(fieldName).getColumnName();
            }
            EnhancedCondition condition = buildCondition(String.format("sum(%s) as sum",
                    getDialect().quoteIdentifier(columnName)), queryData);
            Map<String, Object> map = getSqlSession().<Map<String, Object>>selectOne(NAMESPACE_GET, condition);
            if (map != null) {
                return NumberUtils.toInt(map.get("sum").toString(), 0);
            }
            return 0;
        } catch (Exception e) {
            logger.error("sum异常", e);
            throw new RuntimeException("操作失败");
        }
    }

    @Override
    public T get(PK key) {
        try {
            return get(getPrimaryQueryData(key));
        } catch (Exception e) {
            logger.error("get异常", e);
            throw new RuntimeException("操作失败");
        }
    }
    
    @Override
    public <TT> T get(String name, TT val) {
        List<T> list = find(name, val, 1);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<T> find(PK... keys) {
        return find(Arrays.asList(keys));
    }
    
    @Override
    public List<T> find(List<PK> keys) {
        try {
            if (keys == null || keys.size() == 0) {
                return Collections.emptyList();
            }
            return find(getPrimaryQueryData(keys));
        } catch (Exception e) {
            logger.error("get异常", e);
            throw new RuntimeException("操作失败");
        }
    }
    
    @Override
    public <TT> List<T> find(String name, TT val, int limit) {
        DynamicQueryFilter filter = new DynamicQueryFilter();
        return find(renderQueryData(filter.equalPair(name, val).limit(limit)));
    }

    @Override
    public int update(T bean) {
        List<T> beans = Lists.newArrayList();
        beans.add(bean);
        return update(beans);
    }
    
    private Object getFieldValue(String propertyName, T bean) {
        try {
            Field field = bean.getClass().getDeclaredField(propertyName);
            if (field != null) {
                if (!field.canAccess(bean)) {
                    field.setAccessible(true);
                }
                return field.get(bean);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            logger.error("can not get field: {}", propertyName, e);
        }
        return null;
    }
    
    private QueryData getPrimaryKeyCondition(T bean) {
        DynamicQueryFilter filter = new DynamicQueryFilter();
        for (String propertyName : primaryKeys) {
            Object val = getFieldValue(propertyName, bean);
            if (val == null) {
                throw new RuntimeException("error when get primary field's value");
            }
            if (val.getClass() == int.class) {
                filter.equalPair(propertyName, (int)val);
            } else if (val.getClass() == long.class) {
                filter.equalPair(propertyName, (long)val);
            } else if (Number.class.isAssignableFrom(val.getClass())) {
                filter.equalPair(propertyName, (Number)val);
            } else if (String.class.isAssignableFrom(val.getClass())) {
                filter.equalPair(propertyName, (String)val);
            } else {
                filter.equalPair(propertyName, String.valueOf(val));
            }
        }
        filter.limit(1);
        return renderQueryData(filter);
    }

    @Override
    public int update(List<T> beanList) {
        int count = 0;
        List<String> properyNameList = Lists.newArrayList();
        for (Entry<String, Column> entry : fieldsMapping.entrySet()) {
            //待更新的属性跳过主键
            if (entry.getValue().isPrimary()) {
                continue;
            }
            properyNameList.add(entry.getKey());
        }
        EnhancedCondition dataBase = buildCondition("", null);
        dataBase.put(KEY_PROPERTY_LIST, properyNameList);
        dataBase.put(KEY_COLUMNS, fieldsMapping);
        if (beanList.size() == 1) {
            // 单条不开启batch
            T bean = beanList.get(0);
            dataBase.put(KEY_DATA, bean);
            dataBase.putAll(getPrimaryKeyCondition(bean));
            count += getSqlSession().update(NAMESPACE_UPDATE, dataBase);
        } else {
            // batch
            int len = beanList.size();
            List<Map<String, Object>> dataList = Lists.newArrayListWithCapacity(len);
            try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                int cur = 0;
                for (T bean : beanList) {
                    Map<String, Object> data = Maps.newHashMap();
                    dataList.add(data);
                    data.putAll(dataBase);
                    data.put(KEY_DATA, bean);
                    data.putAll(getPrimaryKeyCondition(bean));
                    session.update(NAMESPACE_UPDATE, data);
                    cur ++;
                    if (cur % batchSize == 0) {
                        //reach batch
                        List<BatchResult> results = session.flushStatements();
                        if (results.size() > 0) {
                            BatchResult batchResult = results.get(0);
                            for (int num : batchResult.getUpdateCounts()) {
                                count += num;
                            }
                            logger.debug("reach a batch");
                        }
                    }
                }
                List<BatchResult> results = session.flushStatements();
                if (results.size() > 0) {
                    BatchResult batchResult = results.get(0);
                    for (int num : batchResult.getUpdateCounts()) {
                        count += num;
                    }
                }
                session.commit();
            }
        }
        return count;
    }
    
    @Override
    public int update(Map<String, UpdateEntry> data, QueryData queryData) {
        if (data == null || data.size() == 0) {
            return 0;
        }
        int count = 0;
        List<String> properyNameList = Lists.newArrayList();
        // 先拼待更新字段
        for (Entry<String, UpdateEntry> entry : data.entrySet()) {
            if (!fieldsMapping.containsKey(entry.getKey())) {
                continue;
            }
            Column field = fieldsMapping.get(entry.getKey());
            //跳过主键
            if (field.isPrimary()) {
                continue;
            }
            properyNameList.add(entry.getKey());
            UpdateEntry updateEntry = entry.getValue();
            if (updateEntry.isPlain()) {
                // 裸值更新
                updateEntry.setValue(ColumnUtil.parseColumnNames(entry.getKey(), updateEntry.getValue(), beanClass,
                        getDialect()));
            }
        }
        if (properyNameList.size() == 0) {
            //没有待更新的键
            return 0;
        }
        QueryData resolvedQueryData = resolveQueryData(queryData);
        rejectUpdateDeleteLimit(resolvedQueryData);
        EnhancedCondition dataBase = buildCondition("", resolvedQueryData);
        dataBase.put(KEY_PROPERTY_LIST, properyNameList);
        dataBase.put(KEY_COLUMNS, fieldsMapping);
        dataBase.put(KEY_DATA, data);
        count += getSqlSession().update(NAMESPACE_UPDATE_FILETER, dataBase);
        return count;
    }

    private QueryData renderQueryData(DynamicQueryFilter filter) {
        return filter.getQueryData(getDialect());
    }

    @SuppressWarnings("unchecked")
    private QueryData resolveQueryData(QueryData queryData) {
        if (queryData == null) {
            return null;
        }
        Object source = queryData.get(DynamicQueryFilter.KEY_FILTER_SOURCE);
        if (source instanceof DynamicQueryFilter) {
            String appendSql = (String) queryData.get(DynamicQueryFilter.KEY_APPEND_SQL);
            if (appendSql != null) {
                return ((DynamicQueryFilter) source).getQueryData(appendSql, getDialect());
            }
            return ((DynamicQueryFilter) source).getQueryData(getDialect());
        }
        return queryData;
    }

    private EnhancedCondition buildCondition(String genericFields, QueryData queryData) {
        QueryData resolvedQueryData = resolveQueryData(queryData);
        String resolvedFields = genericFields;
        if (genericFields == null) {
            resolvedFields = "";
        }
        EnhancedCondition condition = new EnhancedCondition(resolvedFields, getDialect().quoteIdentifier(tableName));
        if (resolvedQueryData != null) {
            condition.putAll(resolvedQueryData);
            int offset = resolvedQueryData.getOffset();
            int limit = resolvedQueryData.getLimit();
            if (limit > 0) {
                condition.setPagingSql(getDialect().renderPaging(offset, limit));
                if (getDialect().supportsUpdateDeleteLimit()) {
                    condition.setUpdateLimitSql(getDialect().renderUpdateLimit(limit));
                    condition.setDeleteLimitSql(getDialect().renderDeleteLimit(limit));
                } else {
                    condition.setUpdateLimitSql("");
                    condition.setDeleteLimitSql("");
                }
            } else {
                condition.setPagingSql("");
                condition.setUpdateLimitSql("");
                condition.setDeleteLimitSql("");
            }
        } else {
            condition.setPagingSql("");
            condition.setUpdateLimitSql("");
            condition.setDeleteLimitSql("");
        }
        return condition;
    }

    private void rejectUpdateDeleteLimit(QueryData queryData) {
        if (queryData == null || queryData.getLimit() <= 0 || getDialect().supportsUpdateDeleteLimit()
                || Boolean.TRUE.equals(queryData.get(KEY_PRIMARY_QUERY))) {
            return;
        }
        Object filterSource = queryData.get(DynamicQueryFilter.KEY_FILTER_SOURCE);
        boolean explicitLimit = !(filterSource instanceof DynamicQueryFilter)
                || Boolean.TRUE.equals(queryData.get(DynamicQueryFilter.KEY_LIMIT_EXPLICIT));
        if (explicitLimit) {
            throw new UnsupportedOperationException(getDialect().name()
                    + " does not use MySQL style update/delete limit in enhanced-dao");
        }
    }
    
    @Override
    public SqlSession getSqlSession() {
        return super.getSqlSession();
    }
}
