package com.gsaikiran.ping.annotations;

import com.gsaikiran.ping.PingInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author sairepos
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ping {

    String interceptor() ;


    String fallback() default "";
}
