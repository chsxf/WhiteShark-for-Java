package com.xhaleera.whiteshark.exceptions;

/**
 * Base class for WhiteShark exceptions
 * 
 * @author Christophe SAUVEUR <christophe@xhaleera.com>
 * @since 1.0
 * @version 1.0
 */
public class WhiteSharkException extends Exception {

	/** Serialization version UID */
	static final long serialVersionUID = 1;
	
	/**
	 * Default constructor with no message
	 */
	public WhiteSharkException() {
		super();
	}

	/**
	 * Constructor with custom message
	 * @param arg0 Custom message
	 */
	public WhiteSharkException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor with cause Throwable
	 * @param arg0 Throwable that caused that exception
	 */
	public WhiteSharkException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Constructor with custom message and cause Throwable
	 * @param arg0 Custom message
	 * @param arg1 Throwable that caused that exception
	 */
	public WhiteSharkException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
