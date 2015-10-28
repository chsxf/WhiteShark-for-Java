package com.xhaleera.whiteshark.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation defining the serialization version of the class
 * 
 * Used for objects only.
 * If missing, 1 is used.
 * 
 * @author Christophe SAUVEUR (christophe@xhaleera.com)
 * @since 1.0
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface WhiteSharkSerializationVersion {

	int value();
	
}
