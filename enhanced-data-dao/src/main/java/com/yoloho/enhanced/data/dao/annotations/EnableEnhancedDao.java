package com.yoloho.enhanced.data.dao.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.yoloho.enhanced.data.dao.api.PrimaryKey;
import com.yoloho.enhanced.data.dao.api.dialect.DialectType;
import com.yoloho.enhanced.data.dao.config.EnableEnhancedDaoConfiguration;

/**
 * 启用 enhanced-dao 的注解入口。
 * <p>
 * 该注解负责声明实体扫描路径、目标 {@code SqlSessionFactory} 和 DAO Bean 命名规则。
 * V1 方言改造后，新增 {@link #dialect()} 用于指定当前扫描出的 DAO 绑定的数据源方言。
 * 默认 {@link DialectType#AUTO} 表示启动期自动识别；无法识别时应启动失败，避免隐式生成错误 SQL。
 *
 * @author neal_wei @ Apr 23, 2026
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableEnhancedDaoConfiguration.class)
public @interface EnableEnhancedDao {
    /**
     * The packages separated by comma to be scanned for models containing {@link PrimaryKey}.<br>
     * The generated dao bean is named by the rule:<br>
     * XXXX => XXXXEnhancedDao
     * 
     * @return
     * @see https://github.com/lukehutch/fast-classpath-scanner/wiki
     */
    String[] scanPath();
    /**
     * Referenced sqlSessionFactory(bean id)
     * 
     * @return
     */
    String sqlSessionFactory() default "sqlSessionFactory";
    
    /**
     * Generated bean's prefix.<br>
     * Default to be empty.
     * 
     * @return
     */
    String prefix() default "";
    
    /**
     * Generated bean's postfix.<br>
     * Default to "EnhancedDao"
     * 
     * @return
     */
    String postfix() default "EnhancedDao";
    
    /**
     * Classpaths to scan other "mapper.xml"(mybatis)
     * 
     * @return
     */
    String[] mapperLocations() default {};

    /**
     * SQL dialect bound to generated DAO beans.
     * <p>
     * AUTO means enhanced-dao will try to detect the dialect from SqlSessionFactory/DataSource
     * and JDBC driver markers.
     *
     * @return
     */
    DialectType dialect() default DialectType.AUTO;
}
