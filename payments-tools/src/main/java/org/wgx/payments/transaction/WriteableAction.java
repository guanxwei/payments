package org.wgx.payments.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Markable annotation that can be annotated on any methods to indicate that these method are designed
 * to update the memcache status, like add or delete the keys. In normal cases 
 *
 * Only one requirement must be fulfilled if you want to use this annotation to help manage transaction when
 * integration with memcache, that is:
 * the target method should return boolean type. 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteableAction {

}
