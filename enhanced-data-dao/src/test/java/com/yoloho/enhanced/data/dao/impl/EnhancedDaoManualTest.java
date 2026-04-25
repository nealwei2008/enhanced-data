package com.yoloho.enhanced.data.dao.impl;

import java.util.Arrays;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import com.yoloho.enhanced.data.dao.support.EnhancedConfig;
import com.yoloho.enhanced.data.dao.support.EnhancedDaoParser;

/**
 * Direct connect to jdbc
 * Test with mix of xml and java code
 * @author jason
 *
 */
public abstract class EnhancedDaoManualTest extends EnhancedDaoImplTest {
    public static class Bean implements BeanDefinitionRegistryPostProcessor {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            EnhancedConfig config = new EnhancedConfig();
            config.setScanPath(Arrays.asList("com.yoloho.enhanced.data"));
            config.setSqlSessionFactory("mybatisSessionFactory");
            EnhancedDaoParser.scan(config, registry);
        }
    }

}
