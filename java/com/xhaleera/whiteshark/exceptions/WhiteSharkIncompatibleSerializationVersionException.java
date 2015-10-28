package com.xhaleera.whiteshark.exceptions;

/**
 * Exception thrown when serialization versions are found incompatible (runtime is of an earlier version than the unserialized data).
 * 
 * @author Christophe SAUVEUR (christophe@xhaleera.com)
 * @since 1.0
 * @version 1.0
 */
public class WhiteSharkIncompatibleSerializationVersionException extends WhiteSharkException {

	/** Serialization version UID */
	static final long serialVersionUID = 1;
	
	/**
	 * Constructor with custom message
	 * @param arg0 Custom message
	 */
	public WhiteSharkIncompatibleSerializationVersionException(String arg0) {
		super(arg0);
	}

}
