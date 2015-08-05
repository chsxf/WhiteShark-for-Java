package com.xhaleera.whiteshark.exceptions;

/**
 * Exception thrown when an expected object property element cannot be found.
 * 
 * @author Christophe SAUVEUR <christophe@xhaleera.com>
 * @since 1.0
 * @version 1.0
 */
public class WhiteSharkNotAPropertyException extends WhiteSharkException {

	/** Serialization version UID */
	static final long serialVersionUID = 1;
	
	/**
	 * Constructor with custom message
	 * @param arg0 Custom message
	 */
	public WhiteSharkNotAPropertyException(String arg0) {
		super(arg0);
	}

}
