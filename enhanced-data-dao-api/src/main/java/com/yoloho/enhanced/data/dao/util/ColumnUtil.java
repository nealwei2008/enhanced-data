package com.yoloho.enhanced.data.dao.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.yoloho.enhanced.common.util.StringUtil;
import com.yoloho.enhanced.data.dao.api.IgnoreKey;
import com.yoloho.enhanced.data.dao.api.dialect.SqlDialect;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

/**
 * 字段占位符解析工具。
 * <p>
 * 该工具负责把表达式中的字段占位符转换成数据库列名，例如把 {@code @displayName@}
 * 转成实际列名。为了兼容老项目，无方言参数的旧方法仅保留历史反引号行为；
 * 新执行链路应传入启动期绑定的 {@link SqlDialect}，由方言决定标识符引用方式。
 * <p>
 * 该类只处理字段名转换和引用，不负责校验业务表达式是否安全。
 *
 * @author jason
 * @author neal_wei @ Apr 23, 2026
 */
public class ColumnUtil {
    /**
     * 用来识别内容中包括的列名替换位
     * <p>
     * "@__self__@" <br>
     * "@parentId@"
     */
    private final static Pattern patternFieldNamePlaceHolder = Pattern.compile("@([a-zA-Z0-9_-]+)@");
    
    private final static LoadingCache<Class<?>, Map<String, String>> fieldsNameCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1800, TimeUnit.SECONDS)
            .build(new CacheLoader<Class<?>, Map<String, String>>() {

                @Override
                public Map<String, String> load(Class<?> cls) throws Exception {
                    if (cls == null) {
                        return null;
                    }
                    Map<String, String> fieldNameMap = Maps.newConcurrentMap();
                    Field[] fields = cls.getDeclaredFields();
                    for (Field field : fields) {
                        String fieldName = field.getName();
                        if (fieldName.equals("serialVersionUID")) {
                            continue;
                        }
                        if (field.isAnnotationPresent(IgnoreKey.class)) {
                            continue;
                        }
                        String propertyName = StringUtil.toCamel(fieldName);
                        String columnName = StringUtil.toUnderline(fieldName);
                        fieldNameMap.put(propertyName, columnName);
                    }
                    return fieldNameMap;
                }
                
            });
    
    /**
     * 根据给定的类对字符串中可能存在的替换位做替换
     * 
     * @param self
     *      当前的属性名
     * @param str
     *      待解析的字符串
     * @param clz
     *      所属的类
     * @return
     */
    public static String parseColumnNames(String self, String str, Class<?> clz) {
        return parseColumnNames(self, str, clz, null);
    }

    /**
     * 根据给定的类和数据库方言，对字符串中可能存在的列名替换位做替换。
     *
     * @param self
     *      当前的属性名
     * @param str
     *      待解析的字符串
     * @param clz
     *      所属的类
     * @param dialect
     *      SQL 方言
     * @return
     */
    public static String parseColumnNames(String self, String str, Class<?> clz, SqlDialect dialect) {
        if (StringUtils.isEmpty(str) || !str.contains("@")) {
            return str;
        }
        Matcher matcher = patternFieldNamePlaceHolder.matcher(str);
        StringBuffer sb = new StringBuffer();
        Map<String, String> fieldsMapping;
        try {
            fieldsMapping = fieldsNameCache.get(clz);
        } catch (ExecutionException e) {
            throw new RuntimeException("解析替换位时出错", e);
        }
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (fieldName.equals("__self__")) {
                if (StringUtils.isEmpty(self)) {
                    throw new RuntimeException("没有指定当前属性名");
                }
                //对self特殊处理
                fieldName = self;
            }
            if (fieldsMapping.containsKey(fieldName)) {
                // found field, replace
                String columnName = fieldsMapping.get(fieldName);
                matcher.appendReplacement(sb, dialect == null
                        ? String.format("`%s`", columnName)
                        : dialect.quoteIdentifier(columnName));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
