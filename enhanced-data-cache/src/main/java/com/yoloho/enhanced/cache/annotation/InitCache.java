package com.yoloho.enhanced.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.yoloho.enhanced.data.cache.xml.InitCacheConfiguration;

/**
 * Initialize cache global configuration
 * 
 * @author jason
 *
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(InitCacheConfiguration.class)
public @interface InitCache {
    String namespace() default "";
    String redisRef() default "plainRedisTemplate";
    /**
     * Whether to use AspectJ mode. Default to be false. 
     * It works only if to run with vm args: javaagent:aspectjweaver.jar
     */
    boolean useAspectJ() default false;
}
