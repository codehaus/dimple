package org.codehaus.dimple;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method or methods declared in a class are used
 * for dynamic implementation of interface(s).
 * <p>
 * @author Ben Yu
 * Dec 17, 2006 6:30:27 PM
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Implement {

}
