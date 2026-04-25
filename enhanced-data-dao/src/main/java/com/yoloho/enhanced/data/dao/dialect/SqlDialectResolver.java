package com.yoloho.enhanced.data.dao.dialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoloho.enhanced.data.dao.api.dialect.DialectType;
import com.yoloho.enhanced.data.dao.api.dialect.Dialects;
import com.yoloho.enhanced.data.dao.api.dialect.SqlDialect;

/**
 * SQL 方言解析器。
 * <p>
 * 该类负责在 DAO Bean 初始化阶段确定实际使用的数据库方言。解析顺序为：
 * 显式配置、DataSource/JDBC URL、classpath 中的 JDBC Driver 标记。解析不到时直接失败，
 * 避免在 PostgreSQL 等新项目中因为隐式默认值生成错误 SQL。
 * <p>
 * 日志策略：
 * <ul>
 *   <li>输出扫描到的 JDBC Driver 标记。</li>
 *   <li>扫描到多个标记时输出 warning。</li>
 *   <li>若 JDBC URL 能明确判断方言，则以 JDBC URL 为准。</li>
 *   <li>若多个标记且无法判断数据源，则要求显式配置。</li>
 * </ul>
 *
 * @author neal_wei @ Apr 23, 2026
 */
public final class SqlDialectResolver {
    private static final Logger logger = LoggerFactory.getLogger(SqlDialectResolver.class);

    private SqlDialectResolver() {
    }

    /**
     * 按配置和绑定的数据源解析 SQL 方言。
     *
     * @param configuredType
     *      DAO 扫描配置中的方言类型，通常来自注解或 XML；{@code null} 等价于 AUTO
     * @param sqlSessionFactory
     *      DAO 绑定的 SqlSessionFactory，用于读取 DataSource/JDBC URL
     * @return 解析后的 SQL 方言
     */
    public static SqlDialect resolve(DialectType configuredType, SqlSessionFactory sqlSessionFactory) {
        if (configuredType == DialectType.MYSQL) {
            logger.info("enhanced-dao selected SQL dialect by explicit annotation config: mysql");
            return Dialects.mysql();
        }
        if (configuredType == DialectType.POSTGRESQL) {
            logger.info("enhanced-dao selected SQL dialect by explicit annotation config: postgresql");
            return Dialects.postgresql();
        }

        Set<DialectType> classpathMarkers = detectClasspathMarkers();
        if (classpathMarkers.isEmpty()) {
            logger.info("enhanced-dao dialect auto detection found no JDBC driver marker");
        } else {
            logger.info("enhanced-dao dialect auto detection found JDBC driver markers: {}", classpathMarkers);
        }
        if (classpathMarkers.size() > 1) {
            logger.warn("enhanced-dao detected multiple SQL dialect markers: {}", classpathMarkers);
        }

        DialectType urlDialect = detectByJdbcUrl(sqlSessionFactory);
        if (urlDialect != null) {
            logger.info("enhanced-dao selected SQL dialect by datasource JDBC URL: {}", urlDialect.name().toLowerCase());
            return toDialect(urlDialect);
        }

        if (classpathMarkers.size() == 1) {
            DialectType marker = classpathMarkers.iterator().next();
            logger.info("enhanced-dao selected SQL dialect by single JDBC driver marker: {}", marker.name().toLowerCase());
            return toDialect(marker);
        }
        if (classpathMarkers.size() > 1) {
            throw new IllegalStateException("Multiple enhanced-dao SQL dialect integrations detected "
                    + classpathMarkers + ", but datasource JDBC URL is unavailable. Please configure dialect explicitly.");
        }
        throw new IllegalStateException("No enhanced-dao SQL dialect integration found. "
                + "Please add mysql/postgresql integration dependency or configure dialect explicitly.");
    }

    private static Set<DialectType> detectClasspathMarkers() {
        Set<DialectType> markers = new LinkedHashSet<>();
        if (isPresent("com.mysql.cj.jdbc.Driver") || isPresent("com.mysql.jdbc.Driver")) {
            markers.add(DialectType.MYSQL);
        }
        if (isPresent("org.postgresql.Driver")) {
            markers.add(DialectType.POSTGRESQL);
        }
        return markers;
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static DialectType detectByJdbcUrl(SqlSessionFactory sqlSessionFactory) {
        if (sqlSessionFactory == null
                || sqlSessionFactory.getConfiguration() == null
                || sqlSessionFactory.getConfiguration().getEnvironment() == null) {
            return null;
        }
        DataSource dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        if (dataSource == null) {
            return null;
        }
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            logger.info("enhanced-dao dialect auto detection datasource URL: {}", url);
            return detectByJdbcUrl(url);
        } catch (SQLException e) {
            logger.warn("enhanced-dao can not inspect datasource JDBC URL for dialect auto detection", e);
            return null;
        }
    }

    static DialectType detectByJdbcUrl(String url) {
        if (url == null) {
            return null;
        }
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.startsWith("jdbc:mysql:")) {
            return DialectType.MYSQL;
        }
        if (lowerUrl.startsWith("jdbc:postgresql:")) {
            return DialectType.POSTGRESQL;
        }
        return null;
    }

    private static SqlDialect toDialect(DialectType type) {
        if (type == DialectType.POSTGRESQL) {
            return Dialects.postgresql();
        }
        if (type == DialectType.MYSQL) {
            return Dialects.mysql();
        }
        throw new IllegalArgumentException("Unsupported resolved dialect type: " + type);
    }
}
