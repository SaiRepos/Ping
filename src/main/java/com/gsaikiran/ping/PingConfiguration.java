package com.gsaikiran.ping;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

/**
 * @author sairepos
 */

import java.util.concurrent.Executor;

public class PingConfiguration implements ImportBeanDefinitionRegistrar {

    PingConfiguration(){}

    private static final String enablePingAnnotation = "com.gsaikiran.ping.annotations.EnablePing";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        if(!annotationMetadata.hasAnnotation(enablePingAnnotation)){
            return;
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(PingAspect.class);
        String customExecutorName =  annotationMetadata.getAnnotationAttributes(enablePingAnnotation).get("executor").toString();
        if(StringUtils.hasText(customExecutorName)){
            Object customExecutor = ((DefaultListableBeanFactory)beanDefinitionRegistry).getBean(customExecutorName);
            if(customExecutor!=null && customExecutor instanceof Executor){
                builder.addPropertyValue("executor",customExecutor);
            }
        }
        beanDefinitionRegistry.registerBeanDefinition(PingAspect.class.getName(),builder.getBeanDefinition());
    }
}
