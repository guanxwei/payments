package org.wgx.payments.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation restricted used on dao implementation class.
 * Used to configure table name, insert method .etc.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableMapping {

    /**
     * Insert method name. Default using save.
     * Advisors will help check if the primary id is set, if not
     * framework will allocate an auto-increasing id for the new inserted entity.
     * @return insert method name
     */
    String insertMethod() default "save";

    /**
     * The table's name.
     * @return database table name
     */
    String table();

    /**
     * Primary id attribute name.
     * @return Primary id attribute name.
     */
    String id() default "id";
}
