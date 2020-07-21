package com.gsaikiran.ping;

import com.gsaikiran.ping.annotations.Fallback;
import com.gsaikiran.ping.annotations.Ping;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * @author sairepos
 */

@Aspect
public class PingAspect {

    @Autowired
    private ApplicationContext context;

    private Executor executor;
    private Map<String, Boolean> interceptorMap = new HashMap<>();
    private Map<String, Method> fallbacks = new HashMap();

    public void setExecutor(Executor executor) {
        if(this.executor==null) {
            this.executor = executor;
        }
    }

    @PostConstruct
    public void init(){
        if(executor==null) {
            executor = new SimpleAsyncTaskExecutor();
        }
    }

    @Around("@annotation(com.gsaikiran.ping.annotations.Ping)")
    public Object ping(ProceedingJoinPoint joinPoint) throws Throwable {
        String interceptorBeanName = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(Ping.class).interceptor();
        //TODO log warning or error when no matching PingInterceptor is found for given interceptorBeanName - at runtime or init
        if(!context.containsBean(interceptorBeanName) || !(context.getBean(interceptorBeanName) instanceof  PingInterceptor )) return joinPoint.proceed();
        PingInterceptor interceptor = context.getBean(interceptorBeanName,PingInterceptor.class);
        this.updateInterceptorMapAsync(interceptor,interceptorBeanName);
        if (interceptorMap.getOrDefault(interceptorBeanName, true)) {
            return joinPoint.proceed();
        }
        if(interceptor.ping()){
            interceptorMap.put(interceptorBeanName,true);
            return joinPoint.proceed();
        }
        else {
            String fallbackMethodName = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(Ping.class).fallback();
            //TODO if method with given fallback name is not found or it does not have same arguments as original method, log warning or error - at runtime or init
            if (StringUtils.isEmpty(fallbackMethodName)) {
                return null;
            }
            if(!fallbacks.containsKey(joinPoint.getSignature().toLongString())){
                Optional<Method> fallbackmethod = Arrays.asList(joinPoint.getTarget().getClass().getMethods()).stream()
                        .filter(a->a.getAnnotations().length>0 && a.getAnnotation(Fallback.class)!=null && a.getAnnotation(Fallback.class).value().equals(fallbackMethodName))
                        .findFirst();
                if(fallbackmethod.isPresent()) {
                    fallbacks.put(joinPoint.getSignature().toLongString(),fallbackmethod.get());
                }
                else{
                    fallbacks.put(joinPoint.getSignature().toLongString(),null);
                }
            }
            if(fallbacks.get(joinPoint.getSignature().toLongString())!=null){
                return fallbacks.get(joinPoint.getSignature().toLongString()).invoke(joinPoint.getThis(), joinPoint.getArgs());
            }
            return null;
        }

    }

    private void updateInterceptorMapAsync(PingInterceptor interceptor,String interceptorBeanName){
        executor.execute(() -> {
            if (!interceptor.ping()) {
                interceptorMap.put(interceptorBeanName, false);
            }});
    }



}
