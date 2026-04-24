package com.yoloho.enhanced.data.dao.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.w3c.dom.Element;

import com.yoloho.enhanced.common.util.JoinerSplitters;
import com.yoloho.enhanced.data.dao.annotations.EnableEnhancedDao;
import com.yoloho.enhanced.data.dao.api.dialect.DialectType;

/**
 * enhanced-dao 扫描配置对象。
 * <p>
 * 该对象承接 XML 和注解两种配置入口，供 DAO 扫描器和 Bean 构建器使用。
 * V1 方言改造后，配置中包含 {@link DialectType}，用于在 DAO Bean 初始化时绑定具体 SQL 方言。
 *
 * @author neal_wei @ Apr 23, 2026
 */
public class EnhancedConfig {
    final static private String ATTR_POSTFIX = "postfix";
    final static private String ATTR_SCAN = "scan-path";
    final static private String ATTR_SQL_SESSION_FACTORY = "sql-session-factory";
    final static private String ATTR_MAPPER_LOCATIONS = "mapper-locations";
    final static private String ATTR_DIALECT = "dialect";
    
    private List<String> scanPath = Collections.emptyList();
    private String sqlSessionFactory = "sqlSessionFactory";
    private String prefix = "";
    private String postfix = "EnhancedDao";
    private String mapperLocations = null;
    private DialectType dialect = DialectType.AUTO;
    
    public EnhancedConfig() {
    }
    
    public EnhancedConfig(Element element) {
        {
            String postfix = element.getAttribute(ATTR_POSTFIX);
            if (StringUtils.isEmpty(postfix)) {
                postfix = "EnhancedDao";
            }
            this.postfix = postfix;
        }
        {
            String path = element.getAttribute(ATTR_SCAN);
            if (StringUtils.isEmpty(path)) {
                throw new RuntimeException("scan path must not be empty");
            }
            this.scanPath = JoinerSplitters.getSplitter(",").splitToList(path);
        }
        {
            String sqlFactoryName = element.getAttribute(ATTR_SQL_SESSION_FACTORY);
            if (StringUtils.isEmpty(sqlFactoryName)) {
                sqlFactoryName = "sqlSessionFactory";
            }
            this.sqlSessionFactory = sqlFactoryName;
        }
        {
            String locations = element.getAttribute(ATTR_MAPPER_LOCATIONS);
            if (StringUtils.isNotEmpty(locations)) {
                this.mapperLocations = locations;
            }
        }
        {
            String dialect = element.getAttribute(ATTR_DIALECT);
            if (StringUtils.isNotEmpty(dialect)) {
                this.dialect = DialectType.valueOf(dialect.toUpperCase());
            }
        }
    }
    
    public EnhancedConfig(AnnotationMetadata annotationMetadata) {
        Map<String, Object> map = annotationMetadata.getAnnotationAttributes(EnableEnhancedDao.class.getName());
        {
            String prefix = (String)map.get("prefix");
            if (StringUtils.isNotEmpty(prefix)) {
                this.prefix = prefix;
            }
        }
        {
            String postfix = (String)map.get("postfix");
            if (StringUtils.isEmpty(postfix)) {
                postfix = "EnhancedDao";
            }
            this.postfix = postfix;
        }
        {
            String[] path = (String[])map.get("scanPath");
            if (path == null || path.length == 0) {
                throw new RuntimeException("scan path must not be empty");
            }
            this.scanPath = Arrays.asList(path);
        }
        {
            String sqlFactoryName = (String)map.get("sqlSessionFactory");
            if (StringUtils.isEmpty(sqlFactoryName)) {
                sqlFactoryName = "sqlSessionFactory";
            }
            this.sqlSessionFactory = sqlFactoryName;
        }
        {
            String[] locations = (String[])map.get("mapperLocations");
            if (locations != null && locations.length > 0) {
                this.mapperLocations = JoinerSplitters.getJoiner(",").join(locations);
            }
        }
        {
            DialectType dialect = (DialectType)map.get("dialect");
            if (dialect != null) {
                this.dialect = dialect;
            }
        }
    }

    public List<String> getScanPath() {
        return scanPath;
    }
    
    public void setScanPath(List<String> scanPath) {
        this.scanPath = scanPath;
    }
    
    public String getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(String sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }
    
    public String getMapperLocations() {
        return mapperLocations;
    }

    public DialectType getDialect() {
        return dialect;
    }

    public void setDialect(DialectType dialect) {
        this.dialect = dialect == null ? DialectType.AUTO : dialect;
    }
}
