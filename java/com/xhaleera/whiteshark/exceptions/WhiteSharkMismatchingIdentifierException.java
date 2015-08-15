package com.xhaleera.whiteshark.exceptions;

/**
 * Exception thrown when the custom stream identifier does not match the one that is excepted.
 * 
 * @author Christophe SAUVEUR (christophe@xhaleera.com)
 * @since 1.0
 * @version 1.0
 */
public class WhiteSharkMismatchingIdentifierException extends WhiteSharkException {

	/** Serialization version UID */
	static final long serialVersionUID = 1;
	
	/**
	 * Constructor with custom message
	 * @param arg0 Custom message
	 */
	public WhiteSharkMismatchingIdentifierException(String arg0) {
		super(arg0);
	}

}
