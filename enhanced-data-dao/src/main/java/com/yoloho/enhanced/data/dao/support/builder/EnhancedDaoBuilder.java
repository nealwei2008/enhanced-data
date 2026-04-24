package com.yoloho.enhanced.data.dao.support.builder;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import com.yoloho.enhanced.common.util.StringUtil;
import com.yoloho.enhanced.data.dao.api.Enhanced;
import com.yoloho.enhanced.data.dao.api.EnhancedType;
import com.yoloho.enhanced.data.dao.api.dialect.DialectType;
import com.yoloho.enhanced.data.dao.impl.EnhancedDaoImpl;
import com.yoloho.enhanced.data.dao.impl.MysqlEnhancedDaoImpl;
import com.yoloho.enhanced.data.dao.impl.PostgreSqlEnhancedDaoImpl;
import com.yoloho.enhanced.data.dao.support.EnhancedDaoConstants;

import io.github.lukehutch.fastclasspathscanner.scanner.AnnotationInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.AnnotationInfo.AnnotationParamValue;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;

/**
 * enhanced-dao 默认 DAO Bean 构建器。
 * <p>
 * 该构建器负责根据实体类和扫描配置生成 {@link EnhancedDaoImpl} BeanDefinition。
 * V1 方言改造后，构建器会把扫描配置中的方言类型注入 DAO Bean，最终由 DAO 在绑定
 * {@code SqlSessionFactory} 后解析出实际 {@code SqlDialect}。
 *
 * @author neal_wei @ Apr 23, 2026
 */
public class EnhancedDaoBuilder implements DaoBuilder{

	@Override
	public EnhancedType getType() {
		return EnhancedType.ENHANCED;
	}

	@Override
	public BeanWrapper build(BuildContext buildContext, String sqlFactoryName) {
        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(resolveBeanClass(buildContext.getConfig().getDialect()));

        ClassInfo classInfo = buildContext.getClazzInfo();
        List<AnnotationInfo> listAnno = classInfo.getAnnotationInfo();
        String beanName = null;
        String tableName = null;
        
        for (AnnotationInfo annotationInfo : listAnno) {
            if (annotationInfo.getAnnotationType().isAssignableFrom(Enhanced.class)) {
                List<AnnotationParamValue> values = annotationInfo.getAnnotationParamValues();
                if (values != null && values.size() > 0) {
                    for (AnnotationParamValue annotationParamValue : values) {
                        String name = annotationParamValue.getParamName();
                        if (name.equals("name")) {
                            beanName = (String) annotationParamValue.getParamValue();
                        } else if (name.equals("tableName")) {
                            tableName = (String) annotationParamValue.getParamValue();
                        }
                    }
                }
            }
        }
        if (beanName == null || beanName.length() == 0) {
            String simpleName = EnhancedDaoConstants.patternClassName.matcher(classInfo.getClassName()).replaceAll("$1");
            StringBuilder beanNameBuilder = new StringBuilder();
            if (StringUtils.isNotEmpty(buildContext.getConfig().getPrefix())) {
                beanNameBuilder.append(buildContext.getConfig().getPrefix());
            }
            beanNameBuilder.append(StringUtil.toCamel(simpleName)).append(buildContext.getConfig().getPostfix());
            beanName = beanNameBuilder.toString();
        }
        daoBuilder.addConstructorArgValue(classInfo.getClassName());
        daoBuilder.addConstructorArgValue(tableName);
        daoBuilder.addConstructorArgReference(sqlFactoryName);
        daoBuilder.addPropertyValue("dialectType", buildContext.getConfig().getDialect());
        daoBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        daoBuilder.setRole(BeanDefinition.ROLE_APPLICATION);
        daoBuilder.addDependsOn(buildContext.getScannerBeanName());

        return BeanWrapper.instance(beanName, daoBuilder.getBeanDefinition());
	}

    private Class<?> resolveBeanClass(DialectType dialectType) {
        if (dialectType == DialectType.MYSQL) {
            return MysqlEnhancedDaoImpl.class;
        }
        if (dialectType == DialectType.POSTGRESQL) {
            return PostgreSqlEnhancedDaoImpl.class;
        }
        return EnhancedDaoImpl.class;
    }
	
}
