package com.xhaleera.whiteshark.exceptions;

/**
 * Exception thrown when the deserialized stream uses a format version the deserializer does not support
 * 
 * @author Christophe SAUVEUR
 * @since 1.0
 * @version 1.0
 */
public class WhiteSharkUnsupportedVersionException extends WhiteSharkException {

	/** Serialization version UID */
	static final long serialVersionUID = 1;
	
	/**
	 * Constructor with custom message
	 * @param arg0 Custom message
	 */
	public WhiteSharkUnsupportedVersionException(String arg0) {
		super(arg0);
	}

}
