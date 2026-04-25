package com.yoloho.enhanced.data.dao.api;

import java.io.Serializable;

import com.yoloho.enhanced.data.dao.api.dialect.DialectType;
import com.yoloho.enhanced.data.dao.api.dialect.SqlDialect;

/**
 * 表达式条件。
 * <p>
 * 历史表达式用于在 {@link com.yoloho.enhanced.data.dao.api.filter.DynamicQueryFilter}
 * 中引用其它列或当前列；V1 方言改造后也用于承载单表方言表达式的渲染器。
 * <p>
 * <b>注意，本类有注入风险，需要严格控制及考虑语法</b>
 * 
 * @author jason
 *
 */
public class ExprEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 方言表达式渲染器。
     * <p>
     * 该接口仅用于描述单表条件中的数据库方言表达式，业务侧不应直接拼接最终 SQL。
     */
    public static interface Renderer extends Serializable {
        /**
         * 渲染方言表达式 SQL 片段。
         *
         * @param dialect
         *      当前 DAO 绑定的 SQL 方言
         * @param columnName
         *      已按方言引用的列名
         * @param parameterPlaceholder
         *      MyBatis 参数占位符
         * @return SQL 条件片段
         */
        String render(SqlDialect dialect, String columnName, String parameterPlaceholder);
    }

    private String value;
    private Class<?> clz;
    private DialectType dialectType;
    private String fieldName;
    private Renderer renderer;
    
    /**
     * 需要引用其它列时，@fieldName@，不能直接指定为列的数据库名字
     * 需要引用当前的列时，@__self__@
     * <p>
     * <b>本类有注入风险，需要严格控制及考虑语法</b>
     * 
     * @param value
     *          表达式
     * @param clz
     *          DynamicQueryFilter所绑定的类
     * 
     */
    public ExprEntry(String value, Class<?> clz) {
        setValue(value);
        setClz(clz);
    }

    /**
     * 构造方言表达式。
     *
     * @param dialectType
     *          表达式所属方言
     * @param clz
     *          DynamicQueryFilter 所绑定的类
     * @param fieldName
     *          属性名
     * @param value
     *          参数值
     * @param renderer
     *          SQL 片段渲染器
     * @return 表达式条件
     */
    public static ExprEntry of(DialectType dialectType, Class<?> clz, String fieldName, String value,
            Renderer renderer) {
        ExprEntry entry = new ExprEntry(value, clz);
        entry.setDialectType(dialectType);
        entry.setFieldName(fieldName);
        entry.setRenderer(renderer);
        return entry;
    }
    
    public String getValue() {
        return value;
    }
    
    public Class<?> getClz() {
        return clz;
    }

    public DialectType getDialectType() {
        return dialectType;
    }

    public void setDialectType(DialectType dialectType) {
        this.dialectType = dialectType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    /**
     * 判断当前表达式是否为方言表达式。
     *
     * @return true 表示该表达式必须通过指定 SQL 方言渲染
     */
    public boolean isDialectExpression() {
        return dialectType != null && fieldName != null && renderer != null;
    }

    /**
     * 渲染方言表达式 SQL 片段。
     *
     * @param dialect
     *      当前 SQL 方言
     * @param columnName
     *      已按方言引用的列名
     * @param parameterPlaceholder
     *      MyBatis 参数占位符
     * @return SQL 条件片段
     */
    public String render(SqlDialect dialect, String columnName, String parameterPlaceholder) {
        return renderer.render(dialect, columnName, parameterPlaceholder);
    }
    
    /**
     * 要更新的值
     * 
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    public void setClz(Class<?> clz) {
        this.clz = clz;
    }
    
}
