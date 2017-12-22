package org.wgx.payments.flowcontrol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be annotated on any method to help control the visiting flow.
 * @author hzweiguanxiong
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowControl {

    /**
     * Global unique key to help automatically configure & update the waiting queue size.
     * @return
     */
    String key();

    /**
     * 触发限流后返回给前端的错误提示信息，不同的接口可能会有不同的需求，这边允许用户在方法层面进行配置.
     * 前端可以直接使用后台返回的错误提示，也可以按照需求在每个HTTP接口层面自行配置.
     * @return
     */
    String message() default "访问的人太多啦，请稍后重试！";
}
