package org.wgx.payments.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark annotation to indicate that the method should be enhanced as dao access method.
 * {@link DAOEnhanceAdvice} will help translate the plain method into dao enhanced method,
 * which means the real action will be delegated to the mybatis proxy.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DAOMethod {

}
