package com.xhaleera.whiteshark.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation identifying serializable class members
 * 
 * @author Christophe SAUVEUR <christophe@xhaleera.com>
 * @since 1.0
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface WhiteSharkSerializable {

}
