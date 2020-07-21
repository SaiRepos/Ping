package com.gsaikiran.ping.annotations;

import com.gsaikiran.ping.PingConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author sairepos
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({PingConfiguration.class})
public @interface EnablePing {
    String executor() default "";
}
