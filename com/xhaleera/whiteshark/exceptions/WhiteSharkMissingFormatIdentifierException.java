package com.xhaleera.whiteshark.exceptions;

/**
 * Exception thrown when the stream format identifier cannot be found.
 * 
 * @author Christophe SAUVEUR
 * @since 1.0
 * @version 1.0
 */
public class WhiteSharkMissingFormatIdentifierException extends WhiteSharkException {

	/** Serialization version UID */
	static final long serialVersionUID = 1;
	
	/**
	 * Constructor with custom message
	 * @param arg0 Custom message
	 */
	public WhiteSharkMissingFormatIdentifierException(String arg0) {
		super(arg0);
	}

}
